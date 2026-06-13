---
name: twelvelittlescoutsclerk-db
description: >
  Domain knowledge for the TwelveLittleScoutsClerk application database schema.
  Use when working with DBStrukt classes, foreign key relationships, database queries,
  or understanding the multi-tenant billing period structure.
---

# TwelveLittleScoutsClerk Database Schema

## Overview

The **TwelveLittleScoutsClerk** application is a scout group management system for financial
and membership tracking. The database uses a **multi-tenant architecture** where all data
is partitioned by `BILLING_PERIOD` — a central container that groups all related records
(tables, members, events, bills, etc.) into isolated datasets.

**Key Design Principle:** Every application table (except `AUDIT`) contains a `bp_idx` 
foreign key referencing `BILLING_PERIOD.idx`. This creates virtual database isolation
without requiring separate database instances.

---

## Architecture Layers

```
TwelveLittleScoutsClerk Application
├── FrameWork (at.redeye.FrameWork)
│   ├── SqlDBInterface/           # Low-level SQL abstraction (see sqldbinterface skill)
│   └── base/
│       ├── bindtypes/           # DBStrukt, DBValue, Condition API
│       ├── dbmanager/           # Schema lifecycle (DatabaseManager)
│       └── transaction/         # Transaction bridge
└── TwelveLittleScoutsClerk (at.redeye.twelvelittlescoutsclerk)
    ├── bindtypes/               # Application DBStrukt classes ← THIS DOCUMENT
    └── dialog_*/                # UI components
```

**Framework Dependencies:**
- Uses `at.redeye.SqlDBInterface` for JDBC connectivity and SQL building
- Uses `at.redeye.FrameWork.base.dbmanager` for schema creation and migration
- Uses `at.redeye.FrameWork.base.bindtypes` for typed column definitions

---

## Package Map

```
TwelveLittleScoutsClerk/bindtypes/
├── DBBillingPeriod.java      # Central tenant container (version 1)
├── DBMember.java             # Scout members (version 2)
├── DBContact.java            # Contact persons (version 2)
├── DBGroup.java              # Scout groups (version 1)
├── DBEvent.java              # Events/activities (version 9)
├── DBBill.java               # Generated bills/invoices (version 6)
├── DBBookingLine.java        # Financial transactions (version 3)
├── DBEventMember.java        # Event participants (version 6)
├── DBMailJob.java            # Email dispatch jobs (version 3)
├── DBMembers2Groups.java     # M2M: members ↔ groups (version 3)
├── DBMembers2Contacts.java   # M2M: members ↔ contacts (version 2)
├── DBBookingLine2Events.java # M2M: booking lines ↔ events (version 3)
├── DBAccountClasses.java     # Accounting categories (version 1)
├── DBBillTemplate.java       # Bill templates (version 2)
└── DBAudit.java              # Audit log (version 1, no bp_idx)
```

---

## Table Reference

### Core Tables

#### BILLING_PERIOD

The **root container** for all application data. Every other table references this.

| Column | Type | PK | Index | Description |
|--------|------|----|-------|-------------|
| `idx` | INTEGER | ✓ | | Primary key |
| `title` | VARCHAR(50) | | | Display name |
| `comment` | VARCHAR(1000) | | | Description |
| `hist` | DBHistory | | | Change history tracking |
| `locked` | DBFlagInteger | | | Read-only flag |

**Version:** 1
**Sequence:** None (manual indexing)

**DBStrukt Class:** `DBBillingPeriod`

```java
// Usage example
DBBillingPeriod bp = new DBBillingPeriod();
bp.title.setValue("Scout Year 2024");
trans.insertValues(bp);
```

---

#### MEMBER

Scout member information.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 2 | Billing period |
| `member_registration_number` | VARCHAR(50) | | | | 1 | Member ID |
| `name` | VARCHAR(50) | | | | 1 | Last name |
| `forname` | VARCHAR(50) | | | | 1 | First name |
| `entry_date` | DATETIME | | | | 1 | Join date |
| `hist` | DBHistory | | | | 1 | Change history |
| `note` | VARCHAR(300) | | | | 1 | Notes |
| `tel` | VARCHAR(50) | | | | 1 | Phone number |
| `inaktiv` | DBFlagInteger | | | | 1 | Inactive flag |
| `de_registered` | DBFlagInteger | | | | 1 | Deregistered flag |
| `group` | VARCHAR(50) | | | | 1 | Group name (denormalized) |

