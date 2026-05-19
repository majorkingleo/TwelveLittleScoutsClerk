---
name: sqldbinterface
description: >
  Domain knowledge for at.redeye.SqlDBInterface — the SQL abstraction layer in the FrameWork project.
  Use when adding new DBMS support, writing new queries, registering table bindings, debugging
  database connectivity, or understanding how SELECT/INSERT/UPDATE flow through the layer.
---

# SqlDBInterface — How It Works

## Purpose

`at.redeye.SqlDBInterface` is a thin, framework-internal SQL abstraction layer. It:

- Hides DBMS-specific JDBC URL construction and driver registration behind a single connect/disconnect interface.
- Provides dialect-aware SQL statement building (identifier quoting differs per DBMS).
- Executes SELECT/INSERT/UPDATE using `PreparedStatement` for type safety.
- Maps database columns to strongly-typed Java values via a **table binding** registry.

It is **not** an ORM. Callers still write or supply WHERE clauses; the layer only handles
column-type marshalling and identifier quoting.

---

## Package Map

```
SqlDBInterface/
├── SqlDBConnection/          # JDBC connection lifecycle
│   ├── DbConnectionInterface.java        # connect / disconnect
│   └── impl/
│       ├── ConnectionDefinition.java     # host, port, user, pwd, instance, DBMS type
│       ├── SupportedDBMSTypes.java       # enum: DB_MYSQL, DB_MARIADB, DB_MSSQL, DB_ORACLE,
│       │                                 #        DB_SQLITE, DB_JAVADB
│       ├── AbstractDBConnector.java      # builds JDBC URL, registers driver, calls DriverManager
│       ├── DBConnector.java              # concrete (empty) subclass — instantiate this
│       ├── MissingConnectionParamException.java
│       └── UnSupportedDatabaseException.java
│
├── SqlDBIO/                  # Statement building & execution
│   ├── StmtCreatorInterface.java         # builds SQL strings (SELECT / INSERT / UPDATE)
│   ├── StmtExecInterface.java            # executes SQL, returns typed Java objects
│   ├── TypeRegistrationInterface.java    # table-binding registry
│   └── impl/
│       ├── DBDataType.java               # enum of supported column types
│       ├── ColumnAttribute.java          # datatype + isPrimaryKey + width
│       ├── TypeRegistration.java         # implements TypeRegistrationInterface
│       ├── creator/
│       │   ├── AbstractStmtCreator.java  # shared SELECT / INSERT / UPDATE builder logic
│       │   ├── StmtCreatorFactory.java   # picks dialect subclass by SupportedDBMSTypes
│       │   ├── DefaultStmtCreator.java   # no-op quoting (fallback)
│       │   ├── StmtCreatorMYSQL.java     # backtick quoting; tables UPPER, columns lower
│       │   ├── StmtCreatorMSSQL.java     # [bracket] quoting
│       │   ├── StmtCreatorSQLITE.java    # backtick quoting; UPDATE uses column-only form
│       │   ├── StmtCreatorDerby.java     # Derby/JavaDB quoting
│       │   └── StmtCreatorOracle.java    # Oracle quoting
│       └── executor/
│           ├── AbstractStmtExecuter.java # PreparedStatement execution + type unmarshalling
│           └── DefaultStmtExecuter.java  # concrete (empty) subclass — instantiate this
│
└── jdbc_driver/              # Vendored README/notes per driver (not framework source)
    ├── javadb/ mariadb/ mssql/ mysql/ oracle/ sqllite/
```

---

## Layer 1 — Connection (`SqlDBConnection`)

### `ConnectionDefinition`

Plain value object. Required fields vary by DBMS:

| DBMS | hostname | port (default) | username | password | instance |
|------|----------|----------------|----------|----------|----------|
| MySQL / MariaDB | ✓ | 3306 | ✓ required | optional | ✓ (DB name) |
| MSSQL | ✓ | 1433 | ✓ required | ✓ required | ✓ (DB name) |
| Oracle | ✓ | 1521 | ✓ required | ✓ required | ✓ (SID/service) |
| SQLite | — | — | — | — | ✓ (file path) |
| JavaDB/Derby | ✓ | 1527 | ✓ | ✓ | ✓ |

### `AbstractDBConnector.connectToDatabase()`

