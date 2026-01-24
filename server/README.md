# Water History Server

Lightweight FastAPI service that accepts history payloads from the app and appends them to CSV/JSON files.

## Requirements
- Python 3.10+

## Setup
```bash
pip install -r requirements.txt
```

## Run
```bash
C:/Users/JHASHANK/Downloads/water_android/venv/Scripts/python.exe -m uvicorn server.main:app --host 0.0.0.0 --port 8000
```

Server starts on the default FastAPI/uvicorn port configured in `main.py`.

## Project structure
- [main.py](main.py): FastAPI app and storage logic.
- data/: CSV/JSON output (history.csv, devices.json).
- certs/: TLS materials for HTTPS (generated).

## Configuration
Environment variables:
- `WATER_API_KEY`: API key required by clients when `REQUIRE_API_KEY=true`.
- `REQUIRE_API_KEY`: `true` (default) or `false`.
- `MAX_HISTORY_ENTRIES`: Maximum entries per upload.
- `MAX_ENTRY_AMOUNT_ML`: Maximum amount per entry.
- `WATER_EXPORT_DIR`: CSV export output directory.

## HTTPS/TLS (local development)
To use HTTPS locally, generate a local CA and server certificate, install the CA on your Android device, and run uvicorn with SSL.

### 1) Generate a local CA and server certificate
Run these commands from the [server](.) folder:

```bash
mkdir -p certs
openssl genrsa -out certs/ca.key 4096
openssl req -x509 -new -nodes -key certs/ca.key -sha256 -days 3650 -out certs/ca.crt -subj "/CN=Water Dev CA"
openssl genrsa -out certs/server.key 2048
openssl req -new -key certs/server.key -out certs/server.csr -subj "/CN=water-dev"
```

Create `certs/server.ext` with the device/server IP as SANs:

```ini
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
IP.1 = 10.0.2.2
IP.2 = <YOUR_PC_IP>
```

Then sign the server cert:

```bash
openssl x509 -req -in certs/server.csr -CA certs/ca.crt -CAkey certs/ca.key -CAcreateserial -out certs/server.crt -days 825 -sha256 -extfile certs/server.ext
```

### 2) Install the CA on your device
Copy `certs/ca.crt` to the device and install it as a user CA certificate.

### 3) Run uvicorn with SSL
```bash
uvicorn server.main:app --host 0.0.0.0 --port 8000 \
  --ssl-keyfile certs/server.key \
  --ssl-certfile certs/server.crt
```

### 4) Use HTTPS in the app
Set base URL to `https://<YOUR_PC_IP>:8000/` (or `https://10.0.2.2:8000/` for emulator) and keep the API key configured.

## Endpoints
- `GET /health`
- `POST /history`
- `POST /device`

### POST /history payload
```json
{
  "deviceId": "optional-device-id",
  "entries": [
    {"entryId": 1, "timestamp": 1700000000000, "amountMl": 250}
  ]
}
```

Data is appended to `server/data/history.csv`.

### POST /device payload
```json
{
  "deviceId": "optional-device-id",
  "manufacturer": "Google",
  "model": "Pixel 7",
  "sdkInt": 34,
  "appVersion": "1.0",
  "locale": "en-US",
  "timeZone": "America/Los_Angeles",
  "unitSystem": "ML",
  "themeMode": "SYSTEM",
  "dailyGoalMl": 2000,
  "cupSizeMl": 250,
  "adaptive": false,
  "weeklyTargetDays": 7
}
```

Data is appended to `server/data/devices.json`.

## Authentication (required by default)
Set environment variable `WATER_API_KEY`. Clients must send header `X-API-Key` with the same value.

To disable authentication in development:
- Set `REQUIRE_API_KEY=false`

## Runtime limits
- `MAX_HISTORY_ENTRIES` (default 5000)
- `MAX_ENTRY_AMOUNT_ML` (default 5000)
