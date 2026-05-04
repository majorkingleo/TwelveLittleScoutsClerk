# BillingHelper — ODT Bill Generation Plan

## Summary

Create `BillingHelper.java` in `at.redeye.twelvelittlescoutsclerk` that generates a bill ODT (or PDF)
from an ODT template whose path is stored in `DBEvent.billing_template`.
Uses `odfdom-java` for safe XML-level text-node replacement of `${...}` placeholders.
PDF conversion is optional via LibreOffice CLI.
Config values are read via `GlobalConfigDefinitions.get(key).getConfigValue()`.

---

## Implementation Steps

### Phase 1 — Add dependency

Add `org.apache.odftoolkit:odfdom-java:0.10.0` to the `<dependencies>` section of `pom.xml`.

### Phase 2 — Implement BillingHelper

Create `src/at/redeye/twelvelittlescoutsclerk/BillingHelper.java`.

**Main method signature:**

```java
public static File generateBill(Transaction trans, DBEvent event,
    DBEventMember eventMember, boolean convertToPdf) throws ...
```

**Logic inside the method:**

1. Fetch `DBMember` via `trans.fetchTableWithPrimkey(member)` using `eventMember.member_idx`.
2. Fetch the first `DBContact` linked to the member via `DBMembers2Contacts` → `DBContact`.
   If none exists, contact placeholders are replaced with empty strings.
3. Build a `Map<String, String>` replacement map from all sources (see table below).
4. Load the ODT template from the path in `event.billing_template.getValue()`.
5. Open with `OdfTextDocument.loadDocument(templateFile)`.
6. Walk **all text nodes** in `doc.getContentDom()` via DOM traversal;
   for each text node apply all `${...}` replacements.
7. Save the filled ODT to `File.createTempFile("bill_<member_idx>_", ".odt")`.
8. If `convertToPdf == true`: invoke
   `libreoffice --headless --convert-to pdf --outdir <tmpDir> <odtFile>`
   via `ProcessBuilder`, then return the generated `.pdf` file.
9. Store the output file path in `eventMember.bill.loadFromString(outputFile.getAbsolutePath())`.
   The caller is responsible for persisting via `trans.updateValues(eventMember)`.
10. Return the output `File`.

### Phase 3 — Wire up from UI

Call `BillingHelper.generateBill(...)` from `jBCreateBillActionPerformed` in
`src/at/redeye/twelvelittlescoutsclerk/dialog_event/EditEvent.java`
after `DBEventMember event_member = values.get(row)`.

### Phase 4 — Update the template

Open `testdata/billing_template.odt` in LibreOffice and insert the placeholder strings
from the table below wherever the corresponding values should appear.

---

## Placeholder Mapping

| Placeholder                    | Source           | Java expression                                                               |
|-------------------------------|------------------|-------------------------------------------------------------------------------|
| `${member.name}`              | DBMember         | `member.name.getValue()`                                                      |
| `${member.forname}`           | DBMember         | `member.forname.getValue()`                                                   |
| `${member.registration_number}` | DBMember       | `member.member_registration_number.getValue()`                                |
| `${member.group}`             | DBMember         | `member.group.getValue()`                                                     |
| `${contact.name}`             | DBContact        | `contact.name.getValue()`                                                     |
| `${contact.forname}`          | DBContact        | `contact.forname.getValue()`                                                  |
| `${contact.email}`            | DBContact        | `contact.email.getValue()`                                                    |
| `${contact.tel}`              | DBContact        | `contact.tel.getValue()`                                                      |
| `${event.name}`               | DBEvent          | `event.name.getValue()`                                                       |
| `${event.costs}`              | DBEvent          | `event.costs.getValue()`                                                      |
| `${event.planned_costs}`      | DBEvent          | `event.planned_costs.getValue()`                                              |
| `${event_member.costs}`       | DBEventMember    | `eventMember.costs.getValue()`                                                |
| `${event_member.paid}`        | DBEventMember    | `eventMember.paid.getValue()`                                                 |
| `${event_member.paid_cash}`   | DBEventMember    | `eventMember.paid_cash.getValue()`                                            |
| `${event_member.comment}`     | DBEventMember    | `eventMember.comment.getValue()`                                              |
| `${org.name}`                 | AppConfigDefinitions | `GlobalConfigDefinitions.get("Organisation").getConfigValue()`            |
| `${org.address_street}`       | AppConfigDefinitions | `GlobalConfigDefinitions.get("OrganisationAddressStreet").getConfigValue()` |
| `${org.address_postal_code}`  | AppConfigDefinitions | `GlobalConfigDefinitions.get("OrganisationAddressPostalCode").getConfigValue()` |
| `${org.address_city}`         | AppConfigDefinitions | `GlobalConfigDefinitions.get("OrganisationAddressCity").getConfigValue()` |
| `${org.iban}`                 | AppConfigDefinitions | `GlobalConfigDefinitions.get("OrganisaiontIBAN").getConfigValue()`        |

---

## Relevant Files

| File | Purpose |
|------|---------|
| `pom.xml` | Add `odfdom-java` dependency |
| `src/at/redeye/twelvelittlescoutsclerk/BillingHelper.java` | New helper class (to be created) |
| `src/at/redeye/twelvelittlescoutsclerk/dialog_event/EditEvent.java` | Wire up `jBCreateBillActionPerformed` |
| `src/at/redeye/twelvelittlescoutsclerk/EventHelper.java` | Reference for static helper pattern |
| `src/at/redeye/twelvelittlescoutsclerk/bindtypes/DBEventMember.java` | `bill` field stores output path |
| `src/at/redeye/twelvelittlescoutsclerk/bindtypes/DBContact.java` | Contact fields |
| `src/at/redeye/twelvelittlescoutsclerk/bindtypes/DBMembers2Contacts.java` | Member→Contact link |
| `src/at/redeye/twelvelittlescoutsclerk/AppConfigDefinitions.java` | Config key names |
| `testdata/billing_template.odt` | Template to update with placeholder strings |

---

## Verification

1. `mvn compile` — must pass after adding `odfdom-java`.
2. Trigger the "Create Bill" button in `EditEvent`; inspect the returned `File`.
3. Open the generated ODT in LibreOffice — verify all placeholders were replaced.
4. If PDF output: open the PDF and verify content.

---

## Key Decisions

- **Library**: `org.apache.odftoolkit:odfdom-java:0.10.0` (handles split text-node edge cases).
- **Config access**: `GlobalConfigDefinitions.get(key).getConfigValue()`.
- **Output location**: `File.createTempFile(...)` in the system temp directory; path returned to caller.
- **PDF**: optional `boolean convertToPdf` parameter; uses `libreoffice --headless`.
- **Persistence**: caller calls `trans.updateValues(eventMember)` after `generateBill` to save `bill` path.
- **Missing contact**: contact placeholders silently replaced with empty string.