Switch on `SupportedDBMSTypes`:
1. Validates required fields, throws `MissingConnectionParamException` if absent.
2. Registers the appropriate JDBC driver via `DriverManager.registerDriver(new <Driver>())`.
3. Assembles the JDBC URL string.
4. Returns `DriverManager.getConnection(url, user, pwd)`.

`disconnectDatabase(conn)` simply calls `conn.close()`.

### Typical usage

```java
ConnectionDefinition def = new ConnectionDefinition(
    "localhost", 3306, "user", "secret", "mydb", SupportedDBMSTypes.DB_MARIADB);
DbConnectionInterface connector = new DBConnector(def);
Connection conn = connector.connectToDatabase();
// ... use conn ...
connector.disconnectDatabase(conn);
```

---

## Layer 2 — Type Registry (`TypeRegistration`)

Before any table-aware query can run, columns must be registered so the executor knows
their Java type and whether they are primary keys.

### Option A — Bind file (CSV)

```
TABLENAME          ← line 1: table name (uppercased automatically)
colname,type,pk    ← subsequent lines
id,int,true
name,string,false
valid,bool,false
```

```java
TypeRegistrationInterface reg = new TypeRegistration(SupportedDBMSTypes.DB_MYSQL);
reg.registerTableBindings("myapp/tables/mytable.bind");
```

### Option B — Programmatic

```java
HashMap<String, ColumnAttribute> cols = new HashMap<>();
cols.put("MYTABLE.id",   new ColumnAttribute(true,  DBDataType.DB_TYPE_INTEGER));
cols.put("MYTABLE.name", new ColumnAttribute(false, DBDataType.DB_TYPE_STRING));
HashMap<String, HashMap<String, ColumnAttribute>> tables = new HashMap<>();
tables.put("MYTABLE", cols);
reg.registerTableBindings(tables);
```

### `DBDataType` enum

`DB_TYPE_STRING`, `DB_TYPE_INTEGER`, `DB_TYPE_LONG`, `DB_TYPE_SHORT`,
`DB_TYPE_FLOAT`, `DB_TYPE_DOUBLE`, `DB_TYPE_BOOLEAN`, `DB_TYPE_BIT`,
`DB_TYPE_DATE`, `DB_TYPE_TIME`, `DB_TYPE_DATETIME`, `DB_TYPE_BLOB`

`TypeRegistration.setTypeMatchTable()` maps string tokens from bind files
(e.g., `"varchar"` → `DB_TYPE_STRING`, `"datetime"` → `DB_TYPE_DATETIME`) for each DBMS.

---

## Layer 3 — Statement Creator (`StmtCreator*`)

`StmtCreatorFactory.getStmtCreator(dbmstype)` returns the right dialect subclass.
Each subclass only overrides two methods:

- `markTableName(String)` — wraps a table name in dialect-appropriate quotes.
- `markColumnName(String)` — wraps a column name in dialect-appropriate quotes.

SQLite also overrides `markTableAndColumnNameForUpdate` to omit the table prefix in SET clauses.

`AbstractStmtCreator` contains the shared SQL-building logic:

| Method | Purpose |
|--------|---------|
| `buildStmtForTable(String[], whereStmt, columnMap)` | Multi-table SELECT with explicit column map |
| `buildStmtForTable(String, pkValues)` | Single-table SELECT by primary key (uses `?` placeholders) |
| `buildInsertStmtForTable(String, values)` | INSERT with `?` placeholders for all supplied values |
| `buildUpdateStmtForTable(String, values, whereStmt)` | UPDATE; uses PK columns if whereStmt is null |
| `getCols2Handle()` | Returns ordered list of columns bound to `?` (for the executor) |

---

## Layer 4 — Statement Executor (`AbstractStmtExecuter`)

Constructed with an open `Connection` and the `SupportedDBMSTypes`. It internally creates
a `TypeRegistration` and a `StmtCreatorFactory` — callers do **not** pass those separately
when going through the executor's high-level API.

### High-level API (table-binding–aware)

| Method | Returns | Notes |
|--------|---------|-------|
| `fetchTableValue(String[] tables, String where)` | `List<HashMap<String,Object>>` | All registered columns; requires prior `registerTableBindings` on the `TypeRegistration` inside the executer — **not** directly accessible; use the overload below instead |
| `fetchTableValue(String table, HashMap pkValues)` | `HashMap<String,Object>` | Single row by PK |
| `insertTableValues(String table, HashMap values)` | `int` rows affected | Uses PreparedStatement |
| `updateTableValues(String table, HashMap values, String where)` | `int` rows affected | where=null → PK-based |

