# TwelveLittleScoutsClerk

A Java-based membership and financial management application for scout organizations.

## What it does

TwelveLittleScoutsClerk helps scout groups manage their members, events, and finances. It covers the full workflow from tracking member registrations through organizing events, generating invoices as PDF/ODT documents, and dispatching those invoices by email.

### Features

**Member management**
- Register and track scout members with registration numbers, entry dates, and group assignments
- Link members to contacts (parents, guardians) with email addresses
- Track member status (active / inactive / de-registered)

**Event management**
- Create events with per-participant costs and planned budgets
- Assign members to events and link billing templates
- Support for multiple billing periods (fiscal years)

**Billing**
- Generate invoices from customizable ODT templates
- Substitute placeholders (`${member_name}`, `${billing_number}`, `${iban}`, …) at generation time
- Convert ODT bills to PDF via LibreOffice
- EPC QR code / Girocode embedded in bills for SEPA payment
- Track payment state (paid in full, partial payment, outstanding)

**Email dispatch**
- Automatically create mail jobs when a bill is generated
- Customizable ODT-based email body templates with the same placeholder system
- Background dispatcher sends queued jobs over SMTP (STARTTLS on port 587)
- Tracks per-job state: `PENDING → SENDING → SENT / FAILED`

**Booking lines**
- Record incoming and outgoing financial transactions
- Link bookings to events or billing periods
- Import bank CSV exports (ELBA / Raiffeisen format)

**Contact management**
- Separate contact database, many-to-many link with members

**Audit log**
- Immutable audit trail for changes to key records

## Technology stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Build | Maven 3 |
| GUI | Swing (NetBeans GUI Designer `.form` files) |
| Database | Apache Derby (embedded, default) · MySQL · MariaDB |
| Document generation | ODF Toolkit 0.10.0 (ODT) · Apache POI 5.3.0 (Excel) |
| QR codes | ZXing 3.5.3 |
| Email | Jakarta Mail / Angus Mail 2.0.3 |
| Framework | Custom `at.redeye.FrameWork` (database abstraction, UI base classes, i18n) |

## Building

```bash
# Compile
mvn clean compile

# Build executable JAR (skips tests)
mvn package -DskipTests
```

The packaged JAR lands in `target/TwelveLittleScoutsClerk-*.jar`; runtime dependencies are copied to `target/lib/`.

## Running

```bash
java -jar target/TwelveLittleScoutsClerk-*.jar
```

On first launch the application creates an embedded Derby database in the user's home directory and migrates the schema automatically.

## Configuration

All settings are stored in the database and edited through the application's settings dialog. Key parameters:

| Setting | Description |
|---|---|
| `Organisation` | Name of the scout organisation (appears on invoices) |
| `Street`, `PostalCode`, `City` | Organisation address |
| `IBAN` | Bank account for SEPA payments / EPC QR code |
| `SmtpHost`, `SmtpPort` | Outgoing mail server (default port 587, STARTTLS) |
| `SmtpUser`, `SmtpPassword` | SMTP credentials |
| `MailFrom`, `MailFromName` | Sender address and display name |
| `MailBodyTemplateName` | Name of the DBBillTemplate used as the email body |
| `OpenCommand` | Command to open files (e.g. a PDF viewer); `%s` is replaced with the file path. If empty, `java.awt.Desktop.open()` is used. |

## License

See [LICENSE](LICENSE).