**Version:** 2
**Sequence:** `MEMBERS_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v2)

**DBStrukt Class:** `DBMember`

---

#### CONTACT

Contact person information (parents, guardians, etc.).

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 2 | Billing period |
| `name` | VARCHAR(50) | | | | 1 | Last name |
| `forname` | VARCHAR(50) | | | | 1 | First name |
| `hist` | DBHistory | | | | 1 | Change history |
| `note` | VARCHAR(300) | | | | 1 | Notes |
| `tel` | VARCHAR(50) | | | | 1 | Phone |
| `email` | VARCHAR(200) | | | | 1 | Email address |
| `bank_account_iban` | VARCHAR(50) | | | | 1 | IBAN |
| `bank_account_bic` | VARCHAR(50) | | | | 1 | BIC/SWIFT |

**Version:** 2
**Sequence:** `CONTACT_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v2)

**DBStrukt Class:** `DBContact`

---

#### GROUP

Scout group definition.

| Column | Type | PK | Index | Version | Description |
|--------|------|----|-------|---------|-------------|
| `idx` | INTEGER | ✓ | | 1 | Primary key |
| `name` | VARCHAR(50) | | | 1 | Group name |
| `hist` | DBHistory | | | 1 | Change history |

**Version:** 1
**Sequence:** `GROUP_IDX_SEQUENCE`

**DBStrukt Class:** `DBGroup`

---

#### EVENT

Events and activities.

| Column | Type | PK | FK | Version | Description |
|--------|------|----|----|---------|-------------|
| `idx` | INTEGER | ✓ | | 1 | Primary key |
| `bp_idx` | INTEGER | | BILLING_PERIOD.idx | 7 | Billing period |
| `name` | VARCHAR(50) | | | 1 | Event name |
| `hist` | DBHistory | | | 1 | Change history |
| `costs` | DOUBLE | | | 1 | Cost per person |
| `paid` | DOUBLE | | | 2 | Amount paid |
| `planned_costs` | DOUBLE | | | 3 | Budgeted costs |
| `billing_template` | VARCHAR(512) | | | 4 | ODT template path |
| `registration_template` | VARCHAR(512) | | | 5 | Registration template |
| `registration_costs` | DOUBLE | | | 6 | Registration fee |
| `counts_to_available_cash_amount` | DBFlagInteger | | | 8 | Include in cash flow |
| `account_class_idx` | INTEGER | | | 9 | Accounting class FK |
| `account_class` | VARCHAR(50) | | | 9 | Accounting class name (denormalized) |

**Version:** 9
**Sequence:** `EVENT_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v7)

**DBStrukt Class:** `DBEvent`

---

### Financial Tables

#### BILLS

Generated invoices and registration bills.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 5 | Billing period |
| `billingnr` | VARCHAR(50) | | | | 1 | Invoice number |
| `file_name` | VARCHAR(255) | | | | 1 | Output file name |
| `odt_data` | BLOB | | | | 1 | ODT template data |
| `pdf_data` | BLOB | | | | 1 | Generated PDF |
| `state` | DBEnumAsInteger | | | | 1 | NORMAL/CANCELED |
| `cancel_reason` | VARCHAR(255) | | | 6 | Cancellation reason |
| `direction` | DBEnumAsInteger | | | 2 | OUTGOING/INCOMING |
| `bill_type` | DBEnumAsInteger | | | 3 | INVOICE/REGISTRATION |
| `registration_number` | INTEGER | | | 4 | Registration sequence |
| `hist` | DBHistory | | | | 1 | Change history |

**Enums:**
- `State`: NORMAL, CANCELED
- `Direction`: OUTGOING, INCOMING
- `BillType`: INVOICE, REGISTRATION

**Version:** 6
**Sequences:** `BILL_IDX_SEQ`, `REGISTRATION_IDX_SEQ`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v5)

**DBStrukt Class:** `DBBill`

**Helper Methods:**
```java
boolean isCanceled()    // Returns true if state == CANCELED
boolean isRegistration() // Returns true if bill_type == REGISTRATION
```

---

#### BOOKINGLINE

Financial transaction records (bank statements, cash entries).

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `date` | DATETIME | | | | 1 | Transaction date |
| `hist` | DBHistory | | | | 1 | Change history |
| `bp_idx` | INTEGER | | | BILLING_PERIOD.idx | 2 | Billing period |
| `line` | VARCHAR(500) | | | | 1 | Description line 1 |
| `reference` | VARCHAR(500) | | | | 1 | Reference text |
| `amount` | DOUBLE | | | | 1 | Transaction amount |
| `from_bank_account_iban` | VARCHAR(50) | | | | 1 | Source IBAN |
| `from_bank_account_bic` | VARCHAR(50) | | | | 1 | Source BIC |
| `from_name` | VARCHAR(255) | | | | 1 | Payer/payee name |
| `contact_idx` | INTEGER | | | CONTACT.idx | 2 | Linked contact |
| `assigned` | DBFlagInteger | | | | 1 | Assignment flag |
| `splitpos` | DBFlagInteger | | | | 1 | Split position flag |
| `parent_idx` | INTEGER | | | | 1 | Parent transaction (for splits) |
| `data_source` | VARCHAR(50) | | | | 1 | Source system (e.g., "elba", "cash") |
| `comment` | VARCHAR(50) | | | | 1 | Additional notes |
| `account_class_idx` | INTEGER | | | 3 | Accounting class FK |
| `account_class` | VARCHAR(50) | | | 3 | Accounting class name (denormalized) |

**Version:** 3
**Sequence:** `BL_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v2)
- `FK_CONTACT` → CONTACT.idx (added in v2)