### Low-level API (caller supplies full SQL)

| Method | Returns |
|--------|---------|
| `fetchColumnValue(String stmt, List<DBDataType> types)` | `List<List<?>>` ordered by column position |
| `insertValues(String stmt)` | `int` |
| `updateValues(String stmt)` | `int` |

### Type unmarshalling (`processTypeValue`)

`ResultSet` values are cast to the registered `DBDataType`:
- DATE / TIME / DATETIME → `java.util.Date` (via `Timestamp`)
- BLOB → `byte[]`
- STRING → trimmed, never null (empty string if DB null)

### PreparedStatement binding (`setPreparedStatementTypes`)

Binds `?` parameters by Java type inspection (`instanceof` chain):
`String`, `Date`, `Float`, `Double`, `Integer`, `Long`, `Short`, `Boolean`, `Byte`, `byte[]`.
Unknown types throw `SQLException`.

---

## End-to-End Flow

```
1. Build ConnectionDefinition (host/port/user/pwd/db, DBMS type)
2. new DBConnector(def).connectToDatabase()  →  java.sql.Connection
3. new DefaultStmtExecuter(conn, dbmstype)   →  StmtExecInterface

   Inside DefaultStmtExecuter constructor:
     new TypeRegistration(dbmstype)           →  TypeRegistrationInterface
     new StmtCreatorFactory(reg).getStmtCreator(dbmstype) → dialect StmtCreator

4. Register table bindings (if using table-aware fetch/update):
     reg.registerTableBindings("path/to/table.bind")
     -- OR --
     executer.getStmtCreator().registration.registerTableBindings(map)

5. Query:
     executer.fetchTableValue(new String[]{"ORDERS"}, "where status='NEW'")
       → stmtCreator.buildStmtForTable(...)   builds SELECT
       → conn.prepareStatement(sql)
       → rs.next() → processTypeValue()       unmarshals each column
       → List<HashMap<String,Object>>

6. connector.disconnectDatabase(conn)
```

---

## Adding a New DBMS

1. Add a constant to `SupportedDBMSTypes`.
2. Add a `case` in `AbstractDBConnector.connectToDatabase()` for URL construction and driver registration.
3. Create `StmtCreatorXxx extends AbstractStmtCreator` implementing `markTableName` / `markColumnName`.
4. Register it in `StmtCreatorFactory.getStmtCreator()`.
5. If the new DBMS uses non-standard type names, extend `TypeRegistration.setTypeMatchTable()`.

---

## Known Constraints / Gotchas

- `TypeRegistration.registeredTables_` is **static** — shared across all `TypeRegistration` instances in the same JVM. Re-instantiating `TypeRegistration` resets the registry.
- `AbstractStmtExecuter.lastStmt` is also **static** — the last-executed SQL string is shared state.
- `fetchTableValue(String[], where)` looks up table bindings internally via the executer's own `TypeRegistration`, which starts empty. The caller must populate it via `new TypeRegistration(dbmstype)` separately **and** pass the same data, or use the overload that accepts a pre-built `TypeRegistration`. (The test application demonstrates registering through the separate `reg` object and relying on the shared static map.)
- Date values serialised to `PreparedStatement` are converted to a formatted string (`toDateString`), not via `ps.setDate()`. DBMS-specific creators may override `toDateString` for correct dialect formatting.

---

## DBManager Layer (`at.redeye.FrameWork.base.dbmanager`)

The DBManager layer sits **above** `SqlDBInterface` and provides schema lifecycle management:
table creation, versioned migration, and type-safe DML via strongly-typed `DBStrukt` objects.
`SqlDBInterface` handles raw SQL; `DBManager` handles the schema and the object model.

### Package Map

