import os
from pathlib import Path
from datetime import datetime, timedelta
from ipaddress import ip_address

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID, ExtendedKeyUsageOID


def _env_device_ip(default="10.0.2.2") -> str:
    root_env = Path(__file__).resolve().parents[1] / ".env"
    if not root_env.exists():
        return default
    for line in root_env.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        if key.strip() == "DEVICE_IP":
            value = value.strip()
            hash_index = value.find("#")
            if hash_index >= 0:
                value = value[:hash_index].strip()
            return value or default
    return default


def main() -> None:
    certs_dir = Path(__file__).resolve().parent / "certs"
    certs_dir.mkdir(parents=True, exist_ok=True)

    ca_key = rsa.generate_private_key(public_exponent=65537, key_size=4096)
    ca_subject = x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, "Water Dev CA"),
    ])
    now = datetime.utcnow()
    ca_cert = (
        x509.CertificateBuilder()
        .subject_name(ca_subject)
        .issuer_name(ca_subject)
        .public_key(ca_key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - timedelta(days=1))
        .not_valid_after(now + timedelta(days=3650))
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
        .sign(private_key=ca_key, algorithm=hashes.SHA256())
    )

    server_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    server_subject = x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, "water-dev"),
    ])

    ip_list = {"10.0.2.2", _env_device_ip()}
    san_list = []
    for ip in ip_list:
        try:
            san_list.append(x509.IPAddress(ip_address(ip)))
        except ValueError:
            pass

    server_cert = (
        x509.CertificateBuilder()
        .subject_name(server_subject)
        .issuer_name(ca_cert.subject)
        .public_key(server_key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - timedelta(days=1))
        .not_valid_after(now + timedelta(days=825))
        .add_extension(
            x509.SubjectAlternativeName(san_list),
            critical=False,
        )
        .add_extension(
            x509.ExtendedKeyUsage([ExtendedKeyUsageOID.SERVER_AUTH]),
            critical=False,
        )
        .sign(private_key=ca_key, algorithm=hashes.SHA256())
    )

    (certs_dir / "ca.key").write_bytes(
        ca_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption(),
        )
    )
    (certs_dir / "ca.crt").write_bytes(
        ca_cert.public_bytes(serialization.Encoding.PEM)
    )
    (certs_dir / "server.key").write_bytes(
        server_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption(),
        )
    )
    (certs_dir / "server.crt").write_bytes(
        server_cert.public_bytes(serialization.Encoding.PEM)
    )

    print("Generated certs in", certs_dir)


if __name__ == "__main__":
    main()