**DBStrukt Class:** `DBBookingLine`

---

#### ACCOUNT_CLASSES

Accounting category classification.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bp_idx` | INTEGER | | | BILLING_PERIOD.idx | 1 | Billing period |
| `category` | DBEnumAsInteger | | | | 1 | INCOME/EXPENSE/LIABILITY |
| `name` | VARCHAR(50) | | | | 1 | Category name |
| `hist` | DBHistory | | | | 1 | Change history |

**Enum Category:** INCOME, EXPENSE, LIABILITY

**Version:** 1
**Sequence:** `ACCLASS_IDX_SEQU`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v1)

**DBStrukt Class:** `DBAccountClasses`

---

#### BILL_TEMPLATES

Template documents for bill generation.

| Column | Type | PK | Index | Version | Description |
|--------|------|----|-------|---------|-------------|
| `idx` | INTEGER | ✓ | | 1 | Primary key |
| `bp_idx` | INTEGER | | ✓ | 1 | Billing period |
| `name` | VARCHAR(255) | | | 1 | Template name |
| `description` | VARCHAR(500) | | | 2 | Description |
| `file_name` | VARCHAR(255) | | | 2 | File path |
| `odt_data` | BLOB | | | 1 | Template data (ODT format) |
| `hist` | DBHistory | | | 1 | Change history |

**Version:** 2
**Sequence:** `BILL_TEMP_IDX_SEQ`

**DBStrukt Class:** `DBBillTemplate`

---

### Junction/Relationship Tables

#### MEMBERS2GROUPS

Many-to-many relationship: members to groups.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `member_idx` | INTEGER | | | MEMBER.idx | 3 | Member |
| `group_idx` | INTEGER | | | GROUP.idx | 3 | Group |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 3 | Billing period |
| `hist` | DBHistory | | | | 1 | Change history |
| `group` | VARCHAR(50) | | | | 2 | Group name (denormalized) |
| `member_name` | VARCHAR(50) | | | | 2 | Member name (denormalized) |

**Version:** 3
**Sequence:** `M2G_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v3)
- `FK_MEMBER` → MEMBER.idx (added in v3)
- `FK_GROUP` → GROUP.idx (added in v3)

**DBStrukt Class:** `DBMembers2Groups`

---

#### MEMBERS2CONTACTS

Many-to-many relationship: members to contacts.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `member_idx` | INTEGER | | | MEMBER.idx | 2 | Member |
| `contact_idx` | INTEGER | | | CONTACT.idx | 2 | Contact |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 2 | Billing period |
| `hist` | DBHistory | | | | 1 | Change history |

