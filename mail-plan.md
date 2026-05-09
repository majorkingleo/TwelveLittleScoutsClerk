# Mail-Plan: Asynchronous Bill Dispatch via E-Mail

## Overview

When a bill is generated for an event member, mail jobs are created for every
contact e-mail address linked to that member.  A background worker thread polls
the database for pending jobs and dispatches them via a configured SMTP server.
Global settings provide defaults; local (per-machine) settings overrule them —
following the existing `add()` / `addLocal()` pattern in `AppConfigDefinitions`.

---

## 1  Mail-Server Configuration

Add the following entries to `AppConfigDefinitions`:

| Field                  | Config key            | Scope  | Default          | Description                |
|------------------------|-----------------------|--------|------------------|----------------------------|
| SMTP host              | `MailSmtpHost`        | global | `localhost`      | Hostname / IP              |
| SMTP port              | `MailSmtpPort`        | global | `587`            | Port (587 = STARTTLS)      |
| STARTTLS               | `MailSmtpStartTls`    | global | `true`           | `"true"` / `"false"`       |
| Sender address         | `MailFrom`            | global | `""`             | From address               |
| Sender display name    | `MailFromName`        | global | `""`             | From display name          |
| SMTP username          | `MailSmtpUser`        | **local** | `""`          | Per-machine login          |
| SMTP password          | `MailSmtpPassword`    | **local** | `""`          | Per-machine password       |

`addLocal()` items live in the local config table and are never written to the
global one, so credentials do not accidentally propagate to the shared DB.

---

## 2  New Database Table: `MAIL_JOBS`

### `DBMailJob` (`bindtypes/DBMailJob.java`)

```
MAIL_JOBS
├── idx               INTEGER  PK   (sequence MAIL_JOB_IDX_SEQ)
├── bp_idx            INTEGER        billing-period scope
├── bill_idx          INTEGER        FK → BILLS.idx (0 = not linked)
├── recipient_email   STRING 200
├── recipient_name    STRING 200
├── subject           STRING 500
├── body              BLOB           plain-text or HTML mail body
├── pdf_data          BLOB           the bill PDF to attach
├── state             DBEnumAsInteger  PENDING(0) / SENDING(1) / SENT(2) / FAILED(3)
├── retry_count       INTEGER        how many send attempts were made
├── error_message     STRING 2000    last error detail if FAILED
└── hist              DBHistory
```

Version: 1 (initial).

### State transition

```
PENDING → SENDING → SENT
                  → FAILED  (retry_count < MAX_RETRIES → back to PENDING after delay)
```

`MAX_RETRIES = 3` — after three failures the job stays FAILED and requires manual
intervention.

---

## 3  New Classes

### 3.1 `MailJobHelper.java`

Static helper, similar to `BillingHelper`.

```
createMailJobs(Transaction trans, DBBill bill, int memberIdx, int bpIdx,
               String subject, String body)
```

Logic:
1. Fetch all `DBMembers2Contacts` links for `memberIdx`.
2. For each link, fetch `DBContact`.
3. If `contact.email` is non-blank, create a `DBMailJob` with:
   - `bill_idx` = `bill.idx`
   - `recipient_email` / `recipient_name` from contact
   - `subject` / `body` passed in
   - `pdf_data` = `bill.pdf_data.value`
   - `state` = PENDING, `retry_count` = 0
4. Insert each job via `trans.insertValues(job)` (or
   `DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey`).

Subject template (configurable or hard-coded initially):

```
Rechnung ${bill.file_name} – ${org.name}
```

Body template (plain text, can reference same `${…}` placeholders as ODT
templates):

```
Sehr geehrte/r ${contact.forname} ${contact.name},

anbei übermitteln wir Ihnen die Rechnung ${bill.file_name}.

Mit freundlichen Grüßen
${org.name}
```

### 3.2 `MailWorker.java`

A `Runnable` started as a daemon thread from `Main.java` after the application
is fully initialised.

Pseudo-code:

