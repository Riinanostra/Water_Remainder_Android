from __future__ import annotations

import csv
import json
import os
from datetime import datetime, timezone
from pathlib import Path
from threading import Lock
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Header
from pydantic import BaseModel, Field

DATA_DIR = Path(__file__).parent / "data"
DATA_DIR.mkdir(parents=True, exist_ok=True)
CSV_PATH = DATA_DIR / "history.csv"
CSV_LOCK = Lock()
DEVICE_JSON_PATH = DATA_DIR / "devices.json"
DEVICE_LOCK = Lock()
MAX_HISTORY_ENTRIES = int(os.getenv("MAX_HISTORY_ENTRIES", "5000"))
MAX_ENTRY_AMOUNT_ML = int(os.getenv("MAX_ENTRY_AMOUNT_ML", "5000"))
REQUIRE_API_KEY = os.getenv("REQUIRE_API_KEY", "true").strip().lower() == "true"
EXPORT_DIR = Path(os.getenv("WATER_EXPORT_DIR", str(Path.home() / "Downloads")))
EXPORT_DIR.mkdir(parents=True, exist_ok=True)

ROOT_ENV_PATH = Path(__file__).resolve().parents[1] / ".env"


def _read_env_api_key() -> str:
    if not ROOT_ENV_PATH.exists():
        return ""
    for line in ROOT_ENV_PATH.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        if key.strip() == "API_KEY":
            value = value.strip()
            hash_index = value.find("#")
            if hash_index >= 0:
                value = value[:hash_index].strip()
            return value
    return ""


DEFAULT_API_KEY = _read_env_api_key()

HISTORY_INDEX_LOADED = False
HISTORY_KEYS: set[tuple[str, str, str]] = set()
HISTORY_IDS_BY_DEVICE: dict[str, set[int]] = {}
HISTORY_MAX_ID_BY_DEVICE: dict[str, int] = {}
HISTORY_KEY_TO_ID: dict[tuple[str, str, str], int] = {}

app = FastAPI(title="Water History Server", version="1.0.0")


def require_api_key(api_key: str | None) -> None:
    expected = os.getenv("WATER_API_KEY", "").strip() or DEFAULT_API_KEY
    if REQUIRE_API_KEY and not expected:
        raise HTTPException(status_code=401, detail="API key required")
    if expected and api_key != expected:
        raise HTTPException(status_code=401, detail="Invalid API key")


def load_history_index() -> None:
    global HISTORY_INDEX_LOADED, HISTORY_KEYS, HISTORY_IDS_BY_DEVICE, HISTORY_MAX_ID_BY_DEVICE, HISTORY_KEY_TO_ID
    HISTORY_KEYS = set()
    HISTORY_IDS_BY_DEVICE = {}
    HISTORY_MAX_ID_BY_DEVICE = {}
    HISTORY_KEY_TO_ID = {}
    if not CSV_PATH.exists():
        HISTORY_INDEX_LOADED = True
        return
    try:
        with CSV_PATH.open("r", newline="", encoding="utf-8") as file:
            reader = csv.DictReader(file)
            for row in reader:
                device_id = (row.get("device_id", "") or "").strip()
                entry_id_raw = (row.get("entry_id", "") or "").strip()
                timestamp_raw = (row.get("timestamp", "") or "").strip()
                amount_raw = (row.get("amount_ml", "") or "").strip()
                key = (device_id, timestamp_raw, amount_raw)
                HISTORY_KEYS.add(key)
                if entry_id_raw.isdigit():
                    entry_id = int(entry_id_raw)
                    HISTORY_IDS_BY_DEVICE.setdefault(device_id, set()).add(entry_id)
                    HISTORY_MAX_ID_BY_DEVICE[device_id] = max(HISTORY_MAX_ID_BY_DEVICE.get(device_id, 0), entry_id)
                    HISTORY_KEY_TO_ID.setdefault(key, entry_id)
    finally:
        HISTORY_INDEX_LOADED = True


class HistoryEntry(BaseModel):
    entryId: Optional[int] = Field(default=None, description="Optional local entry id")
    timestamp: int = Field(description="Unix epoch millis")
    amountMl: int = Field(ge=0, description="Amount in milliliters")