**Version:** 2
**Sequence:** `M2C_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v2)
- `FK_MEMBER` → MEMBER.idx (added in v2)
- `FK_CONTACT` → CONTACT.idx (added in v2)

**DBStrukt Class:** `DBMembers2Contacts`

---

#### EVENTMEMBERS

Event participants with financial tracking.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 6 | Billing period |
| `event_idx` | INTEGER | | | EVENT.idx | 6 | Event |
| `member_idx` | INTEGER | | | MEMBER.idx | 6 | Member |
| `group_idx` | INTEGER | | | GROUP.idx | 6 | Group |
| `name` | VARCHAR(50) | | | | 1 | Member name (denormalized) |
| `forname` | VARCHAR(50) | | | | 1 | Member first name (denormalized) |
| `group` | VARCHAR(50) | | | | 1 | Group name (denormalized) |
| `hist` | DBHistory | | | | 1 | Change history |
| `costs` | DOUBLE | | | | 1 | Individual cost (may vary from event cost) |
| `paid` | DOUBLE | | | | 1 | Amount paid |
| `paid_cash` | DOUBLE | | | 2 | Cash payment amount |
| `comment` | VARCHAR(255) | | | | 1 | Notes |
| `bill` | VARCHAR(255) | | | 3 | Bill reference |
| `bill_idx` | INTEGER | | | 4 | FK to BILLS.idx (regular bill) |
| `registration_bill_idx` | INTEGER | | | 5 | FK to BILLS.idx (registration bill) |

**Version:** 6
**Sequence:** `EVENTMEMBER_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v6)
- `FK_EVENT` → EVENT.idx (added in v6)
- `FK_MEMBER` → MEMBER.idx (added in v6)
- `FK_GROUP` → GROUP.idx (added in v6)
- `FK_BILL` → BILLS.idx (added in v6)
- `FK_REGISTRATION_BILL` → BILLS.idx (added in v6)

**DBStrukt Class:** `DBEventMember`

---

#### BOOKINGLINE2EVENTS