```
FrameWork/base/
├── bindtypes/
│   ├── DBValue.java          # abstract column descriptor (name, type, PK flag, index flag)
│   ├── DBStrukt.java         # table descriptor — holds DBValue objects with version numbers
│   ├── DBString.java         # concrete DBValue for VARCHAR
│   ├── DBInteger.java        # concrete DBValue for INTEGER
│   ├── DBDateTime.java       # concrete DBValue for DATETIME
│   └── ...                   # one subclass per SQL type
│
├── transaction/
│   └── Transaction.java      # abstract bridge: opens Connection + StmtExecInterface,
│                             # registers DBStrukt bindings, provides fetchTable / updateValues
│
└── dbmanager/
    ├── DBManager.java         # interface: createTable, autoCreateTable, migrateTable, …
    ├── DBBindtypeManager.java # interface: register, autocreate, check_table_versions, …
    ├── ShowTables.java        # interface: showTables, db_supports_all_requested_features
    ├── CreateSql.java         # interface (thin): createSqlforTable(strukt, dbmstype)
    └── impl/
        ├── BaseCreateSql.java         # generates DDL strings from DBStrukt
        ├── CreateSqlMySql.java        # MySQL dialect (type mapping + storage engine)
        ├── CreateSqlMariaDB.java      # MariaDB dialect (extends MySQL, utf32 collation)
        ├── CreateSqlMSSql.java        # MSSQL dialect
        ├── CreateSqlOracle.java       # Oracle dialect
        ├── CreateSqlSqlite.java       # SQLite dialect
        ├── CreateSqlDerby.java        # Derby/JavaDB dialect
        ├── ShowTablesMySql.java       # MySQL SHOW TABLES + version detection
        ├── ShowTablesMSSql.java       # MSSQL information_schema query
        ├── ShowTablesOracle.java      # Oracle user_tables query
        ├── ShowTablesSqlite.java      # SQLite sqlite_master query
        ├── ShowTablesDerby.java       # Derby system tables query
        ├── DatabaseManager.java       # implements DBManager + DBBindtypeManager
        └── bindtypes/
            └── DBTableVersion.java    # DBStrukt for the TABLEVERSION tracking table
```

---

### `DBValue` and `DBStrukt` — The Object Model

`DBValue` is the abstract base for a typed column. Concrete subclasses (`DBString`, `DBInteger`,
`DBDateTime`, …) know their `DBDataType`, can load from DB result sets, and carry a title for
UI display. Key flags on `DBValue`:

- `isPrimaryKey()` / `setAsPrimaryKey()` — marks the column as a PK.
- `shouldHaveIndex()` / `setShouldHaveIndex()` — requests a secondary index.

`DBStrukt` is a named collection of `DBValue` objects representing one table.
Columns are added with an integer **version number**:

```java
public void add(DBValue value)                    // version defaults to 1
public void add(DBValue value, Integer version)   // explicitly versioned
```

The strukt's own version is the maximum version of any of its columns.
`getHashMap()` returns all columns; `getHashMapForVersion(v)` returns only columns
introduced **at** version `v` (used during migration).
Foreign key declarations are stored alongside columns:

```java
public void addForeignKey(ForeignKeyDefinition fk)              // version defaults to 1
public void addForeignKey(ForeignKeyDefinition fk, int version) // explicitly versioned
public ArrayList<ForeignKeyDefinition> getForeignKeys()         // all FKs
public ArrayList<ForeignKeyDefinition> getForeignKeysForVersion(int version) // FKs at that version
```
**Typical subclass:**

```java
public class Customer extends DBStrukt {
    public DBString  name    = new DBString("name", 100);
    public DBString  email   = new DBString("email", 200);
    public DBInteger active  = new DBInteger("active");
    public DBInteger group_idx = new DBInteger("group_idx");

    public Customer() {
        super("CUSTOMER");
        name.setAsPrimaryKey();
        add(name,   1);
        add(email,  1);
        add(active, 2);    // added in schema version 2
        add(group_idx, 3); // added in schema version 3

        addForeignKey(new ForeignKeyDefinition("group_idx", "CUSTOMER_GROUP", "idx"), 3);
    }

    @Override public DBStrukt getNewOne() { return new Customer(); }
}
```

---

### `Transaction` — The Bridge

`Transaction` (abstract) combines a `SqlDBInterface` connection + executor with
`DBStrukt`-aware DML:

```
new ConnectionDefinition(...)
    → new DBConnector(def).connectToDatabase()    (SqlDBInterface layer)
    → new DefaultStmtExecuter(conn, dbmstype)
    → new TypeRegistration(dbmstype)
```

Convenience methods on `Transaction` bridge `DBStrukt` ↔ `SqlDBInterface`:

