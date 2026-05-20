# Typed WHERE Conditions with Value Binding — Implementation Plan

## Goals

| Goal | How |
|------|-----|
| No more raw WHERE strings for equality filters | Fluent `Condition` API stores `(column, op, value)` tuples |
| Compile error on typo in column/table name | Column refs use existing `static final DBValue` fields — wrong name → `cannot find symbol` |
| Values bound via `?` (PreparedStatement params) | New executor overload threads `List<Object>` params through `setObject` |
| Zero breaking changes | All existing `fetchTable2(strukt, String where)` calls remain valid |

---

## "Compile-time safety" without annotation processors

JPA-style metamodel generation (`@Column` → `Orders_.customerId`) is **not needed** here.
`DBStrukt` subclasses already declare columns as `public static final DBValue` fields:

```java
public class OrderLine extends DBStrukt {
    public static final DBInteger ORDER_ID  = new DBInteger("order_id");
    public static final DBString  ITEM_CODE = new DBString("item_code");
    // ...
}
```

Using these fields as column references in `Condition.where(OrderLine.ORDER_ID)` gives a compiler
error if the field name is mistyped. No annotation processor required.

---

## API at the call site (target state)

```java
// Single condition
List<OrderLine> lines = fetchTable2(new OrderLine(),
        Condition.where(OrderLine.ORDER_ID).eq(42));

// AND chain
List<OrderLine> lines = fetchTable2(new OrderLine(),
        Condition.where(OrderLine.ORDER_ID).eq(orderId)
                 .and(OrderLine.ITEM_CODE).like("A%"));

// OR
Condition cond = Condition.where(OrderLine.STATUS).eq("open")
                          .or(OrderLine.STATUS).eq("pending");

// FK fetch (uses Condition internally — no change to call site)
List<OrderLine> lines = fetchChildren(new OrderLine(), Order.FK_ORDER, 42);
```

---

## New classes

### `Condition`  (`at.redeye.FrameWork.base.bindtypes`)

Immutable (after construction). Holds a list of `Term` records and can render itself to
SQL + parameter list when given a `StmtCreatorInterface`.

```java
// Simplified internal structure
record Term(DBValue column, String op, Object value, boolean isAnd) {}

// Rendering (called by Transaction, not callers)
String renderWhere(StmtCreatorInterface creator, String tableName);
List<Object> getBindValues();
```

Entry point is a static factory to start the chain:

```java
// Returns a ConditionStep (transient builder step)
Condition.where(MyTable.SOME_COL)   →  ConditionStep
```

### `ConditionStep`  (`at.redeye.FrameWork.base.bindtypes`, package-private)

Transient — holds the pending column reference until an operator method completes the term.

```java
ConditionStep → .eq(val)   → Condition   (term added: col = ?)
             → .ne(val)   → Condition   (col <> ?)
             → .gt(val)   → Condition   (col > ?)
             → .lt(val)   → Condition   (col < ?)
             → .gte(val)  → Condition   (col >= ?)
             → .lte(val)  → Condition   (col <= ?)
             → .like(val) → Condition   (col LIKE ?)
             → .isNull()  → Condition   (col IS NULL  — no bind value)
             → .isNotNull() → Condition (col IS NOT NULL)
```

After each `eq(val)` etc., the returned `Condition` exposes `.and(col)` / `.or(col)` to chain
the next `ConditionStep`.

---

## Phase 1 — `Condition` + `ConditionStep`

**New files:**
- `src/at/redeye/FrameWork/base/bindtypes/Condition.java`
- `src/at/redeye/FrameWork/base/bindtypes/ConditionStep.java`

### `Condition.java` key points

- `private final List<Term> terms` (built by `ConditionStep`)
- `private Condition()` — only `ConditionStep` adds terms
- `public static ConditionStep where(DBValue column)` — entry point
- `public ConditionStep and(DBValue column)` — chain next term with AND
- `public ConditionStep or(DBValue column)` — chain next term with OR
- `public String renderWhere(StmtCreatorInterface creator, String tableName)` — builds
  `WHERE t.col1 = ? AND t.col2 > ?` using `creator.markTableName` / `markColumnName`;
  IS NULL / IS NOT NULL are emitted literally with no `?`
- `public List<Object> getBindValues()` — returns values in term order, skipping IS NULL terms
- Package: `at.redeye.FrameWork.base.bindtypes`

### `ConditionStep.java` key points

- Package-private constructor `ConditionStep(Condition owner, DBValue col, boolean isAnd)`
- Each operator method calls `owner.addTerm(col, op, value, isAnd)` then returns `owner`
- `isNull()` / `isNotNull()` — no value, emit SQL fragment without `?`

---

## Phase 2 — PreparedStatement parameter binding in executor

**Files to modify:**
- `src/at/redeye/SqlDBInterface/SqlDBIO/StmtExecInterface.java`
- `src/at/redeye/SqlDBInterface/SqlDBIO/impl/executor/AbstractStmtExecuter.java`

### New method on `StmtExecInterface`

```java
List<HashMap<String, Object>> fetchTableValue(
        String[] tablenames,
        String whereStmt,
        List<Object> params)
throws SQLException, UnsupportedDBDataTypeException, TableBindingNotRegisteredException;
```

### New implementation in `AbstractStmtExecuter`

Same logic as the existing `fetchTableValue(String[], String)` but after `prepareStatement`:

```java
PreparedStatement s = conn.prepareStatement(stmt);
for (int i = 0; i < params.size(); i++) {
    s.setObject(i + 1, params.get(i));   // JDBC is 1-based
}
ResultSet rs = s.executeQuery();
// ... same result processing as today ...
```

The existing no-params overload is unchanged — it calls `prepareStatement` then `executeQuery()`
with no parameter setting, exactly as today.