```java
while (!Thread.currentThread().isInterrupted()) {
    try {
        List<DBMailJob> pending = fetchPendingJobs(trans);  // state=PENDING
        for (DBMailJob job : pending) {
            markSending(trans, job);
            try {
                sendMail(job);
                markSent(trans, job);
            } catch (Exception ex) {
                job.retry_count++;
                if (job.retry_count >= MAX_RETRIES) {
                    markFailed(trans, job, ex.getMessage());
                } else {
                    markPending(trans, job, ex.getMessage()); // retry later
                }
            }
        }
        Thread.sleep(POLL_INTERVAL_MS); // e.g. 30 000
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

`sendMail()` uses **Jakarta Mail** (`jakarta.mail` / `angus-mail`) to connect
to the configured SMTP server, builds a `MimeMessage` with:
- `From` = `MailFromName <MailFrom>`
- `To` = `recipient_email`
- `Subject` = `subject`
- Body part: `text/plain` (or `text/html`)
- Attachment: `application/pdf` with filename `${bill.file_name}.pdf`

### 3.3 Config resolution helper (inside `MailWorker` or `MailJobHelper`)

```java
private static String cfg(DBConfig global, DBConfig local) {
    String loc = local.getConfigValue();
    return (loc != null && !loc.isBlank()) ? loc : global.getConfigValue();
}
```

Since `addLocal()` values already shadow global ones at load time in the
framework, this may be handled automatically.  Verify against the framework
behaviour; add explicit fallback only if needed.

---

## 4  Maven Dependency

Add to `pom.xml` (before `<scope>test</scope>` section):

```xml
<!-- Jakarta Mail (Angus implementation) -->
<dependency>
    <groupId>org.eclipse.angus</groupId>
    <artifactId>angus-mail</artifactId>
    <version>2.0.3</version>
</dependency>
```

`angus-mail` bundles `jakarta.mail-api` so no separate API artifact is needed.

---

## 5  Wiring: `EditEvent.jBCreateBillActionPerformed`

After `trans.commit()` (bill inserted) add:

```java
// Enqueue mail jobs for all member contacts
String subject = "Rechnung " + billName + " – "
        + AppConfigDefinitions.Organisation.getConfigValue();
String body = buildMailBody(event_member, event, billName);
MailJobHelper.createMailJobs(trans, bill, event_member.member_idx.getValue(),
        mainwin.getBPIdx(), subject, body);
trans.commit(); // second commit for the mail jobs
```

`buildMailBody()` is a simple private helper that returns the plain-text
template with substitutions applied.

---

## 6  Background Thread Startup (`Main.java`)

After `root.show()`:

```java
MailWorker worker = new MailWorker(root.getTransaction());
Thread mailThread = new Thread(worker, "MailWorker");
mailThread.setDaemon(true);
mailThread.start();
```

Using a daemon thread means the JVM exits cleanly without explicitly stopping
the thread.

---

## 7  Optional: Mail-Jobs Dialog

A simple read-only list dialog `MailJobs` / `ViewMailJob` (following the
`BaseDialog` / `BaseDialogDialog` pattern from the netbeans-dialog skill) that
shows all `MAIL_JOBS` ordered by state and creation date, with columns:

| Datum | Empfänger | Betreff | Status | Versuche | Fehler |

A "Retry" button re-sets FAILED jobs back to PENDING.

---

## 8  Implementation Order

1. `pom.xml` — add angus-mail dependency
2. `AppConfigDefinitions.java` — add 7 mail config entries
3. `DBMailJob.java` — new bindtype, version 1
4. `Main.java` — register `DBMailJob` with `BindtypeManager`
5. `MailJobHelper.java` — `createMailJobs()` + `buildMailBody()`
6. `MailWorker.java` — polling loop + `sendMail()`
7. `EditEvent.java` — call `MailJobHelper.createMailJobs()` after bill insert
8. `Main.java` — start daemon thread
9. *(optional)* `MailJobs.java` + `ViewMailJob.java` — list dialog + menu entry