| Method | Description |
|--------|-------------|
| `fetchTable(DBStrukt, whereStmt)` | SELECT all registered columns; returns `Vector<DBStrukt>` |
| `fetchTableWithPrimkey(DBStrukt)` | SELECT single row by PK values already set on the strukt |
| `insertValues(DBStrukt)` | INSERT using current field values |
| `updateValues(DBStrukt)` | UPDATE by PK |
| `updateValues(String sql)` | Raw SQL UPDATE/INSERT/DDL |
| `getSql()` | Returns last executed SQL (for diagnostics) |
| `getDBMSType()` | Returns the active `SupportedDBMSTypes` |
| `markTable(DBStrukt)` | Returns dialect-quoted table name |

`Transaction` also auto-registers all `DBStrukt` bindings into `TypeRegistration`
so that `StmtExecInterface` can marshall result sets back to Java types.

---

### `BaseCreateSql` — DDL Generation

`BaseCreateSql.createSqlforTable(DBStrukt)` produces a multi-statement DDL string:

```
CREATE TABLE <t> (col1 TYPE NOT NULL, col2 TYPE NOT NULL, ...);
ALTER TABLE <t> ADD PRIMARY KEY (pk_col);
ALTER TABLE <t> ADD INDEX IDX_<T>_<COL>(col);
```

Each dialect subclass overrides:
- `createSqlForRow(ColumnAttribute)` → maps `DBDataType` to the DBMS SQL type token.
- `markColumn(String)` → dialect-appropriate identifier quoting.
- `addStorageInfo()` → appended after `CREATE TABLE (...)`, e.g. `ENGINE='InnoDB'`.
- `appendNotNullIfSupportedbyNewRows(ColumnAttribute)` → some DBMS cannot add `NOT NULL` columns via `ALTER TABLE ADD`.

`createSqlForNewRows(DBStrukt, Integer version)` generates `ALTER TABLE ADD` statements
for all columns introduced **at** that version — used during migration.

FK-related methods on `BaseCreateSql`:

| Method | Description |
|--------|-------------|
| `createFKSql(DBStrukt)` | Returns `ALTER TABLE ADD CONSTRAINT` statements for all FKs on the strukt; empty string when none declared |
| `dropFKSql(DBStrukt)` | Returns drop statements for all FKs; delegates per-FK to `dropFKStatement` |
| `dropFKStatement(table, name)` | Single-FK drop; override per dialect (default: ANSI `DROP CONSTRAINT`) |
| `fkActionSql(FKAction)` | `NO_ACTION` → `"NO ACTION"` etc. |

| Dialect subclass | Notable differences |
|---|---|
| `CreateSqlMySql` | Detects MySQL version; uses `TEXT` for large `VARCHAR`; `InnoDB` engine; `dropFKStatement` uses `DROP FOREIGN KEY` |
| `CreateSqlMariaDB` | Extends MySQL; `utf32_bin` collation; inherits `DROP FOREIGN KEY` |
| `CreateSqlMSSql` | `[bracket]` quoting; ANSI `DROP CONSTRAINT` |
| `CreateSqlOracle` | `"double-quote"` quoting; `NUMBER`/`VARCHAR2` types; ANSI `DROP CONSTRAINT` |
| `CreateSqlSqlite` | Backtick quoting; `NOT NULL` omitted in `ALTER TABLE ADD`; `dropFKStatement` returns `""` (SQLite cannot drop named constraints) |
| `CreateSqlDerby` | Derby-specific quoting and types; ANSI `DROP CONSTRAINT` |

---

### `DatabaseManager` — Schema Lifecycle

`DatabaseManager` implements both `DBManager` and `DBBindtypeManager`.
It is initialised with a `Root` and optionally a `Transaction`:

```java
DatabaseManager mgr = new DatabaseManager(root);
mgr.setTransaction(trans);   // picks correct CreateSql* and ShowTables* by DBMS type
```

#### Registration

```java
mgr.register(new Customer());   // adds to internal Vector<DBStrukt>
mgr.register(new Order());
```

#### `autocreate()` — creates or migrates all registered tables

Calls `autoCreateTable(strukt)` for each registered strukt. Returns `false` on first failure.

#### `autoCreateTable(DBStrukt strukt)` — per-table lifecycle

```
tableExists("TABLEVERSION")?
  No  → createTable(DBTableVersion)  → recurse
  Yes →
    tableExists(strukt.getName())?
      No  → createTable(strukt)          → applyForeignKeys(strukt) → setTableVersion(name, version)
      Yes →
        getTableVersion(strukt.getName()) == strukt.getVersion()?
          Yes → nothing to do
          No  → dropAllForeignKeys(strukt)
                  → migrateTable(strukt, currentVersion)
                      → applyForeignKeys(strukt)
                  → setTableVersion(name, version)
```

