---
name: message-translation
description: "How to translate and manage message box texts in TwelveLittleScoutsClerk dialogs. Use when asked to: translate German UI messages to English, add message box text, refactor JOptionPane strings, initialize dialog messages, convert hardcoded strings to MESSAGE_ fields, apply MlM() to message texts."
argument-hint: "Dialog class name (e.g. EditEvent)"
---

# Translating Message Box Texts in TwelveLittleScoutsClerk

All user-visible message strings in a dialog class must **not** be hardcoded inline. Instead, they are declared as `private String` fields, initialized in an `initMessages()` method using the `MlM()` localisation helper, and referenced by name at every use site.

---

## Pattern

### 1. Declare fields

Add a `private String MESSAGE_<NAME>;` field for each distinct message at the top of the class (alongside other field declarations).

```java
private String MESSAGE_NO_BILLING_TEMPLATE;
private String MESSAGE_TEMPLATE_HAS_NO_FILE;
private String MESSAGE_BILL_CREATED;
```

### 2. Initialize in `initMessages()`

Create (or extend) a `private void initMessages()` method. Initialize every field with `MlM("English text")`. The method must be called from the constructor, **after** `initComponents()`.

```java
private void initMessages() {
    MESSAGE_NO_BILLING_TEMPLATE = MlM("No billing template found with name: %s");
    MESSAGE_TEMPLATE_HAS_NO_FILE = MlM("Billing template has no file: %s");
    MESSAGE_BILL_CREATED         = MlM("Bill created and saved.");
}
```

Constructor call order:
```java
public EditFoo(...) {
    super(...);
    initComponents();
    initMessages();   // ← must come after initComponents
    // ...
}
```

### 3. Use fields at call sites

Replace every inline string passed to `JOptionPane.showMessageDialog` (or `setText`, status bars, etc.) with the corresponding field.

```java
// Before
JOptionPane.showMessageDialog(null, "Rechnung erstellt und gespeichert.");

// After
JOptionPane.showMessageDialog(null, MESSAGE_BILL_CREATED);
```

For parameterised messages use `String.format`:
```java
// Before
JOptionPane.showMessageDialog(null, "Keine Vorlage: " + templateName);

// After
JOptionPane.showMessageDialog(null, String.format(MESSAGE_NO_BILLING_TEMPLATE, templateName));
```

---

## Rules

- **All** user-visible strings go through `MlM()` — never pass a raw string literal to `JOptionPane` or a status-bar label.
- Field names follow `MESSAGE_<SCREAMING_SNAKE_CASE>` convention.
- German source strings are translated to English when added to `initMessages()`.
- `initMessages()` is the **only** place where the English text appears; call sites reference only the field.
- If a message is shared by multiple methods, declare it as one field — do not duplicate.
- Status-bar format strings (e.g. `"Total paid: %1$.2f, …"`) follow the same pattern.

---

## Translation Reference (common German → English)

| German | English |
|--------|---------|
| Schließen | Close |
| Speichern | Save |
| Löschen | Delete |
| Bearbeiten | Edit |
| Neu | New |
| Anzeigen | View |
| Rechnungen | Bills |
| Rechnung | Bill |
| Rechnungsvorlage | Billing template |
| Abrechnungsvorlagen | Billing Templates |
| Dateiname / Datei | File Name / File |
| Beschreibung | Description |
| Vorlage hochladen | Upload Template |
| Vorlage speichern | Save Template |
| Datum | Date |
| Benutzer | User |
| Empfänger | Recipient |
| Betreff | Subject |
| Fehler | Error |
| Notiz | Note |
| Vorname | First Name |
| Telefonnummer | Phone Number |
| Für dieses Mitglied wurde noch keine Rechnung erstellt. | No bill has been created for this member yet. |
| Rechnung erstellt und gespeichert. | Bill created and saved. |
| Mail-Job(s) erstellt und in die Warteschlange eingereiht. | Mail job(s) created and queued. |
| Keine Rechnungsvorlage gefunden mit dem Namen: %s | No billing template found with name: %s |
| Rechnungsvorlage hat keine Datei: %s | Billing template has no file: %s |
