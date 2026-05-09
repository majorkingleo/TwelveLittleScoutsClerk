# EPC QR Code Format (v2.10)

Source: [EPC guideline](https://www.europeanpaymentscouncil.eu/document-library/guidance-documents/quick-response-code-guidelines-enable-data-capture-initiation), [Wikipedia](https://en.wikipedia.org/wiki/EPC_QR_code)

## Payload Structure

Each line is separated by `\n` (LF). The payload is encoded in UTF-8 (or the declared character set).

| Line | Field | Value / Rules |
|------|-------|--------------|
| 1 | Service Tag | Always `BCD` |
| 2 | Version | `001` or `002` |
| 3 | Character set | `1` = UTF-8, `2` = ISO 8859-1, … `8` = ISO 8859-7 |
| 4 | Identification | Always `SCT` (SEPA Credit Transfer) |
| 5 | BIC | BIC of beneficiary's bank (8 or 11 chars); **required in v001**, optional/blank in v002 |
| 6 | Beneficiary Name | Max 70 characters |
| 7 | Beneficiary IBAN | IBAN without spaces, e.g. `AT483200000012345864` |
| 8 | Amount | `EUR` followed by decimal amount, e.g. `EUR12.34`; `EUR0` or blank = unspecified |
| 9 | Purpose code | 4-char ISO 20022 purpose code (e.g. `CHAR` for charity); may be blank |
| 10 | Structured reference | ISO 11649 or Belgian structured reference; blank if using unstructured |
| 11 | Unstructured remittance | Free text, max 140 chars; blank if using structured reference |
| 12 | Beneficiary to originator info | Max 70 chars; optional |

### Example payload (v002, no BIC, no structured ref)

```
BCD
002
1
SCT

Red Cross of Belgium
BE72000000001616
EUR1.00
CHAR

Urgency fund
Sample EPC QR code
```

## Encoding rules

- **Encoding**: UTF-8 if character set = `1`
- **Error correction**: Level **M** (required by EPC spec)
- **Max size**: 331 bytes in UTF-8
- **QR version**: auto-selected by library based on payload length
- IBAN: strip all spaces and hyphens before encoding
- Amount: use exactly 2 decimal places; never use comma as decimal separator
- Trailing blank lines at the end of the payload are allowed and safe

## Version differences

| Feature | v001 | v002 |
|---------|------|------|
| BIC | Mandatory | Optional (leave blank) |
| Used in | Older AT/BE implementations | Current standard (Germany, NL, FI) |

## Validation constraints

- Beneficiary name ≤ 70 chars
- IBAN: valid IBAN format (check digit passes), no spaces
- Amount: 0.01 – 999999999.99 EUR (or 0 / blank for "any amount")
- Remittance (unstructured): ≤ 140 chars
- Total payload: ≤ 331 bytes UTF-8

## Common purpose codes

| Code | Meaning |
|------|---------|
| `CHAR` | Charitable payment |
| `GDDS` | Purchase of goods |
| `SCVE` | Purchase of services |
| `BEXP` | Business expenses |
| *(blank)* | Not specified |

In most club/scout invoicing scenarios, leave purpose code blank or use `CHAR`.
