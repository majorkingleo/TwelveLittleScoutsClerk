---
name: ods-documents
description: "Create LibreOffice/OpenDocument spreadsheet (ODS) documents in TwelveLittleScoutsClerk using odfdom. Use when asked to: create an ODS export, add an 'Export ODS' button, generate a LibreOffice/Calc document, build a spreadsheet report (monthly columns, sums), add a second sheet, fix odfdom export problems, or make an exported report look like a printed ledger."
argument-hint: "Report name (e.g. IncomeExpense) or what to export"
---

# Creating ODS (LibreOffice Calc) Documents

All spreadsheet exports in this project are **ODS** files written with **odfdom 0.10.0**
(`org.odftoolkit:odfdom-java`, already in `TwelveLittleScoutsClerk/pom.xml`). The
established pattern is a report dialog with an HTML preview plus an **Export ODS**
button (see `reports/cashflow/ReportCashFlow.java`,
`reports/incomeexpense/ReportIncomeExpense.java`,
`reports/eventsettlement/ReportEventSettlement.java`).

Ready-to-copy helper class: [OdsExportHelper.java](./references/OdsExportHelper.java)

---

## Step 1 — Dialog skeleton

The export always happens from a `BaseDialog` (report dialog):

- Data collection in a `*Renderer extends BaseReportRenderer` with getters for the
  export (rows, month labels, totals, balances, booking lines).
- `collectData()` and `render()` are called once more inside the export
  `AutoMBox`, then the ODS is written from the renderer's getters.
- File chooser defaults to a sensible name, extension `.ods` is appended if missing,
  `mainwin.setLastOpenPath(targetFile.getParent())` is called afterwards.

```java
new AutoMBox("Export ODS") {
    @Override
    public void do_stuff() throws Exception {
        setWaitCursor();
        MyRenderer renderer = createRenderer();
        renderer.render();              // fills the data structures
        writeOds(targetFile, renderer);
        setWaitCursor(false);
    }
};
```

---

## Step 2 — Create the document

```java
OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.newSpreadsheetDocument();
OdfTable table = spreadsheet.getSpreadsheetTables().get(0);
table.setTableName("Einnahmen-Ausgaben");
// IMPORTANT: the default sheet is created with table:print="false"
table.getOdfElement().setTablePrintAttribute(true);
```

`newSpreadsheetDocument()` always contains **one sheet with exactly one empty row and
one column**. Grow it:

```java
if (table.getColumnCount() < columnCount) {
    table.appendColumns(columnCount - table.getColumnCount());
}
```

After `appendColumns(...)`, every `appendRow()` already has `columnCount` cells.

### NEVER remove the existing row

```java
// WRONG - infinite loop: odfdom never removes the LAST row of a sheet
while (table.getRowCount() > 0) {
    table.removeRowsByIndex(0, 1);
}
```

Reuse the existing empty row for the first line instead:

```java
OdfTableRow firstRow = table.getRowCount() > 0 ? table.getRowByIndex(0) : table.appendRow();
```

All further lines use `table.appendRow()`. Use an empty `appendRow()` as a spacer
row between blocks.

---

## Step 3 — Write cells

Always address cells through the row (`row.getCellByIndex(col)`, 0-based).

```java
// text
OdfTableCell cell = row.getCellByIndex(0);
cell.setStringValue("Ausgaben 2024");

// money - numeric and formatted (never write formatted strings)
cell.setDoubleValue(159.58);
cell.setFormatString("€ #,##0.00");
if (value < 0.0) {
    cell.getOdfElement().setProperty(OdfTextProperties.Color, "#c00000");   // negatives in red
}
```

Do **not** call `setCurrencyFormat(...)`: it throws `IllegalArgumentException` in
odfdom 0.10.0. `setFormatString(...)` is the supported way and also produces the
`number:number-style` in the document.

### Bold / background

```java
cell.getOdfElement().setProperty(OdfTextProperties.FontWeight, "bold");
cell.getOdfElement().setProperty(OdfTableCellProperties.BackgroundColor, "#8ea9db");
```

All property calls on the same cell accumulate into **one automatic cell style**, so
number format, bold, font colour and background can be combined freely. Do not build
number styles or cell styles manually with `OdfOfficeStyles` — it is unnecessary and
error prone (`newStyleStyleElement(a, b)` takes (family, name), not (name, family)).

### Colour scheme (project standard)

Background colours are used **only for section header rows**. Everything else is
plain and distinguished by bold text only.

| Element | Property | Value |
|---------|----------|-------|
| Section header rows (Ausgaben / Einnahmen / …) | background | `#8ea9db` (medium blue), bold |
| Total rows | — | bold only, no fill |
| Balance line | — | bold only, no fill |
| Negative amounts | font color | `#c00000` (dark red) |
| Zeitraum / Startkontostand | — | bold only |
| Second sheet (e.g. Buchungszeilen) | — | no fills at all, bold header text |

Keep it that simple: one fill colour, one accent colour for negatives.

### Blank instead of zero

Ledger-style reports leave zero values empty (like the printed sheet). Skip a cell when
the value is `0.0` **and** no bold/background is needed.

