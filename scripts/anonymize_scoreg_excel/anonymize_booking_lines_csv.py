#!/usr/bin/env python3
"""Anonymize personal and banking data in ELBA-like booking line CSV files.

Features:
- Reuses existing anonymization mapping JSON from the Excel anonymizer.
- Uses existing mapped names (first/last/full name) when available.
- Falls back to a themed 400-name pool (Babylon 5, Star Wars, Simpsons, Harry Potter).
- Anonymizes IBAN/account numbers, BICs, and bank names.
- Keeps account replacement stable per person across multiple transfers.
- Persists all generated mappings for repeatable anonymization.
"""

from __future__ import annotations

import argparse
import csv
import random
import re
from pathlib import Path
from typing import Dict, List, Optional, Set

from anonymization_common import (
    EMAIL_REGEX,
    SHARED_FULL_NAME_POOL_400,
    ensure_map,
    load_mapping_dict,
    normalize_spaces,
    random_digits,
    random_like_segment,
    save_mapping_dict,
)


IBAN_REGEX = re.compile(r"\b[A-Z]{2}\d{2}[A-Z0-9]{11,30}\b")
BIC_REGEX = re.compile(r"\b[A-Z]{6}[A-Z0-9]{2}(?:[A-Z0-9]{3})?\b")
ACCOUNT_LABEL_REGEX = re.compile(
    r"(?i)\b(iban|konto|kontonummer)\b([^:;\n]{0,40}):\s*([A-Z0-9]{8,34})\b"
)

# Labels commonly found in booking text segments.
PERSON_LABEL_REGEX = re.compile(
    r"(?i)\b(auftraggeber|beguenstigter|empfaenger|zahlungspflichtiger|kontoinhaber)\s*:\s*"
    r"([^;]+?)(?=\s+(?:zahlungsreferenz|verwendungszweck|iban|bic|auftraggeber|beguenstigter|empfaenger|"
    r"zahlungspflichtiger|kontoinhaber)\s*:|$)"
)

REFERENCE_LABEL_REGEX = re.compile(
    r"(?i)\b(zahlungsreferenz|verwendungszweck)\s*:\s*([^;]+?)(?=\s+(?:iban|bic|auftraggeber|beguenstigter|"
    r"empfaenger|zahlungspflichtiger|kontoinhaber)\s*:|$)"
)

BANK_FIELD_REGEX = re.compile(r"(?i)\b(bank(?:name)?|institut|kreditinstitut)\s*:\s*([^;]+)")

KNOWN_BANK_NAMES = [
    "Erste Bank",
    "Sparkasse",
    "Raiffeisen",
    "UniCredit",
    "Bank Austria",
    "Volksbank",
    "BAWAG",
    "PSK",
    "Oberbank",
    "Hypo",
    "ING",
    "N26",
    "Revolut",
]

FALLBACK_BANK_NAMES = [
    "Corellia Credit Union",
    "Hogsmeade Savings Bank",
    "Springfield Community Bank",
    "Babylon Trade Bank",
    "Tatooine Mutual",
    "Centauri Trust",
    "Minbari Cooperative",
    "Moe Financial Services",
    "Diagon Alley Capital",
    "Naboo Reserve",
    "Rivendell Ledger House",
    "Quahog Deposit Works",
]


THEMED_NAME_POOL = SHARED_FULL_NAME_POOL_400


def is_likely_person_name(value: str) -> bool:
    normalized = normalize_spaces(value)
    if not normalized:
        return False
    if any(ch.isdigit() for ch in normalized):
        return False

    parts = normalized.split(" ")
    if len(parts) < 2 or len(parts) > 4:
        return False

    for part in parts:
        cleaned = part.replace("-", "").replace("'", "")
        if len(cleaned) < 2 or not cleaned.isalpha():
            return False
    return True


def anonymize_iban_like(original: str, rng: random.Random) -> str:
    # Keep country code and checksum shape, randomize payload.
    country = original[:2]
    checksum = original[2:4]
    payload = original[4:]
    anon_payload = random_like_segment(payload, rng)
    anon_checksum = random_digits(len(checksum), rng)
    return f"{country}{anon_checksum}{anon_payload}"


def anonymize_bic_like(original: str, rng: random.Random) -> str:
    return random_like_segment(original, rng)


