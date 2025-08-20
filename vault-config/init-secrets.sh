#!/bin/sh
set -e

# =========================
# Vault Init Secrets Script
# =========================
VAULT_ADDR=${VAULT_ADDR:-http://localhost:8200}
VAULT_TOKEN=${VAULT_DEV_ROOT_TOKEN_ID:-dev-root-token}

echo ">> Waiting a bit for Vault to be ready..."
sleep 5

echo ">> Logging in"
vault login $VAULT_TOKEN || true

# =========================
# Enable KV v2 secrets engine
# =========================
vault secrets enable -path=secret kv-v2 || true

echo "==> Writing secrets into Vault..."

# ===== DEV ENVIRONMENT =====
vault kv put secret/dev/database \
  POSTGRES_URL=jdbc:postgresql://localhost:5432/postgresdb \
  POSTGRES_USERNAME=postgres \
  POSTGRES_PASSWORD=postgres \
  MONGO_URL=mongodb://mongo:mongo@localhost:27017/mongodb?authSource=admin \
  REDIS_HOST="localhost:6379" \
  REDIS_PASSWORD="redis_password"

vault kv put secret/dev/kafka \
  KAFKA_BOOTSTRAP_SERVERS=localhost:29092

# ===== QA ENVIRONMENT =====
vault kv put secret/qa/database \
  POSTGRES_URL=jdbc:postgresql://localhost:5432/postgresdb \
  POSTGRES_USERNAME=postgres \
  POSTGRES_PASSWORD=postgres \
  MONGO_URL=mongodb://mongo:mongo@localhost:27017/mongodb?authSource=admin \
  REDIS_HOST="localhost:6379" \
  REDIS_PASSWORD="redis_password"

vault kv put secret/qa/kafka \
  KAFKA_BOOTSTRAP_SERVERS=localhost:29092

# ===== PROD ENVIRONMENT =====
vault kv put secret/prod/database \
  POSTGRES_URL=jdbc:postgresql://localhost:5432/postgresdb \
  POSTGRES_USERNAME=postgres \
  POSTGRES_PASSWORD=postgres \
  MONGO_URL=mongodb://mongo:mongo@localhost:27017/mongodb?authSource=admin \
  REDIS_HOST="localhost:6379" \
  REDIS_PASSWORD="redis_password"

vault kv put secret/prod/kafka \
  KAFKA_BOOTSTRAP_SERVERS=localhost:29092

echo ">> All secrets written."