#### `migrateTable(DBStrukt strukt, Integer fromVersion)` — non-destructive migration

1. **Drop all FK constraints** (`dropAllForeignKeys`) — required so `ALTER TABLE ADD COLUMN` is not blocked by referential checks.
2. Finds a free backup name (e.g. `CUSTOMER_01_01`, `CUSTOMER_01_02`, …).
3. `backupTable(origin, backupName)` — `CREATE TABLE backup AS SELECT * FROM origin`.
4. For each version step from `fromVersion` to `strukt.getVersion()`:
   - `createSqlForNewRows(strukt, i+1)` → `ALTER TABLE ADD` for new columns at that step.
5. Executes the combined SQL.
6. **Re-apply all FK constraints** (`applyForeignKeys`) — restores the complete constraint set.
7. Updates `TABLEVERSION`.

#### `check_table_versions()` — detects out-of-date tables without migrating

Returns `false` if any registered table's DB version differs from the strukt's declared version.
Used at startup to show a warning dialog (`check_table_versions_with_message(permLevel)`).

#### `TABLEVERSION` — the version tracking table

A single special table (`DBTableVersion` strukt) with columns `table` (PK) and `version`.
`getTableVersion(name)` reads from it (result cached in `table_versions` HashMap).
`setTableVersion(name, version)` upserts into it.

---

### Typical Application Startup Flow

```
1. App creates ConnectionDefinition + Transaction (subclass)
2. mgr.setTransaction(trans)
3. mgr.register(new Customer())      // register all app tables
   mgr.register(new Order())
   mgr.register(new OrderLine())
4. mgr.check_table_versions_with_message(permLevel)
     → if out-of-date and user is admin → prompt to run autocreate
5. mgr.autocreate()
     → autoCreateTable per strukt
     → create new tables / migrate existing ones
6. App proceeds; DML via trans.fetchTable() / trans.insertValues() / trans.updateValues()
```

---

---

## Foreign Keys

### Classes

**`FKAction`** (enum) — referential action for `ON DELETE` / `ON UPDATE`:

| Value | SQL | Behaviour |
|-------|-----|-----------|
| `NO_ACTION` | `NO ACTION` | DBMS rejects the operation if it would break the constraint (default) |
| `CASCADE` | `CASCADE` | Automatically delete / update dependent rows |
| `SET_NULL` | `SET NULL` | Set the FK column to NULL (column must be nullable) |
| `RESTRICT` | `RESTRICT` | Like `NO_ACTION` but checked immediately, not deferred |

**`ForeignKeyDefinition`** (immutable value object):

```java
// Minimal — NO_ACTION for both; constraint name auto-generated as FK_<TABLE>_<COLUMN>
new ForeignKeyDefinition("owner_col", "PARENT_TABLE", "parent_col")

// With explicit actions
new ForeignKeyDefinition("owner_col", "PARENT_TABLE", "parent_col",
                         FKAction.CASCADE, FKAction.NO_ACTION)

// With explicit constraint name
new ForeignKeyDefinition("FK_MY_NAME", "owner_col", "PARENT_TABLE", "parent_col",
                         FKAction.NO_ACTION, FKAction.NO_ACTION)
```

If the constraint name is `null`, `DBStrukt.addForeignKey` auto-generates one as
`FK_<OWNINGTABLE>_<OWNERCOLUMN>` (uppercased).

### Declaring FKs in a DBStrukt

**Declare `ForeignKeyDefinition` objects as `public static final` fields** on the strukt
subclass — not as inline `new ForeignKeyDefinition(...)` inside the constructor.

| Concern | inline `new` in constructor | `static final` field |
|---------|-----------------------------|---------------------|
| Allocation | One object per strukt instance | One object per class |
| Auto-name generation | Repeated string concat per instance | Done once; `withName()` copy stored in `foreign_keys_` |
| Use with `fetchChildren` | Must lookup via `getForeignKeys()` at runtime | Pass `MyTable.FK_NAME` directly — type-safe, zero lookup |
| Readability | Anonymous | Self-documenting symbol |

