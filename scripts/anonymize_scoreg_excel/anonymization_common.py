#!/usr/bin/env python3
"""Shared data structures and helpers for anonymizer scripts."""

from __future__ import annotations

import json
import random
import re
import string
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, cast


ID_REGEX = re.compile(r"\b\d+-[A-Z0-9]{3,8}-[A-Z0-9]{4,16}\b")
EMAIL_REGEX = re.compile(r"\b[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}\b")
NAME_SPLIT_REGEX = re.compile(r"\s+")


@dataclass
class MappingStore:
    first_name_map: Dict[str, str]
    last_name_map: Dict[str, str]
    full_name_map: Dict[str, str]
    street_map: Dict[str, str]
    email_map: Dict[str, str]
    phone_map: Dict[str, str]
    postal_code_map: Dict[str, str]
    country_map: Dict[str, str]
    date_map: Dict[str, str]
    id_map: Dict[str, str]

    @classmethod
    def from_json(cls, data: Dict[str, Dict[str, str]]) -> "MappingStore":
        return cls(
            first_name_map=data.get("first_name_map", {}),
            last_name_map=data.get("last_name_map", {}),
            full_name_map=data.get("full_name_map", {}),
            street_map=data.get("street_map", {}),
            email_map=data.get("email_map", {}),
            phone_map=data.get("phone_map", {}),
            postal_code_map=data.get("postal_code_map", {}),
            country_map=data.get("country_map", {}),
            date_map=data.get("date_map", {}),
            id_map=data.get("id_map", {}),
        )

    def to_json(self) -> Dict[str, Dict[str, str]]:
        return {
            "first_name_map": self.first_name_map,
            "last_name_map": self.last_name_map,
            "full_name_map": self.full_name_map,
            "street_map": self.street_map,
            "email_map": self.email_map,
            "phone_map": self.phone_map,
            "postal_code_map": self.postal_code_map,
            "country_map": self.country_map,
            "date_map": self.date_map,
            "id_map": self.id_map,
        }


def normalize_spaces(text: str) -> str:
    return re.sub(r"\s+", " ", text.strip())


def _dedupe_keep_order(values: List[str]) -> List[str]:
    seen: set[str] = set()
    out: List[str] = []
    for value in values:
        if value in seen:
            continue
        seen.add(value)
        out.append(value)
    return out


# Merged pool used by both anonymizers.
_BASE_FIRST_NAMES: List[str] = [
    "James", "JeanLuc", "Benjamin", "Kathryn", "Leonard", "Spock", "Nyota", "Montgomery", "Hikaru", "Pavel",
    "Deanna", "William", "Data", "Geordi", "Beverly", "Worf", "Julian", "Jadzia", "Ezri", "Kira",
    "Quark", "Odo", "Rom", "Nog", "Miles", "Keiko", "Tom", "BElanna", "Harry", "Tuvok",
    "Neelix", "Chakotay", "Seven", "Phlox", "Hoshi", "Travis", "Malcolm", "Michael", "Saru", "Sylvia",
    "Homer", "Marge", "Bart", "Lisa", "Maggie", "Ned", "Maude", "Milhouse", "Nelson", "Ralph",
    "Martin", "Apu", "Moe", "Lenny", "Carl", "Barney", "Krusty", "Sideshow", "Waylon", "Seymour",
    "Edna", "Patty", "Selma", "Abraham", "Mona", "Clancy", "Luann", "Helen", "Rainier", "Agnes",
    "Sherri", "Terri", "Snake", "Chief", "Wiggum", "Comic", "Manjula", "Rod", "Todd", "Otto",
    "Troy", "Kent", "Lurleen", "Jacqueline", "Ling", "Akira", "Sela", "Ro", "Kes", "Icheb",
    "Naomi", "Kestra", "Boimler", "Mariner", "Tendi", "Rutherford", "Freeman", "Shaxs", "Kayshon", "TPol",
]

_EXTRA_FIRST_NAMES: List[str] = [
    "John", "Susan", "Delenn", "Lennier", "Stephen", "Lyta", "Londo", "GKar", "Talia", "Zack",
    "Vir", "Morden", "NaToth", "Kosh", "Lorien", "Ivanova", "Garibaldi", "Sheridan", "Luke", "Leia",
    "Han", "Anakin", "Padme", "ObiWan", "Yoda", "Mace", "Ahsoka", "Rey", "Finn", "Poe",
    "Jyn", "Cassian", "Lando", "Boba", "Din", "Grogu", "Sabine", "Ezra", "Draco", "Luna",
    "Neville", "Cedric", "Cho", "Severus", "Albus", "Minerva", "Sirius", "Remus", "Rubeus", "Fleur",
    "Viktor", "Molly", "Arthur", "Nymphadora", "Hermione", "Ron", "Ginny",
]