---

## Phase 3 — `Transaction.fetchTable2(T strukt, Condition condition)`

**File to modify:** `src/at/redeye/FrameWork/base/transaction/Transaction.java`

New overload:

```java
public <T extends DBStrukt> List<T> fetchTable2(T strukt, Condition condition)
throws SQLException, TableBindingNotRegisteredException,
       UnsupportedDBDataTypeException, WrongBindFileFormatException {

    registerTable(strukt);
    StmtCreatorInterface creator = executer.getStmtCreator();
    String whereSql = condition.renderWhere(creator, strukt.getName());
    List<Object> params = condition.getBindValues();
    // delegates to the new executor overload
    String[] tablenames = { strukt.getName() };
    List<HashMap<String, Object>> result =
            executer.fetchTableValue(tablenames, whereSql, params);
    // ... same result assembly as fetchTable2(T, String) ...
}
```

**Requires exposing `getStmtCreator()` on `StmtExecInterface`** if not already public.
Check `AbstractStmtExecuter` — `stmtCreator` is private; add `getStmtCreator()` getter to
`StmtExecInterface`.

---

## Phase 4 — Refactor FK fetch methods to use `Condition`

**File to modify:** `src/at/redeye/FrameWork/base/transaction/Transaction.java`

Replace `buildWhereForValue(DBStrukt, String, Object)` usages with `Condition`:

```java
// Before (in fetchChildren / fetchParent):
String where = buildWhereForValue(childStrukt, fk.getOwnerColumn(), value);
return fetchTable2(childStrukt, where);

// After:
DBValue ownerCol = childStrukt.getValue(fk.getOwnerColumn());
return fetchTable2(childStrukt, Condition.where(ownerCol).eq(value));
```

Remove `buildWhereForValue` once all call sites are converted.

> **Edge case**: If `getValue(colName)` returns `null` (column not in strukt),
> `Condition.where(null)` must throw `IllegalArgumentException` with a clear message.

---

## Phase 5 — `getStmtCreator()` on `StmtExecInterface` (if missing)

Check whether `StmtExecInterface` exposes `getStmtCreator()`. If not, add:

```java
StmtCreatorInterface getStmtCreator();
```

`AbstractStmtExecuter` already has the field; add the getter there.

---

## Files summary

| File | Change |
|------|--------|
| `src/.../bindtypes/Condition.java` | **NEW** — fluent WHERE builder |
| `src/.../bindtypes/ConditionStep.java` | **NEW** — transient per-column step |
| `SqlDBIO/StmtExecInterface.java` | Add `fetchTableValue(..., List<Object> params)` + `getStmtCreator()` |
| `SqlDBIO/impl/executor/AbstractStmtExecuter.java` | Implement the new overload; add `getStmtCreator()` getter |
| `base/transaction/Transaction.java` | Add `fetchTable2(T, Condition)`; refactor FK methods; remove `buildWhereForValue` |

No changes to `DBStrukt`, `ForeignKeyDefinition`, `DatabaseManager`, or DDL classes.

---

## What this does NOT do (intentional scope limit)

- **No annotation processor** — `static final DBValue` fields are already compile-time-safe symbols
- **No JOIN support** — multi-table queries continue to use raw WHERE strings
- **No ORDER BY / LIMIT in Condition** — those remain raw strings passed to the `String where` overload
- **No change to `fetchTable(DBStrukt, String)` (non-generic)** — legacy method left as-is
- **No change to `updateValues`, `insertValues`, `deleteWithPrimaryKey`** — future plan

---

## Single-source column name pattern

`DBValue.getCopy()` is already implemented on every subtype — it returns a fresh typed
instance with the same column name (and the current default value). Use it to derive the
mutable instance field from the `static final` metadata field so the column name string
is written **exactly once**:

```java
// Column name string written ONCE in the static final declaration.
// Instance field derives name via getCopy() — no second string literal.
public static final DBInteger ORDER_ID = new DBInteger("order_id");
public DBInteger order_id = ORDER_ID.getCopy();   // → new DBInteger("order_id"), value=0
```

`getCopy()` returns the concrete subtype (covariant return), so no cast is needed.
When `consumeFast(map)` loads a row, it calls `loadFromDB` on each field in the instance,
overwriting the default copied value with the actual DB data.

If the column is later renamed, only the `static final` declaration changes — the instance
field and the `add()` call automatically pick up the new name.

---

## Usage example (after implementation)

```java
public class OrderLine extends DBStrukt {
    // Column name written once — static final is the single source of truth
    public static final DBInteger ORDER_ID = new DBInteger("order_id");
    public static final DBString  STATUS   = new DBString("status");

    // Instance fields derived via getCopy() — no repeated string literals
    public DBInteger order_id = ORDER_ID.getCopy();
    public DBString  status   = STATUS.getCopy();

    public static final ForeignKeyDefinition FK_ORDER =
        new ForeignKeyDefinition(ORDER_ID.getName(), "ORDERS", "id");

    public OrderLine() {
        add(order_id, 1);
        add(status, 1);
        addForeignKey(FK_ORDER, 1);
    }

    @Override public DBStrukt getNewOne() { return new OrderLine(); }
    @Override public String getName() { return "ORDER_LINE"; }
}

// --- In a Transaction subclass ---

// Compile-safe: ORDER_ID is a symbol; typo → compiler error
List<OrderLine> open = fetchTable2(new OrderLine(),
    Condition.where(OrderLine.ORDER_ID).eq(42)
             .and(OrderLine.STATUS).eq("open"));

// FK convenience (unchanged call site)
List<OrderLine> children = fetchChildren(new OrderLine(), Order.FK_ORDER, 42);
```