def anonymize_name(
    name: str,
    mapping: Dict[str, Dict[str, str]],
    used_names: Set[str],
    rng: random.Random,
) -> str:
    booking_name_map = ensure_map(mapping, "booking_name_map")
    full_name_map = ensure_map(mapping, "full_name_map")
    first_name_map = ensure_map(mapping, "first_name_map")
    last_name_map = ensure_map(mapping, "last_name_map")

    normalized = normalize_spaces(name)
    if not normalized:
        return name

    if normalized in booking_name_map:
        return booking_name_map[normalized]

    if normalized in full_name_map:
        anon = full_name_map[normalized]
        booking_name_map[normalized] = anon
        used_names.add(anon)
        return anon

    parts = normalized.split(" ")
    if len(parts) >= 2:
        first = parts[0]
        last = parts[-1]
        mapped_first = first_name_map.get(first)
        mapped_last = last_name_map.get(last)
        if mapped_first and mapped_last:
            anon = f"{mapped_first} {mapped_last}"
            if anon not in used_names:
                booking_name_map[normalized] = anon
                used_names.add(anon)
                return anon

    candidates = [candidate for candidate in THEMED_NAME_POOL if candidate not in used_names]
    if candidates:
        anon = rng.choice(candidates)
    else:
        anon = f"{rng.choice(THEMED_NAME_POOL)} {rng.randint(100, 999)}"

    booking_name_map[normalized] = anon
    used_names.add(anon)
    return anon


def anonymize_bank_name(
    original: str,
    mapping: Dict[str, Dict[str, str]],
    used_banks: Set[str],
    rng: random.Random,
) -> str:
    bank_map = ensure_map(mapping, "bank_map")
    key = normalize_spaces(original)
    if not key:
        return original
    if key in bank_map:
        return bank_map[key]

    candidates = [b for b in FALLBACK_BANK_NAMES if b not in used_banks]
    candidate = rng.choice(candidates) if candidates else f"{rng.choice(FALLBACK_BANK_NAMES)} {rng.randint(10, 99)}"

    bank_map[key] = candidate
    used_banks.add(candidate)
    return candidate


def anonymize_iban(
    original: str,
    person: Optional[str],
    mapping: Dict[str, Dict[str, str]],
    used_ibans: Set[str],
    rng: random.Random,
) -> str:
    iban_map = ensure_map(mapping, "iban_map")
    person_account_map = ensure_map(mapping, "person_account_map")

    if original in iban_map:
        return iban_map[original]

    if person:
        person_key = normalize_spaces(person)
        existing = person_account_map.get(person_key)
        if existing:
            iban_map[original] = existing
            return existing

    candidate = anonymize_iban_like(original, rng)
    while candidate in used_ibans:
        candidate = anonymize_iban_like(original, rng)

    iban_map[original] = candidate
    used_ibans.add(candidate)

    if person:
        person_account_map[normalize_spaces(person)] = candidate

    return candidate


def anonymize_bic(
    original: str,
    mapping: Dict[str, Dict[str, str]],
    used_bics: Set[str],
    rng: random.Random,
) -> str:
    bic_map = ensure_map(mapping, "bic_map")
    if original in bic_map:
        return bic_map[original]

    candidate = anonymize_bic_like(original, rng)
    while candidate in used_bics:
        candidate = anonymize_bic_like(original, rng)

    bic_map[original] = candidate
    used_bics.add(candidate)
    return candidate


def extract_names_from_reference(text: str) -> List[str]:
    names: List[str] = []
    for _, content in REFERENCE_LABEL_REGEX.findall(text):
        # Capture first likely "Firstname Lastname" pair.
        m = re.search(r"\b([A-Z][A-Za-z\-']+)\s+([A-Z][A-Za-z\-']+)\b", content)
        if m:
            candidate = f"{m.group(1)} {m.group(2)}"
            if is_likely_person_name(candidate):
                names.append(candidate)
    return names


def extract_labeled_names(text: str) -> List[str]:
    names: List[str] = []
    for _, content in PERSON_LABEL_REGEX.findall(text):
        cleaned = normalize_spaces(content)
        cleaned = re.sub(r"\b(iban|bic)\b.*$", "", cleaned, flags=re.IGNORECASE).strip()
        if cleaned and is_likely_person_name(cleaned):
            names.append(cleaned)
    return names


def replace_name(text: str, source_name: str, target_name: str) -> str:
    pattern = re.compile(re.escape(source_name))
    return pattern.sub(target_name, text)


