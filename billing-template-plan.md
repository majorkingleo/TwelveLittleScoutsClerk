# Plan: Billing Template Dialog

## Goal

A fully NetBeans-compatible billing template CRUD dialog, modeled on `dialog_member`.
The **Bearbeiten** subdialog lets the user upload a file (stored as a BLOB in the DB) and download it back.

---

## Files to Create

| # | File | Purpose |
|---|------|---------|
| 1 | `src/at/redeye/twelvelittlescoutsclerk/bindtypes/DBBillingTemplate.java` | DB bindtype: `idx`, `name`, `description`, `file_name`, `mime_type`, `file_data` (DBBlob), `hist` |
| 2 | `src/at/redeye/twelvelittlescoutsclerk/dialog_billingtemplate/BillingTemplate.java` | List dialog extending `BaseDialog` + `NewSequenceValueInterface` |
| 3 | `src/at/redeye/twelvelittlescoutsclerk/dialog_billingtemplate/BillingTemplate.form` | Paired NetBeans .form (`JFrameFormInfo`) |
| 4 | `src/at/redeye/twelvelittlescoutsclerk/dialog_billingtemplate/EditBillingTemplate.java` | Edit subdialog — name/desc fields + Upload + Download buttons |
| 5 | `src/at/redeye/twelvelittlescoutsclerk/dialog_billingtemplate/EditBillingTemplate.form` | Paired NetBeans .form (`JDialogFormInfo`) |
| 6 | `src/at/redeye/twelvelittlescoutsclerk/dialog_billingtemplate/CreateBillingTemplate.java` | Create dialog (name + description + upload) |
| 7 | `src/at/redeye/twelvelittlescoutsclerk/dialog_billingtemplate/CreateBillingTemplate.form` | Paired .form |

## Files to Modify

| # | File | Change |
|---|------|--------|
| 8 | `src/at/redeye/twelvelittlescoutsclerk/MainWin.java` | Variable decl, menu assembly, action handler (GEN markers), variable in GEN:variables, import |
| 9 | `src/at/redeye/twelvelittlescoutsclerk/MainWin.form` | `<MenuItem>` for `jMenuItemBillingTemplates` inside `jMenu5` |

---

## Key Design Decisions

- **No `bp_idx`** — billing templates are global (not per billing period), so no `checkAz()` guard in the menu handler
- **BLOB storage** — file bytes in `DBBlob file_data`; `file_name` (255) and `mime_type` (100) stored so download can suggest the original filename
- **Upload** — `JFileChooser` → `Files.readAllBytes(path)` → load into `DBBlob`
- **Download** — write `file_data` bytes to user-chosen path via save `JFileChooser`; button disabled when no file is stored yet
- **Hidden columns** — `file_data`, `file_name`, `mime_type` hidden in the list table (binary/internal fields only useful in the subdialog)
- **Double-click** on table row opens Bearbeiten (same as Member dialog)

---

## DBBillingTemplate Fields

| Field | Type | Max | Notes |
|-------|------|-----|-------|
| `idx` | `DBInteger` | — | Primary key |
| `name` | `DBString` | 200 | Shown in list |
| `description` | `DBString` | 500 | Optional |
| `file_name` | `DBString` | 255 | Original filename for download |
| `mime_type` | `DBString` | 100 | e.g. `application/pdf` |
| `file_data` | `DBBlob` | — | Actual file bytes |
| `hist` | `DBHistory` | — | Audit trail |

---

## EditBillingTemplate Subdialog Layout

```
[ Name         ] [ _________________________ ]
[ Beschreibung ] [ _________________________ ]
[ Datei        ] [ current-file-name.pdf     ]
                 [ Hochladen ] [ Herunterladen ]

                              [ Speichern ] [ Schließen ]
```

---

## Verification Steps

1. `mvn compile` — must succeed with no errors
2. Run app → **Daten** menu → "Billing Templates" entry appears
3. Create a new template, upload a file, save → row appears in list
4. Select row → **Bearbeiten** → download the file → bytes match original
5. Delete a row → row removed from list

---

## Status

- [ ] `DBBillingTemplate.java`
- [ ] `BillingTemplate.java` + `BillingTemplate.form`
- [ ] `EditBillingTemplate.java` + `EditBillingTemplate.form`
- [ ] `CreateBillingTemplate.java` + `CreateBillingTemplate.form`
- [ ] `MainWin.java` wiring
- [ ] `MainWin.form` wiring
- [ ] `mvn compile` passes