---

## Step 4 — Second sheet

```java
OdfTable detail = OdfTable.newTable(spreadsheet);
detail.setTableName("Buchungszeilen");
if (detail.getColumnCount() < 4) {
    detail.appendColumns(4 - detail.getColumnCount());
}
OdfTableRow header = detail.appendRow();
```

`newTable(...)` creates a table whose first row is already usable; keep it or reuse it
with `getRowByIndex(0)`.

---

## Step 5 — Column widths and save

LibreOffice does **not** recalculate `style:use-optimal-column-width` on load — the
columns stay at the default width and long labels get truncated. Set explicit widths
as style properties:

```java
String[] widths = new String[columnCount];
widths[0] = "5.0cm";                        // label column
for (int i = 1; i < columnCount - 1; i++) {
    widths[i] = "2.3cm";                    // one per month
}
widths[columnCount - 1] = "2.8cm";          // sum column

for (int i = 0; i < table.getColumnCount() && i < widths.length; i++) {
    table.getColumnByIndex(i).getOdfElement()
         .setProperty(OdfTableColumnProperties.ColumnWidth, widths[i]);
}
```

Never use `col.setWidth(...)`: odfdom 0.10.0 throws `NumberFormatException` on locales
with comma decimals.

```java
try (FileOutputStream fos = new FileOutputStream(targetFile)) {
    spreadsheet.save(fos);
}
```

---

## Layout conventions (ledger look)

A report that resembles the printed yearly ledger uses:

| Row | Content |
|-----|---------|
| 0 | `Zeitraum: <from> - <till>` (bold, no fill) |
| 1 | `Startkontostand per <date> : Eur <amount>` (bold, no fill) |
| 2 | spacer |
| 3 | section header: `Ausgaben <year>` + month labels + `Summe`, background `#8ea9db` |
| … | one row per account class: `1. Name`, one money cell per month, total |
| … | total row `Summe` (bold, no fill) |
| … | spacer, `Einnahmen <year>` (month labels only in the first section) |
| … | spacer, `Kontostand per <till> : Eur <amount>` (bold, no fill) |

Month labels: `Jan Feb März April Mai Juni Juli Aug. Sept Okt Nov Dez`, with a
2-digit year suffix (`Jan 24`) when the range spans multiple years. Cap the number of
month columns (e.g. 60) so a bad date range cannot produce a gigantic sheet.

Money in text lines (balances) uses `String.format(Locale.GERMANY, "%,.2f", v)`.

---

## Step 6 — Verify the result

1. `mvn clean compile` from the combined root (incremental `mvn compile` can fail if
   the Java language server wrote Java-25 class files into `FrameWork/target/classes`).
2. Export the file in the running app, then check it with LibreOffice — **always with a
   private profile and a timeout**, otherwise LibreOffice may hang and spin CPU:

```bash
timeout 60 soffice --headless -env:UserInstallation=file:///tmp/lo_profile \
    --convert-to csv --outdir /tmp/ods_check /path/to/file.ods
```

If the conversion succeeds, the document is valid; inspect the CSV for row/column
layout and totals.

---

## Checklist

- [ ] Export runs inside `AutoMBox`, uses the renderer getters (no duplicated queries)
- [ ] `appendColumns(...)` before the first `appendRow()`; first line reuses row 0
- [ ] `setTablePrintAttribute(true)` on the first sheet
- [ ] No `removeRowsByIndex(...)` loop
- [ ] Money via `setDoubleValue(...)` + `setFormatString("€ #,##0.00")`, negatives red
- [ ] Zero values left blank (unless a total row needs bold formatting)
- [ ] Background only on section header rows; detail sheets without fills
- [ ] Bold/background via `getOdfElement().setProperty(...)`
- [ ] Explicit column widths via `ColumnWidth` style property
- [ ] `.ods` extension enforced, last open path updated
- [ ] `mvn clean compile` succeeds and no `data/` folder is touched
- [ ] AI modifications marked with start/end comments

---

## Anti-patterns

| Don't | Why |
|-------|-----|
| `while (getRowCount() > 0) removeRowsByIndex(0, 1)` | never terminates — last row cannot be removed |
| `cell.setCurrencyFormat(symbol, pattern)` | `IllegalArgumentException` in odfdom 0.10.0 |
| `setDoubleValue` with formatted strings (`"€ 159,58"`) | value becomes text, no calculation possible |
| `col.setWidth(...)` | `NumberFormatException` on comma-decimal locales |
| `setUseOptimalWidth(true)` only | LibreOffice does not recalculate it on load → labels truncate |
| leaving `table:print="false"` on the first sheet | sheet is missing in PDF/print output |
| manual `OdfOfficeStyles` number/cell styles | `setProperty` already builds automatic styles |

---

## Reference

- [OdsExportHelper.java](./references/OdsExportHelper.java) — ready-to-copy helper methods
- Existing implementations:
  `reports/cashflow/ReportCashFlow.java`,
  `reports/incomeexpense/ReportIncomeExpense.java`,
  `reports/eventsettlement/ReportEventSettlement.java`
