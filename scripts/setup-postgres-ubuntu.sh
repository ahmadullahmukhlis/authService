#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./scripts/setup-postgres-ubuntu.sh
#
# This script configures the local Ubuntu PostgreSQL service for this project:
# - database: auth_db
# - user: mukhlis
# - password: password123

DB_NAME="${DB_NAME:-auth_db}"
DB_USER="${DB_USER:-mukhlis}"
DB_PASSWORD="${DB_PASSWORD:-password123}"

echo "Configuring PostgreSQL database '${DB_NAME}' and user '${DB_USER}'..."

sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${DB_USER}') THEN
      CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASSWORD}';
   ELSE
      ALTER ROLE ${DB_USER} WITH LOGIN PASSWORD '${DB_PASSWORD}';
   END IF;
END
\$\$;

DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}') THEN
      CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};
   END IF;
END
\$\$;
SQL

sudo -u postgres psql -d "${DB_NAME}" -v ON_ERROR_STOP=1 <<SQL
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
GRANT ALL ON SCHEMA public TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DB_USER};
SQL

echo "Done."
echo "Run app with:"
echo "DB_URL=jdbc:postgresql://localhost:5432/${DB_NAME} DB_USERNAME=${DB_USER} DB_PASSWORD=${DB_PASSWORD} ./mvnw spring-boot:run"
