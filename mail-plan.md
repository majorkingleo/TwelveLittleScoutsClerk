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

### 3.1 Mail-Body ODT Template

The mail body is **itself an ODT document** processed with the same
`${…}` placeholder replacement engine used for bills (`BillingHelper.replaceInNode`).
After substitution the plain text is extracted by walking all text nodes —
no LibreOffice conversion needed, just the odfdom DOM.

A ready-to-use starting template is in
`testdata/mail_body_template.odt` (content below after placeholder expansion):

```
Sehr geehrte/r ${contact.forname} ${contact.name},

anbei erhalten Sie die Rechnung für ${member.fullname} betreffend ${event.name}.

Rechnungsnummer: ${billing_number}
Betrag:          ${event_member.costs} €

Bitte überweisen Sie den Betrag auf folgendes Konto:
IBAN: ${org.iban}

Mit freundlichen Grüßen
${org.name}
${org.address_street}, ${org.address_postal_code} ${org.address_city}
```

**Storage** — use the existing BLOB field `odt_data` of `DBBillTemplate`

Add to globalconfig definitions a config value which names the template that is to use for sending mails.

### 3.2 `MailJobHelper.java`

Static helper, similar to `BillingHelper`.

```
createMailJobs(Transaction trans, DBBill bill, DBBillTemplate template,
               DBEvent event, DBEventMember eventMember, DBMember member)
```

Logic:
1. Load mail body ODT:
   - If `template.mail_body_odt_data` is non-empty → load from blob bytes
   - Otherwise → report and error
2. Apply the same `buildReplacementMap()` used in `BillingHelper` plus
   `${billing_number}` = `bill.billingnr`.
3. Fetch all `DBMembers2Contacts` links for `eventMember.member_idx` and collect
   all linked `DBContact` records with a non-blank `email` into a list
   (`allRecipients`).
4. For each recipient, build the `${mail.also_sent_to}` value as the *other*
   recipients in the list:
   - If `allRecipients` has only one entry → `${mail.also_sent_to}` resolves to
     `""` (empty string → the placeholder paragraph is blank and effectively
     invisible).
   - If more than one → value is:
     ```
     Hinweis: Diese Rechnung wurde ebenfalls an folgende Adressen gesendet:
     Max Mustermann <max@example.com>, Erika Muster <erika@example.com>
     ```
     where the list excludes the current recipient.
5. Compute `${mail.payment_note}` based on payment state:
   - `totalPaid = eventMember.paid + eventMember.paid_cash`
   - If `totalPaid >= eventMember.costs` (fully paid):
     ```
     Hinweis: Diese Rechnung ist bereits vollstaendig bezahlt. Es ist keine weitere Ueberweisung erforderlich.
     ```
   - If `totalPaid > 0` but less than `costs` (partially paid):
     ```
     Hinweis: Ein Teilbetrag von ${event_member.paid_total} EUR wurde bereits bezahlt. Offener Restbetrag: ${event_member.costs_remaining} EUR.
     ```
     where `paid_total = totalPaid` and `costs_remaining = costs - totalPaid`.
   - If `totalPaid == 0` → `${mail.payment_note}` resolves to `""` (blank line,
     invisible). The bank transfer paragraph below remains the call to action.
   - When fully paid, the paragraph `"Bitte ueberweisen Sie den Betrag auf
     folgendes Konto: ..."` should be suppressed. This is handled by also
     substituting a dedicated `${mail.transfer_request}` placeholder:
     - If paid in full → `""`
     - Otherwise → `"Bitte ueberweisen Sie den Betrag auf folgendes Konto:"`
6. Add `${mail.payment_note}`, `${mail.transfer_request}`, and
   `${mail.also_sent_to}` to the replacement map before step 7.
7. Extract plain text from the processed DOM by collecting all text-node values
   (same recursive walk as `replaceInNode`, collecting instead of replacing).
7. Build subject line:
   ```
   Rechnung ${billing_number} – ${org.name}
   ```
8. For each linked `DBContact` with a non-blank `email`, create a `DBMailJob`:
   - `bill_idx` = `bill.idx`
   - `recipient_email` / `recipient_name` from contact
   - `subject` from step 4
   - `body` = extracted plain text from step 3
   - `pdf_data` = `bill.pdf_data.value`
   - `state` = PENDING, `retry_count` = 0
7. Insert each job via `DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey`.

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

### 3.3 `MailWorker.java`

See section 3 original description — this class is unchanged by the ODT-body
approach.

### 3.4 Config resolution helper (inside `MailWorker` or `MailJobHelper`)

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

After the bill `trans.commit()` add:

```java
// Enqueue mail jobs for all member contacts
MailJobHelper.createMailJobs(trans, bill, template, event, event_member, member);
trans.commit(); // second commit for the mail jobs
```

The `member` object is already fetched inside `generateBillFromTemplate`; pass
it as an additional parameter or re-fetch it here.

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

1. `pom.xml` — add angus-mail dependency  ✅ **DONE**
2. `AppConfigDefinitions.java` — add mail config entries  ✅ **DONE**
   - Added: `MailBodyTemplateName`, `MailSmtpHost`, `MailSmtpPort`, `MailSmtpStartTls`, `MailFrom`, `MailFromName` (global); `MailSmtpUser`, `MailSmtpPassword` (local)
3. `DBBillTemplate.java` — no extra BLOB field; mail body ODT comes from a separate template looked up by `MailBodyTemplateName`  ✅ **DONE** (version stays at 2)
4. `DBMailJob.java` — new bindtype, version 1  ✅ **DONE**
5. `Main.java` — register `DBMailJob` with `BindtypeManager`  ✅ **DONE**
6. `MailJobHelper.java` — `createMailJobs()` implemented  ✅ **DONE**
   - Loads ODT from the `DBBillTemplate` named by `MailBodyTemplateName`
   - Applies replacement map + mail-specific placeholders
   - Inserts `DBMailJob` rows (state PENDING) for each contact with e-mail
7. `MailWorker.java` — polling loop + `sendMail()`  ✅ **DONE**
8. `EditEvent.java` — "Send Mail" button added  ✅ **DONE**
   - Separate from "Create Bill"; calls `MailJobHelper.createMailJobs()`
   - Button disabled until a row is selected, `MailBodyTemplateName` is configured, and the member has at least one contact with an e-mail
9. `Main.java` — start `MailWorker` daemon thread  ✅ **DONE**
10. `testdata/mail_body_template.odt`  ✅ **DONE** (already existed)
11. *(optional)* `MailJobs.java` + `ViewMailJob.java` — list dialog + menu entry  ✅ **DONE**