def anonymize_text_cell(
    text: str,
    mapping: Dict[str, Dict[str, str]],
    used_names: Set[str],
    used_ibans: Set[str],
    used_bics: Set[str],
    used_banks: Set[str],
    rng: random.Random,
) -> str:
    value = text

    all_names: List[str] = []
    all_names.extend(extract_labeled_names(value))
    all_names.extend(extract_names_from_reference(value))

    primary_person = all_names[0] if all_names else None

    def iban_repl(match: re.Match[str]) -> str:
        return anonymize_iban(match.group(0), primary_person, mapping, used_ibans, rng)

    value = IBAN_REGEX.sub(iban_repl, value)

    def labeled_account_repl(match: re.Match[str]) -> str:
        label = match.group(1)
        qualifier = match.group(2)
        account = match.group(3)
        anonymized = anonymize_iban(account, primary_person, mapping, used_ibans, rng)
        return f"{label}{qualifier}: {anonymized}"

    value = ACCOUNT_LABEL_REGEX.sub(labeled_account_repl, value)

    def bic_repl(match: re.Match[str]) -> str:
        return anonymize_bic(match.group(0), mapping, used_bics, rng)

    value = BIC_REGEX.sub(bic_repl, value)

    for original_name in all_names:
        anonymized_name = anonymize_name(original_name, mapping, used_names, rng)
        value = replace_name(value, original_name, anonymized_name)

    def bank_repl(match: re.Match[str]) -> str:
        label = match.group(1)
        bank_name = normalize_spaces(match.group(2))
        anon = anonymize_bank_name(bank_name, mapping, used_banks, rng)
        return f"{label}: {anon}"

    value = BANK_FIELD_REGEX.sub(bank_repl, value)

    # Also replace known bank names in free text.
    for known in KNOWN_BANK_NAMES:
        known_pattern = re.compile(r"(?<![A-Za-z0-9])" + re.escape(known) + r"(?![A-Za-z0-9])", re.IGNORECASE)
        if known_pattern.search(value):
            anon = anonymize_bank_name(known, mapping, used_banks, rng)
            value = known_pattern.sub(anon, value)

    # Minimal email anonymization for completeness.
    email_map = ensure_map(mapping, "email_map")
    used_emails = set(email_map.values())

    def email_repl(match: re.Match[str]) -> str:
        original = match.group(0)
        if original in email_map:
            return email_map[original]
        domain = original.split("@", 1)[1] if "@" in original else "example.org"
        candidate = f"anon{rng.randint(100000, 999999)}@{domain}"
        while candidate in used_emails:
            candidate = f"anon{rng.randint(100000, 999999)}@{domain}"
        email_map[original] = candidate
        used_emails.add(candidate)
        return candidate

    value = EMAIL_REGEX.sub(email_repl, value)
    return value


def anonymize_booking_csv(input_file: Path, output_file: Path, mapping_file: Path, seed: int) -> None:
    rng = random.Random(seed)
    mapping = load_mapping_dict(mapping_file)

    booking_name_map = ensure_map(mapping, "booking_name_map")
    bank_map = ensure_map(mapping, "bank_map")
    iban_map = ensure_map(mapping, "iban_map")
    bic_map = ensure_map(mapping, "bic_map")

    used_names = set(booking_name_map.values())
    used_names.update(ensure_map(mapping, "full_name_map").values())
    used_ibans = set(iban_map.values())
    used_bics = set(bic_map.values())
    used_banks = set(bank_map.values())

    output_file.parent.mkdir(parents=True, exist_ok=True)

    with input_file.open("r", encoding="utf-8", newline="") as src, output_file.open(
        "w", encoding="utf-8", newline=""
    ) as dst:
        reader = csv.reader(src, delimiter=";", quotechar='"')
        writer = csv.writer(dst, delimiter=";", quotechar='"', quoting=csv.QUOTE_MINIMAL)

        for row in reader:
            out_row: List[str] = []
            for cell in row:
                if not cell:
                    out_row.append(cell)
                    continue

                anonymized = anonymize_text_cell(
                    cell,
                    mapping,
                    used_names,
                    used_ibans,
                    used_bics,
                    used_banks,
                    rng,
                )
                out_row.append(anonymized)

            writer.writerow(out_row)

    save_mapping_dict(mapping_file, mapping)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Anonymize names, account identifiers (IBAN/BIC), and bank names in ELBA-like booking CSV files, "
            "while keeping person-account mappings stable across transfers."
        )
    )
    parser.add_argument("input", type=Path, help="Input CSV file")
    parser.add_argument("output", type=Path, help="Output anonymized CSV file")
    parser.add_argument(
        "--mapping",
        type=Path,
        default=Path("testdata/anonymization_mapping.json"),
        help="JSON mapping file shared with the Excel anonymizer",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=20260423,
        help="Random seed for reproducible new mappings",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    anonymize_booking_csv(args.input, args.output, args.mapping, args.seed)
    print(f"Anonymized CSV written to: {args.output}")
    print(f"Mapping file written to: {args.mapping}")


if __name__ == "__main__":
    main()
