#!/usr/bin/env python3
"""Regression tests for booking-line anonymization behavior."""

from __future__ import annotations

import random
import unittest

from anonymize_booking_lines_csv import anonymize_text_cell


class ReferenceNameExtractionTest(unittest.TestCase):
    def test_reference_prefers_person_name_after_event_text(self) -> None:
        text = "Zahlungsreferenz: LK Thxalot Oberzalek Martin IBAN Empfänger:"
        mapping: dict[str, dict[str, str]] = {}

        anonymized = anonymize_text_cell(
            text=text,
            mapping=mapping,
            used_names=set(),
            used_ibans=set(),
            used_bics=set(),
            used_banks=set(),
            rng=random.Random(7),
        )

        self.assertIn("LK Thxalot", anonymized)
        self.assertNotIn("Oberzalek Martin", anonymized)
        self.assertIn("Oberzalek Martin", mapping.get("booking_name_map", {}))

    def test_reference_prefers_already_mapped_swapped_name(self) -> None:
        text = "Zahlungsreferenz: Martin Oberzalek Waylon Vulcan IBAN Auftraggeber:"
        mapping: dict[str, dict[str, str]] = {
            "booking_name_map": {
                "Oberzalek Martin": "JeanLuc Picard",
            }
        }

        anonymized = anonymize_text_cell(
            text=text,
            mapping=mapping,
            used_names=set(mapping["booking_name_map"].values()),
            used_ibans=set(),
            used_bics=set(),
            used_banks=set(),
            rng=random.Random(9),
        )

        self.assertIn("JeanLuc Picard", anonymized)
        self.assertNotIn("Martin Oberzalek", anonymized)

    def test_reference_prefers_rightmost_known_name(self) -> None:
        text = "Zahlungsreferenz: Lando Rios Oberzalek Martin IBAN Empfänger:"
        mapping: dict[str, dict[str, str]] = {
            "booking_name_map": {
                "Lando Rios": "Tom Paris",
                "Oberzalek Martin": "JeanLuc Picard",
            }
        }

        anonymized = anonymize_text_cell(
            text=text,
            mapping=mapping,
            used_names=set(mapping["booking_name_map"].values()),
            used_ibans=set(),
            used_bics=set(),
            used_banks=set(),
            rng=random.Random(11),
        )

        self.assertIn("Tom Paris", anonymized)
        self.assertIn("JeanLuc Picard", anonymized)
        self.assertNotIn("Lando Rios", anonymized)
        self.assertNotIn("Oberzalek Martin", anonymized)

    def test_second_pass_replaces_known_name_variants(self) -> None:
        text = "Info: Oberzalek Martin und Martin Oberzalek sind gemeldet."
        mapping: dict[str, dict[str, str]] = {
            "booking_name_map": {
                "Oberzalek Martin": "JeanLuc Picard",
            }
        }

        anonymized = anonymize_text_cell(
            text=text,
            mapping=mapping,
            used_names=set(mapping["booking_name_map"].values()),
            used_ibans=set(),
            used_bics=set(),
            used_banks=set(),
            rng=random.Random(13),
        )

        self.assertNotIn("Oberzalek Martin", anonymized)
        self.assertNotIn("Martin Oberzalek", anonymized)
        self.assertIn("JeanLuc Picard", anonymized)


if __name__ == "__main__":
    unittest.main()