Links financial transactions to events and participants.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bl_idx` | INTEGER | | | BOOKINGLINE.idx | 3 | Booking line |
| `event_idx` | INTEGER | | | EVENT.idx | 3 | Event |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 3 | Billing period |
| `member_idx` | INTEGER | | | MEMBER.idx | 3 | Member |
| `contact_idx` | INTEGER | | | CONTACT.idx | 3 | Contact |
| `member_name` | VARCHAR(50) | | | | 2 | Member name (denormalized) |
| `event_name` | VARCHAR(50) | | | | 2 | Event name (denormalized) |
| `hist` | DBHistory | | | | 1 | Change history |

**Version:** 3
**Sequence:** `B2E_IDX_SEQUENCE`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v3)
- `FK_BOOKING_LINE` → BOOKINGLINE.idx (added in v3)
- `FK_EVENT` → EVENT.idx (added in v3)
- `FK_MEMBER` → MEMBER.idx (added in v3)
- `FK_CONTACT` → CONTACT.idx (added in v3)

**DBStrukt Class:** `DBBookingLine2Events`

---

### System Tables

#### MAIL_JOBS

Email dispatch job queue.

| Column | Type | PK | Index | FK | Version | Description |
|--------|------|----|-------|----|---------|-------------|
| `idx` | INTEGER | ✓ | | | 1 | Primary key |
| `bp_idx` | INTEGER | | ✓ | BILLING_PERIOD.idx | 2 | Billing period |
| `bill_idx` | INTEGER | | | BILLS.idx | 2 | Related bill |
| `recipient_email` | VARCHAR(200) | | | | 1 | Destination email |
| `recipient_name` | VARCHAR(200) | | | | 1 | Recipient display name |
| `subject` | VARCHAR(500) | | | | 1 | Email subject |
| `body` | BLOB | | | | 1 | Email body (HTML) |
| `pdf_data` | BLOB | | | | 1 | PDF attachment |
| `state` | DBEnumAsInteger | | | | 1 | PENDING/SENDING/SENT/FAILED |
| `acknowledged` | DBFlagInteger | | | 3 | User acknowledged |
| `retry_count` | INTEGER | | | | 1 | Retry attempts |
| `error_message` | VARCHAR(2000) | | | | 1 | Last error |
| `hist` | DBHistory | | | | 1 | Change history |

**Enum State:** PENDING, SENDING, SENT, FAILED

**Version:** 3
**Sequence:** `MAIL_JOB_IDX_SEQ`
**Foreign Keys:**
- `FK_BILLING_PERIOD` → BILLING_PERIOD.idx (added in v2)
- `FK_BILL` → BILLS.idx (added in v2)

**DBStrukt Class:** `DBMailJob`

**Helper Methods:**
```java
boolean isPending()    // Returns true if state == PENDING
boolean isFailed()     // Returns true if state == FAILED
boolean isAcknowledged() // Returns true if acknowledged flag is set
void setAcknowledged(boolean val) // Sets acknowledged flag
```

---

#### AUDIT

System audit log (the **only table without bp_idx**).

| Column | Type | PK | Index | Description |
|--------|------|----|-------|-------------|
| `idx` | INTEGER | ✓ | | Primary key |
| `audit_idx` | INTEGER | | ✓ | Audit entry sequence |
| `bp_idx` | INTEGER | | ✓ | Billing period (for filtering, not a true FK) |
| `member_idx` | INTEGER | | ✓ | Related member |
| `message` | VARCHAR(3000) | | | Audit message |
| `date` | DATETIME | | ✓ | Timestamp |
| `user` | VARCHAR(50) | | | User who performed action |

**Version:** 1
**Sequence:** `AUDIT_IDX_SEQ`

**DBStrukt Class:** `DBAudit`

---

## Foreign Key Relationships Map

All foreign keys use `NO_ACTION` (reject violating DML; no automatic cascades).

### Hierarchy from BILLING_PERIOD

```
BILLING_PERIOD (bp_idx)
├── MEMBER
├── CONTACT
├── GROUP
├── EVENT
├── BILLS
├── BOOKINGLINE
├── EVENTMEMBERS
├── MAIL_JOBS
├── MEMBERS2GROUPS
├── MEMBERS2CONTACTS
├── BOOKINGLINE2EVENTS
└── ACCOUNT_CLASSES
```

### Complete FK Reference Table

| Child Table | FK Column | Parent Table | Version Added |
|-------------|-----------|--------------|----------------|
| MEMBER | bp_idx | BILLING_PERIOD | 2 |
| CONTACT | bp_idx | BILLING_PERIOD | 2 |
| EVENT | bp_idx | BILLING_PERIOD | 7 |
| BILLS | bp_idx | BILLING_PERIOD | 5 |
| BOOKINGLINE | bp_idx | BILLING_PERIOD | 2 |
| BOOKINGLINE | contact_idx | CONTACT | 2 |
| MAIL_JOBS | bp_idx | BILLING_PERIOD | 2 |
| MAIL_JOBS | bill_idx | BILLS | 2 |
| EVENTMEMBERS | bp_idx | BILLING_PERIOD | 6 |
| EVENTMEMBERS | event_idx | EVENT | 6 |
| EVENTMEMBERS | member_idx | MEMBER | 6 |
| EVENTMEMBERS | group_idx | GROUP | 6 |
| EVENTMEMBERS | bill_idx | BILLS | 6 |
| EVENTMEMBERS | registration_bill_idx | BILLS | 6 |
| MEMBERS2GROUPS | bp_idx | BILLING_PERIOD | 3 |
| MEMBERS2GROUPS | member_idx | MEMBER | 3 |
| MEMBERS2GROUPS | group_idx | GROUP | 3 |
| MEMBERS2CONTACTS | bp_idx | BILLING_PERIOD | 2 |
| MEMBERS2CONTACTS | member_idx | MEMBER | 2 |
| MEMBERS2CONTACTS | contact_idx | CONTACT | 2 |
| BOOKINGLINE2EVENTS | bp_idx | BILLING_PERIOD | 3 |
| BOOKINGLINE2EVENTS | bl_idx | BOOKINGLINE | 3 |
| BOOKINGLINE2EVENTS | event_idx | EVENT | 3 |
| BOOKINGLINE2EVENTS | member_idx | MEMBER | 3 |
| BOOKINGLINE2EVENTS | contact_idx | CONTACT | 3 |
| ACCOUNT_CLASSES | bp_idx | BILLING_PERIOD | 1 |


### FK Declaration Pattern in DBStrukt Classes

Foreign keys are declared as `public static final` fields (not inline in constructor):

```java
public class DBEventMember extends DBStrukt {
    // FK declarations - static final, created once per class
    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_EVENT =
        new ForeignKeyDefinition("event_idx", "EVENT", "idx");
    public static final ForeignKeyDefinition FK_MEMBER =
        new ForeignKeyDefinition("member_idx", "MEMBER", "idx");
    public static final ForeignKeyDefinition FK_GROUP =
        new ForeignKeyDefinition("group_idx", "GROUP", "idx");
    public static final ForeignKeyDefinition FK_BILL =
        new ForeignKeyDefinition("bill_idx", "BILLS", "idx");
    public static final ForeignKeyDefinition FK_REGISTRATION_BILL =
        new ForeignKeyDefinition("registration_bill_idx", "BILLS", "idx");

