#!/usr/bin/env python3
"""Generate Android string resources for My Vault's global language rollout.

This temporary helper is intended to run in GitHub Actions, where the public
Google Translate endpoint is reachable. It protects Android format arguments,
line-break escapes, product names and technical terms, translates the English
values in small batches, and validates every result before writing XML.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path
from xml.sax.saxutils import escape as xml_escape

# app language tag -> (Google Translate target, Android values directory)
TARGETS: dict[str, tuple[str, str]] = {
    "hi": ("hi", "values-hi"),
    "es": ("es", "values-es"),
    "fr": ("fr", "values-fr"),
    "ar": ("ar", "values-ar"),
    "ru": ("ru", "values-ru"),
    "pt-BR": ("pt", "values-pt-rBR"),
    "zh-CN": ("zh-CN", "values-zh-rCN"),
    "id": ("id", "values-in"),
    "de": ("de", "values-de"),
    "ja": ("ja", "values-ja"),
    "ko": ("ko", "values-ko"),
    "tr": ("tr", "values-tr"),
    "it": ("it", "values-it"),
}

UNTRANSLATED_KEYS = {
    "app_name",
    "lock_my_vault",
    "creator_name",
    "settings_theme_oled",
    "accent_orion",
    "ack_space_grotesk_title",
    "ack_androidx_title",
    "ack_mlkit_title",
    "ack_coil_title",
}

# Longest first is applied at runtime.
PROTECTED_TERMS = [
    "RookieZ (RookieEnough)",
    "Google Play services",
    "PBKDF2-HMAC-SHA256",
    "Android Keystore-backed",
    "Android Keystore",
    "Jetpack Compose",
    "Google ML Kit",
    "GitHub Releases",
    "Space Grotesk",
    "Material You",
    "Orion Store",
    "AES-256-GCM",
    "Material 3",
    ".mvbackup",
    "My Vault",
    "AndroidX",
    "DataStore",
    "AES-256",
    "AES-GCM",
    "Aadhaar",
    "Android",
    "Biometric",
    "GitHub",
    "Google",
    "ML Kit",
    "Sinan",
    "Orion",
    "Room",
    "Coil",
    "600,000",
    "APK",
    "PDF",
    "PIN",
    "PAN",
]

PLACEHOLDER_RE = re.compile(r"%\d+\$[a-zA-Z%]|%%")
SEGMENT_RE = re.compile(r"\[\[\s*MVSEG\s*(\d{3})\s*\]\]")
NEWLINE_SENTINEL = "\ue000"


@dataclass
class SourceString:
    name: str
    value: str
    formatted: str | None


@dataclass
class PreparedString:
    source: SourceString
    text_for_translation: str
    tokens: dict[str, str]


class Translator:
    def __init__(self) -> None:
        self.cache: dict[tuple[str, str], str] = {}

    def translate_raw(self, text: str, target: str) -> str:
        key = (target, text)
        if key in self.cache:
            return self.cache[key]

        query = urllib.parse.urlencode(
            {
                "client": "gtx",
                "sl": "en",
                "tl": target,
                "dt": "t",
                "q": text,
            }
        )
        request = urllib.request.Request(
            f"https://translate.googleapis.com/translate_a/single?{query}",
            headers={
                "User-Agent": (
                    "Mozilla/5.0 (X11; Linux x86_64) "
                    "AppleWebKit/537.36 (KHTML, like Gecko) "
                    "Chrome/124.0 Safari/537.36"
                )
            },
        )

        last_error: Exception | None = None
        for attempt in range(5):
            try:
                with urllib.request.urlopen(request, timeout=45) as response:
                    payload = json.loads(response.read().decode("utf-8"))
                translated = "".join(
                    str(segment[0])
                    for segment in payload[0]
                    if segment and segment[0] is not None
                )
                self.cache[key] = translated
                return translated
            except (OSError, urllib.error.URLError, ValueError, IndexError) as exc:
                last_error = exc
                if attempt < 4:
                    time.sleep(1.5 * (attempt + 1))

        raise RuntimeError(f"Translation request failed for {target}: {last_error}")

    def translate_batch(
        self, items: list[PreparedString], target: str
    ) -> dict[str, str]:
        if not items:
            return {}

        if len(items) == 1:
            item = items[0]
            return {
                item.source.name: self.translate_raw(
                    item.text_for_translation, target
                ).strip()
            }

        marked = "\n".join(
            f"[[MVSEG{index:03d}]] {item.text_for_translation}"
            for index, item in enumerate(items)
        )
        translated_marked = self.translate_raw(marked, target)
        matches = list(SEGMENT_RE.finditer(translated_marked))
        parsed: dict[str, str] = {}

        if len(matches) == len(items):
            for position, match in enumerate(matches):
                index = int(match.group(1))
                if index >= len(items):
                    break
                start = match.end()
                end = (
                    matches[position + 1].start()
                    if position + 1 < len(matches)
                    else len(translated_marked)
                )
                parsed[items[index].source.name] = translated_marked[
                    start:end
                ].strip()
            if len(parsed) == len(items):
                return parsed

        midpoint = len(items) // 2
        result = self.translate_batch(items[:midpoint], target)
        result.update(self.translate_batch(items[midpoint:], target))
        return result


def load_sources(path: Path) -> list[SourceString]:
    root = ET.parse(path).getroot()
    sources: list[SourceString] = []
    for element in root.findall("string"):
        sources.append(
            SourceString(
                name=element.attrib["name"],
                value="".join(element.itertext()),
                formatted=element.attrib.get("formatted"),
            )
        )
    return sources


def prepare(source: SourceString) -> PreparedString:
    text = source.value.replace("\\'", "'").replace('\\"', '"')
    tokens: dict[str, str] = {}
    counter = 0

    def add_token(kind: str, original: str) -> str:
        nonlocal counter
        label = f"MV{kind}{counter}"
        counter += 1
        tokens[label] = original
        return f"[[{label}]]"

    # Android's literal \n sequences must survive as escapes, not real lines.
    text = re.sub(r"\\n", lambda _: add_token("NL", NEWLINE_SENTINEL), text)

    # Format arguments are syntax, not prose.
    text = PLACEHOLDER_RE.sub(lambda match: add_token("PH", match.group(0)), text)

    # Product names, file extensions and technical identifiers stay verbatim.
    for term in sorted(PROTECTED_TERMS, key=len, reverse=True):
        if term in text:
            text = text.replace(term, add_token("TERM", term))

    # The batch protocol is line based.
    text = " ".join(text.split())
    return PreparedString(source=source, text_for_translation=text, tokens=tokens)


def restore_tokens(translated: str, prepared: PreparedString) -> tuple[str, list[str]]:
    missing: list[str] = []
    result = translated
    for label, original in prepared.tokens.items():
        pattern = re.compile(r"\[\[\s*" + re.escape(label) + r"\s*\]\]", re.I)
        if pattern.search(result):
            result = pattern.sub(original, result)
            continue

        loose_pattern = re.compile(re.escape(label), re.I)
        if loose_pattern.search(result):
            result = loose_pattern.sub(original, result)
        else:
            missing.append(label)
    return result.strip(), missing


def android_escape(text: str) -> str:
    # Escape only Android-sensitive characters. XML escaping happens after.
    escaped = text.replace("\\", "\\\\")
    escaped = escaped.replace("'", "\\'").replace('"', '\\"')
    escaped = escaped.replace(NEWLINE_SENTINEL, "\\n")
    if escaped.startswith(("@", "?")):
        escaped = "\\" + escaped
    return xml_escape(escaped)


def chunks(items: list[PreparedString], max_chars: int = 3600) -> list[list[PreparedString]]:
    result: list[list[PreparedString]] = []
    current: list[PreparedString] = []
    current_length = 0
    for item in items:
        item_length = len(item.text_for_translation) + 18
        if current and current_length + item_length > max_chars:
            result.append(current)
            current = []
            current_length = 0
        current.append(item)
        current_length += item_length
    if current:
        result.append(current)
    return result


def placeholders(text: str) -> list[str]:
    return PLACEHOLDER_RE.findall(text)


def generate_language(
    *,
    sources: list[SourceString],
    google_target: str,
    output_file: Path,
    translator: Translator,
) -> dict[str, object]:
    prepared_by_name: dict[str, PreparedString] = {}
    to_translate: list[PreparedString] = []
    for source in sources:
        prepared = prepare(source)
        prepared_by_name[source.name] = prepared
        if source.name not in UNTRANSLATED_KEYS:
            to_translate.append(prepared)

    raw_translations: dict[str, str] = {}
    for index, chunk in enumerate(chunks(to_translate), start=1):
        print(
            f"[{google_target}] chunk {index}: {len(chunk)} strings",
            flush=True,
        )
        raw_translations.update(translator.translate_batch(chunk, google_target))

    missing_tokens: dict[str, list[str]] = {}
    placeholder_mismatches: list[str] = []
    empty_values: list[str] = []
    final_values: dict[str, str] = {}

    for source in sources:
        if source.name in UNTRANSLATED_KEYS:
            final_values[source.name] = source.value
            continue

        prepared = prepared_by_name[source.name]
        translated = raw_translations.get(source.name, "")
        restored, missing = restore_tokens(translated, prepared)
        if missing:
            missing_tokens[source.name] = missing
        if not restored:
            empty_values.append(source.name)
            restored = source.value.replace("\\'", "'").replace('\\"', '"')
        if placeholders(restored) != placeholders(source.value):
            placeholder_mismatches.append(source.name)
        final_values[source.name] = restored

    output_file.parent.mkdir(parents=True, exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for source in sources:
        formatted = (
            f' formatted="{source.formatted}"' if source.formatted is not None else ""
        )
        lines.append(
            f'    <string name="{source.name}"{formatted}>'
            f"{android_escape(final_values[source.name])}</string>"
        )
    lines.append("</resources>")
    output_file.write_text("\n".join(lines) + "\n", encoding="utf-8")

    return {
        "strings": len(sources),
        "missing_tokens": missing_tokens,
        "placeholder_mismatches": placeholder_mismatches,
        "empty_values": empty_values,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--source",
        type=Path,
        default=Path("app/src/main/res/values/strings.xml"),
    )
    parser.add_argument("--output-root", type=Path, default=Path("generated-locales"))
    parser.add_argument(
        "--languages",
        nargs="*",
        choices=sorted(TARGETS),
        default=sorted(TARGETS),
    )
    args = parser.parse_args()

    sources = load_sources(args.source)
    translator = Translator()
    report: dict[str, object] = {}

    for language in args.languages:
        google_target, directory = TARGETS[language]
        output_file = args.output_root / directory / "strings.xml"
        print(f"Generating {language} -> {output_file}", flush=True)
        report[language] = generate_language(
            sources=sources,
            google_target=google_target,
            output_file=output_file,
            translator=translator,
        )

    args.output_root.mkdir(parents=True, exist_ok=True)
    (args.output_root / "translation-report.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(report, ensure_ascii=False, indent=2))

    has_problems = any(
        details["missing_tokens"]
        or details["placeholder_mismatches"]
        or details["empty_values"]
        for details in report.values()
        if isinstance(details, dict)
    )
    return 1 if has_problems else 0


if __name__ == "__main__":
    sys.exit(main())