`ForeignKeyDefinition` is **immutable**. When `addForeignKey()` auto-generates a
constraint name, it calls `fk.withName(autoName)` which returns a **new copy** — the
static field is never mutated:

```
OrderLine.FK_ORDER         (name = null)                     ← static field, unchanged forever
    ↓  addForeignKey() calls withName("FK_ORDER_LINE_ORDER_ID")
stored copy                (name = "FK_ORDER_LINE_ORDER_ID") ← used for DDL only
```

The static field with `name = null` is still fully usable for `fetchChildren` and
`fetchParent` because those methods only need `ownerColumn`, `referencedTable`, and
`referencedColumn` — never the constraint name.

**Pattern:**

```java
public class DBOrderLine extends DBStrukt {

    // FK declarations — static final, created once per class
    public static final ForeignKeyDefinition FK_ORDER =
        new ForeignKeyDefinition("order_idx", "ORDERS", "idx");
    public static final ForeignKeyDefinition FK_PRODUCT =
        new ForeignKeyDefinition("product_idx", "PRODUCTS", "idx",
                                 FKAction.RESTRICT, FKAction.NO_ACTION);

    // Column instances — per-instance as usual
    public DBInteger idx        = new DBInteger("idx");
    public DBInteger order_idx  = new DBInteger("order_idx");
    public DBInteger product_idx = new DBInteger("product_idx");
    public DBInteger qty        = new DBInteger("qty");

    public DBOrderLine() {
        super("ORDER_LINE");
        idx.setAsPrimaryKey();
        add(idx,         1);
        add(order_idx,   1);
        add(product_idx, 1);
        add(qty,         1);
        addForeignKey(FK_ORDER,   1);   // reference to static field
        addForeignKey(FK_PRODUCT, 1);
        setVersion(1);
    }

    @Override public DBStrukt getNewOne() { return new DBOrderLine(); }
}
```

### FK-based fetch methods (`Transaction`)

Three convenience methods on `Transaction` use FK metadata to build WHERE clauses without
the caller writing raw SQL strings.

**`ForeignKeyNotFoundException`** (checked) — thrown when the requested FK relationship
cannot be resolved from the strukt's declared FK list (wrong column name or table name).

#### `fetchChildren` — given a parent row, find all children

```java
// High-level: parent strukt provides the filter value automatically
public <T extends DBStrukt> List<T> fetchChildren(
        T childStrukt, DBStrukt parentStrukt, String fkColumnName)
    throws SQLException, ..., ForeignKeyNotFoundException
```

Searches `childStrukt.getForeignKeys()` for a FK where `ownerColumn == fkColumnName`
and `referencedTable == parentStrukt.getName()` (both case-insensitive). Gets the parent's
referenced-column value, builds a `WHERE` clause, delegates to `fetchTable2`.

```java
// Low-level: pass a static FK field and the filter value directly
public <T extends DBStrukt> List<T> fetchChildren(
        T childStrukt, ForeignKeyDefinition fk, Object fkValue)
    throws SQLException, ...
```

Useful when the FK value comes from a UI field or raw result rather than a loaded strukt.

#### `fetchParent` — given a child row, fetch the referenced parent

```java
public <P extends DBStrukt> P fetchParent(
        DBStrukt childStrukt, String fkColumnName, P parentStrukt)
    throws SQLException, ..., ForeignKeyNotFoundException
```

Reads the FK column's current value from `childStrukt`, builds a WHERE clause against the
parent table's referenced column, delegates to `fetchTable2`. Returns `null` if not found.

#### `buildWhereForValue` (private helper)

Produces `WHERE <table>.<col> = <value>`. Numeric DB types are unquoted; all others are
single-quoted. Both column name and value originate from FK declarations and loaded
`DBValue` instances (internal trusted sources) — not from user input.

#### Usage example

```java
// Load a parent order
DBOrder order = new DBOrder();
order.idx.loadFromString("42");
trans.fetchTableWithPrimkey(order);

// Fetch all order lines — no raw WHERE string needed
List<DBOrderLine> lines = trans.fetchChildren(new DBOrderLine(), order, "order_idx");

// Navigate back to the parent
DBOrder parent = trans.fetchParent(lines.get(0), "order_idx", new DBOrder());

// Low-level variant using the static FK field directly
List<DBOrderLine> lines2 = trans.fetchChildren(new DBOrderLine(), DBOrderLine.FK_ORDER, 42);
```