    public DBEventMember() {
        // ... add columns ...
        addForeignKey(FK_BILLING_PERIOD, 6);
        addForeignKey(FK_EVENT, 6);
        addForeignKey(FK_MEMBER, 6);
        addForeignKey(FK_GROUP, 6);
        addForeignKey(FK_BILL, 6);
        addForeignKey(FK_REGISTRATION_BILL, 6);
    }
}
```

---

## Schema Migration

The application uses **versioned schema migration** via the FrameWork's `DatabaseManager`:

### How Migration Works

1. Each `DBStrukt` declares its current version via `setVersion(N)`
2. Columns added in later versions specify their version: `add(column, version)`
3. The `TABLEVERSION` table tracks each table's current database version
4. On startup, `DatabaseManager.check_table_versions()` compares DB versions with code versions
5. `DatabaseManager.autocreate()` performs migrations automatically

### Migration Process

```
For table T with current DB version V and code version N (V < N):
1. Drop all foreign key constraints on T
2. Create backup table T_backup
3. For each version step from V+1 to N:
   - Execute ALTER TABLE ADD COLUMN for columns added at that version
4. Re-create all foreign key constraints
5. Update TABLEVERSION for T to N
```

### Version History

| Table | Current Version | Notable Changes |
|-------|-----------------|-----------------|
| BILLING_PERIOD | 1 | Initial |
| GROUP | 1 | Initial |
| ACCOUNT_CLASSES | 1 | Initial |
| AUDIT | 1 | Initial |
| MEMBER | 2 | Added bp_idx FK |
| CONTACT | 2 | Added bp_idx FK |
| MEMBERS2CONTACTS | 2 | Added bp_idx FK |
| BILL_TEMPLATES | 2 | Added description, file_name |
| BOOKINGLINE | 3 | Added account_class_idx, account_class |
| MEMBERS2GROUPS | 3 | Added bp_idx FK, group, member_name |
| BOOKINGLINE2EVENTS | 3 | Added member_name, event_name |
| BILL | 6 | Added registration_number, bill_type, direction, state |
| EVENT | 9 | Added costs, paid, planned_costs, templates, registration_costs, account fields |
| EVENTMEMBERS | 6 | Added bill_idx, registration_bill_idx |
| MAIL_JOBS | 3 | Added acknowledged flag |

---

## Typed WHERE Conditions

All tables support the `Condition` API for type-safe, injection-safe queries:

```java
// Using Condition API (recommended)
List<DBMember> activeMembers = trans.fetchTable2(
    new DBMember(),
    Condition.where(DBMember.INAKTIV).eq(0)
        .and(DBMember.DE_REGISTERED).eq(0)
        .and(DBMember.BP_IDX).eq(currentBpIdx)
);

// Using raw SQL string (legacy, still works)
List<DBMember> activeMembers = trans.fetchTable2(
    new DBMember(),
    "WHERE inaktiv = 0 AND de_registered = 0 AND bp_idx = " + currentBpIdx
);

// Fetch with foreign key helper
List<DBEventMember> participants = trans.fetchChildren(
    new DBEventMember(),
    DBEventMember.FK_EVENT,  // static FK field
    eventId                   // FK value
);
```

---

## Sequences

Each primary key uses a database sequence:

| Table | Sequence Name | Purpose |
|-------|---------------|---------|
| MEMBER | MEMBERS_IDX_SEQUENCE | Member IDs |
| CONTACT | CONTACT_IDX_SEQUENCE | Contact IDs |
| GROUP | GROUP_IDX_SEQUENCE | Group IDs |
| EVENT | EVENT_IDX_SEQUENCE | Event IDs |
| BILLS | BILL_IDX_SEQ | Bill IDs |
| BILLS | REGISTRATION_IDX_SEQ | Registration numbers |
| EVENTMEMBERS | EVENTMEMBER_IDX_SEQUENCE | Participant IDs |
| MAIL_JOBS | MAIL_JOB_IDX_SEQ | Mail job IDs |
| MEMBERS2GROUPS | M2G_IDX_SEQUENCE | Member-group link IDs |
| MEMBERS2CONTACTS | M2C_IDX_SEQUENCE | Member-contact link IDs |
| BOOKINGLINE2EVENTS | B2E_IDX_SEQUENCE | Booking-event link IDs |
| BOOKINGLINE | BL_IDX_SEQUENCE | Booking line IDs |
| ACCOUNT_CLASSES | ACCLASS_IDX_SEQU | Account class IDs |
| BILL_TEMPLATES | BILL_TEMP_IDX_SEQ | Template IDs |
| AUDIT | AUDIT_IDX_SEQ | Audit entry IDs |

---

## Common Patterns

### Pattern 1: Multi-Tenant Query

Always filter by `bp_idx`:

```java
public List<DBMember> getMembersForBillingPeriod(int bpIdx) throws SQLException {
    return trans.fetchTable2(
        new DBMember(),
        Condition.where(DBMember.BP_IDX).eq(bpIdx)
    );
}
```

### Pattern 2: Denormalized Fields

Many junction tables include denormalized fields for performance:

```java
// In DBEventMember:
public DBString name = NAME.getCopy();           // Copy of member name
public DBString forname = FORNAME.getCopy();     // Copy of member forname
public DBString group = GROUP.getCopy();         // Copy of group name