class HistoryPayload(BaseModel):
    deviceId: Optional[str] = Field(default=None, description="Optional device identifier")
    entries: List[HistoryEntry]


class DevicePayload(BaseModel):
    deviceId: Optional[str] = Field(default=None)
    manufacturer: str
    model: str
    sdkInt: int
    appVersion: str
    locale: str
    timeZone: str
    unitSystem: str
    themeMode: str
    dailyGoalMl: int
    cupSizeMl: int
    adaptive: bool
    weeklyTargetDays: int


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "utc": datetime.now(timezone.utc).isoformat()}


@app.post("/history")
def upload_history(payload: HistoryPayload, x_api_key: str | None = Header(default=None)) -> dict:
    require_api_key(x_api_key)
    if not payload.entries:
        raise HTTPException(status_code=400, detail="entries cannot be empty")
    if len(payload.entries) > MAX_HISTORY_ENTRIES:
        raise HTTPException(status_code=413, detail="too many entries")
    now_ms = int(datetime.now(timezone.utc).timestamp() * 1000)
    for entry in payload.entries:
        if entry.amountMl > MAX_ENTRY_AMOUNT_ML:
            raise HTTPException(status_code=400, detail="amount_ml too large")
        if entry.timestamp > now_ms + 5 * 60 * 1000:
            raise HTTPException(status_code=400, detail="timestamp too far in future")

    with CSV_LOCK:
        if not HISTORY_INDEX_LOADED:
            load_history_index()
        is_new_file = not CSV_PATH.exists()

        with CSV_PATH.open("a", newline="", encoding="utf-8") as file:
            writer = csv.writer(file)
            if is_new_file:
                writer.writerow(["device_id", "entry_id", "timestamp", "amount_ml", "received_utc"])
            received_utc = datetime.now(timezone.utc).isoformat()
            new_entries = []
            for entry in payload.entries:
                device_id = payload.deviceId or ""
                key = (device_id, str(entry.timestamp), str(entry.amountMl))
                if key in HISTORY_KEYS:
                    continue
                existing_ids = HISTORY_IDS_BY_DEVICE.setdefault(device_id, set())
                current_max = HISTORY_MAX_ID_BY_DEVICE.get(device_id, 0)
                entry_id_value = entry.entryId if entry.entryId is not None else None
                if entry_id_value is None:
                    entry_id_value = HISTORY_KEY_TO_ID.get(key)
                if entry_id_value is None or entry_id_value in existing_ids:
                    entry_id_value = current_max + 1
                existing_ids.add(entry_id_value)
                HISTORY_MAX_ID_BY_DEVICE[device_id] = max(HISTORY_MAX_ID_BY_DEVICE.get(device_id, 0), entry_id_value)
                HISTORY_KEYS.add(key)
                HISTORY_KEY_TO_ID[key] = entry_id_value
                writer.writerow([
                    device_id,
                    entry_id_value,
                    entry.timestamp,
                    entry.amountMl,
                    received_utc,
                ])
                new_entries.append((entry_id_value, entry))

        if new_entries:
            export_name = datetime.now().strftime("water_export_%Y%m%d_%H%M.csv")
            export_path = EXPORT_DIR / export_name
            with export_path.open("w", newline="", encoding="utf-8") as export_file:
                export_writer = csv.writer(export_file)
                export_writer.writerow(["id", "timestamp", "amount_ml"])
                for entry_id_value, entry in new_entries:
                    export_writer.writerow([
                        entry_id_value,
                        entry.timestamp,
                        entry.amountMl,
                    ])

    return {"saved": len(new_entries)}


@app.post("/device")
def upload_device(payload: DevicePayload, x_api_key: str | None = Header(default=None)) -> dict:
    require_api_key(x_api_key)
    record = payload.model_dump()
    record["received_utc"] = datetime.now(timezone.utc).isoformat()

    with DEVICE_LOCK:
        if DEVICE_JSON_PATH.exists():
            try:
                existing = DEVICE_JSON_PATH.read_text(encoding="utf-8")
                data = [] if not existing.strip() else json.loads(existing)
                if not isinstance(data, list):
                    data = []
            except json.JSONDecodeError:
                data = []
        else:
            data = []

        data.append(record)
        DEVICE_JSON_PATH.write_text(json.dumps(data, indent=2), encoding="utf-8")

    return {"saved": 1}