#### Error behaviour

| Situation | Behaviour |
|-----------|----------|
| No FK matching `(fkColumnName, parentTable)` | Throws `ForeignKeyNotFoundException` |
| FK column value is `null` | `WHERE col = 'null'` — no rows returned (safe no-op) |
| Multiple FKs from child to same parent via same column | First declaration wins; log warning |
| Parent `getValue(referencedColumn)` returns `null` | `NullPointerException` — caller must load parent columns before calling |

### DDL lifecycle (DatabaseManager)

`DatabaseManager` calls two private helpers that wrap `BaseCreateSql`:

| Helper | When called | Behaviour on error |
|--------|-------------|--------------------|
| `applyForeignKeys(strukt)` | After `createTable` and after `migrateTable` | Logs a warning, returns `true` (non-fatal) |
| `dropAllForeignKeys(strukt)` | Before `migrateTable` | Logs at DEBUG level, swallows exception (constraint may not exist yet) |

Generated DDL example (MySQL/MariaDB):
```sql
ALTER TABLE `ORDERS` ADD CONSTRAINT `FK_ORDERS_CUSTOMER_IDX`
  FOREIGN KEY (`customer_idx`) REFERENCES `CUSTOMER` (`idx`)
  ON DELETE NO ACTION ON UPDATE NO ACTION;
```

### Dialect differences

| DBMS | DROP syntax |
|------|-------------|
| MySQL / MariaDB | `ALTER TABLE … DROP FOREIGN KEY <name>` |
| MSSQL / Oracle / Derby | `ALTER TABLE … DROP CONSTRAINT <name>` (ANSI) |
| SQLite | No-op — SQLite cannot drop named constraints; FK enforcement requires `PRAGMA foreign_keys = ON` per connection |

### TwelveLittleScoutsClerk FK map

All FKs use `NO_ACTION` (reject violating DML; no automatic cascades).

| Child table | FK column(s) | Parent table |
|---|---|---|
| `MEMBER` | `bp_idx` | `BILLING_PERIOD.idx` |
| `CONTACT` | `bp_idx` | `BILLING_PERIOD.idx` |
| `EVENT` | `bp_idx` | `BILLING_PERIOD.idx` |
| `BILLS` | `bp_idx` | `BILLING_PERIOD.idx` |
| `BOOKINGLINE` | `bp_idx` | `BILLING_PERIOD.idx` |
| `BOOKINGLINE` | `contact_idx` | `CONTACT.idx` |
| `MAIL_JOBS` | `bp_idx` | `BILLING_PERIOD.idx` |
| `MAIL_JOBS` | `bill_idx` | `BILLS.idx` |
| `EVENTMEMBERS` | `bp_idx` | `BILLING_PERIOD.idx` |
| `EVENTMEMBERS` | `event_idx` | `EVENT.idx` |
| `EVENTMEMBERS` | `member_idx` | `MEMBER.idx` |
| `EVENTMEMBERS` | `group_idx` | `GROUP.idx` |
| `EVENTMEMBERS` | `bill_idx` (v4) | `BILLS.idx` |
| `EVENTMEMBERS` | `registration_bill_idx` (v5) | `BILLS.idx` |
| `MEMBERS2GROUPS` | `bp_idx` | `BILLING_PERIOD.idx` |
| `MEMBERS2GROUPS` | `member_idx` | `MEMBER.idx` |
| `MEMBERS2GROUPS` | `group_idx` | `GROUP.idx` |
| `MEMBERS2CONTACTS` | `bp_idx` | `BILLING_PERIOD.idx` |
| `MEMBERS2CONTACTS` | `member_idx` | `MEMBER.idx` |
| `MEMBERS2CONTACTS` | `contact_idx` | `CONTACT.idx` |
| `BOOKINGLINE2EVENTS` | `bp_idx` | `BILLING_PERIOD.idx` |
| `BOOKINGLINE2EVENTS` | `bl_idx` | `BOOKINGLINE.idx` |
| `BOOKINGLINE2EVENTS` | `event_idx` | `EVENT.idx` |
| `BOOKINGLINE2EVENTS` | `member_idx` | `MEMBER.idx` |
| `BOOKINGLINE2EVENTS` | `contact_idx` | `CONTACT.idx` |

### See Also

- [Foreign Key Implementation Plan](./references/foreign-key-plan.md) — original design notes (now implemented).