// In DBMembers2Groups:
public DBString group = GROUP.getCopy();         // Copy of group name
public DBString member_name = MEMBER_NAME.getCopy(); // Copy of member name

// In DBBookingLine2Events:
public DBString member_name = MEMBER_NAME.getCopy();
public DBString event_name = EVENT_NAME.getCopy();
```

These allow displaying related data without joins.

### Pattern 3: Flag Fields

Boolean flags are implemented as `DBFlagInteger` (0 = false, >0 = true):

```java
public DBFlagInteger INAKTIV = new DBFlagInteger("inactiv","Inactiv");

// Check flag
if (member.inaktiv.getValue() > 0) {
    // Member is inactive
}

// Set flag
member.inaktiv.handler.setValue(1); // true
member.inaktiv.handler.setValue(0); // false
```

### Pattern 4: Enum Fields

Enum-type columns use `DBEnumAsInteger` with custom handlers:

```java
// In DBBill:
public enum State { NORMAL, CANCELED }
public enum Direction { OUTGOING, INCOMING }
public enum BillType { INVOICE, REGISTRATION }

// Column definitions use handlers:
public static final DBEnumAsInteger STATE = 
    new DBEnumAsInteger("state", "State", new StateHandler());

// Usage:
bill.state.handler.setValue(DBBill.State.CANCELED.ordinal());
if (bill.isCanceled()) { /* ... */ }
```

### Pattern 5: DBHistory

All tables have a `hist` field (DBHistory) for change tracking:

```java
public static final DBHistory HIST = new DBHistory("hist");

// In constructor:
hist.setTitle(" "); // Sets display title for history dialog

// Usage: The framework automatically tracks changes
```

---

## Application Startup Flow

```
1. App creates Root with module name
2. AppConfigDefinitions.registerDefinitions()
3. FrameWorkConfigDefinitions.registerDefinitions()
4. Register all DBStrukt classes with root.getBindtypeManager()
5. Load database connection from setup
6. DatabaseManager.check_table_versions_with_message()
   - Compares DB table versions with DBStrukt versions
   - Shows warning if out-of-date
   - Prompts admin to run autocreate
7. DatabaseManager.autocreate()
   - Creates missing tables
   - Migrates existing tables to current version
   - Creates TABLEVERSION tracking table
8. App is ready for use
```

---

## Test Setup

See `SetupTestDB.java` for the test database configuration pattern:

```java
// Register all tables
root.getBindtypeManager().register(new DBBillingPeriod());
root.getBindtypeManager().register(new DBMember());
root.getBindtypeManager().register(new DBContact());
// ... register all other DBStrukt classes ...

// Set transaction
DBBindtypeManager bindtypeManager = root.getBindtypeManager();
bindtypeManager.setTransaction(t);

// Autocreate all tables
bindtypeManager.autocreate();
```

---

## See Also

- [SqlDBInterface Skill](../../FrameWork/.github/skills/database/SKILL.md) - Low-level SQL abstraction
- [Foreign Key Implementation Plan](../references/foreign-key-plan.md) - FK design notes
- [FrameWork DBManager Documentation](../../FrameWork/docs/DBManager.md) - Schema lifecycle
- [Condition API Documentation](../../FrameWork/docs/ConditionAPI.md) - Typed WHERE clauses