_BASE_LAST_NAMES: List[str] = [
    "Kirk", "Picard", "Sisko", "Janeway", "McCoy", "Spock", "Uhura", "Scotty", "Sulu", "Chekov",
    "Troi", "Riker", "Crusher", "LaForge", "Worf", "Bashir", "Dax", "Kira", "OBrien", "Paris",
    "Torres", "Kim", "Tuvok", "Neelix", "Archer", "Sato", "Mayweather", "Reed", "Burnham", "Saru",
    "Georgiou", "Pike", "Spiner", "Q", "Borg", "Simpson", "Flanders", "VanHouten", "Muntz", "Wiggum",
    "Nahasapeemapetilon", "Szyslak", "Leonard", "Carlson", "Gumble", "Krustofsky", "Burns", "Smithers", "Skinner", "Krabappel",
    "Bouvier", "Abe", "Lovejoy", "Hibbert", "Quimby", "Chalmers", "Frink", "Wolfcastle", "Otter", "Terwilliger",
    "McClure", "Brockman", "Prince", "Zoidberg", "Boimler", "Mariner", "Tendi", "Rutherford", "Freeman", "Shaxs",
    "Data", "Lwaxana", "Ro", "Yar", "Pulaski", "Wesley", "Shran", "Morn", "Garak", "Dukat",
    "Weyoun", "Romulan", "Cardassia", "Vulcan", "Andorian", "Bajoran", "Ferengi", "Hansen", "Rios", "Musiker",
    "Jurati", "Soong", "Noonian", "Talos", "Kelvin", "Daystrom", "Shelby", "Locarno", "Sybok", "Vina",
]

_EXTRA_LAST_NAMES: List[str] = [
    "Sheridan", "Ivanova", "Delenn", "Garibaldi", "Franklin", "Mollari", "Narn", "Kosh", "Morden", "Cole",
    "Corwin", "Allan", "Galen", "Sinclair", "Lochley", "Neroon", "TaLon", "NaKal", "Bester", "Drakh",
    "Skywalker", "Organa", "Solo", "Kenobi", "Amidala", "Windu", "Tano", "Palpatine", "Ren", "Andor",
    "Erso", "Calrissian", "Fett", "Djarin", "Kryze", "Bridger", "Wren", "Mandalor", "Plo", "Jarrus",
    "Potter", "Granger", "Weasley", "Malfoy", "Lovegood", "Longbottom", "Diggory", "Chang", "Snape", "Dumbledore",
    "McGonagall", "Black", "Lupin", "Hagrid", "Delacour", "Krum", "Tonks", "Moody", "Lestrange", "Riddle",
]

SHARED_FIRST_NAME_POOL: List[str] = _dedupe_keep_order(_BASE_FIRST_NAMES + _EXTRA_FIRST_NAMES)
SHARED_LAST_NAME_POOL: List[str] = _dedupe_keep_order(_BASE_LAST_NAMES + _EXTRA_LAST_NAMES)


def build_shared_full_name_pool_400() -> List[str]:
    combos = [f"{first} {last}" for first in SHARED_FIRST_NAME_POOL for last in SHARED_LAST_NAME_POOL if first != last]
    rng = random.Random(40777)
    rng.shuffle(combos)
    return combos[:400]


SHARED_FULL_NAME_POOL_400: List[str] = build_shared_full_name_pool_400()


def random_alnum(length: int, rng: random.Random) -> str:
    chars = string.ascii_uppercase + string.digits
    return "".join(rng.choice(chars) for _ in range(length))


def random_digits(length: int, rng: random.Random) -> str:
    if length <= 0:
        return ""
    first = rng.choice("123456789")
    rest = "".join(rng.choice(string.digits) for _ in range(length - 1))
    return first + rest


def random_like_segment(segment: str, rng: random.Random) -> str:
    chars: List[str] = []
    for ch in segment:
        if ch.isdigit():
            chars.append(rng.choice(string.digits))
        elif ch.isalpha():
            chars.append(rng.choice(string.ascii_uppercase))
        else:
            chars.append(ch)
    return "".join(chars)


def randomize_digits_by_pattern(value: str, rng: random.Random) -> str:
    chars: List[str] = []
    for ch in value:
        if ch.isdigit():
            chars.append(rng.choice(string.digits))
        else:
            chars.append(ch)
    return "".join(chars)


def ensure_map(data: Dict[str, Dict[str, str]], key: str) -> Dict[str, str]:
    value = data.get(key)
    if isinstance(value, dict):
        return value
    data[key] = {}
    return data[key]


def load_mapping_dict(path: Path) -> Dict[str, Dict[str, str]]:
    if not path.exists():
        return {}
    with path.open("r", encoding="utf-8") as handle:
        loaded_obj: object = json.load(handle)
        if isinstance(loaded_obj, dict):
            loaded_dict = cast(Dict[object, object], loaded_obj)
            result: Dict[str, Dict[str, str]] = {}
            for key, value in loaded_dict.items():
                if isinstance(key, str) and isinstance(value, dict):
                    value_dict = cast(Dict[object, object], value)
                    cleaned: Dict[str, str] = {}
                    for sub_key, sub_value in value_dict.items():
                        if isinstance(sub_key, str) and isinstance(sub_value, str):
                            cleaned[sub_key] = sub_value
                    result[key] = cleaned
            return result
    return {}


def save_mapping_dict(path: Path, mapping: Dict[str, Dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(mapping, handle, indent=2, ensure_ascii=False)


def load_mapping_store(path: Path) -> MappingStore:
    data = load_mapping_dict(path)
    return MappingStore.from_json(data)


def save_mapping_store(path: Path, mapping: MappingStore) -> None:
    save_mapping_dict(path, mapping.to_json())
