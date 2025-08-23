#!/bin/sh
# =========================
# CORRECTED Vault Init Script - Individual Service Paths
# =========================
set -e

# Set variables with proper fallbacks
VAULT_ADDR=${VAULT_ADDR:-http://localhost:8200}
VAULT_TOKEN=${VAULT_DEV_ROOT_TOKEN_ID:-token}

echo ">> Setting Vault address: $VAULT_ADDR"
export VAULT_ADDR=$VAULT_ADDR

echo ">> Waiting for Vault to be fully ready..."
sleep 15

# Add retry logic for Vault readiness
max_attempts=5
attempt=1
while [ $attempt -le $max_attempts ]; do
    echo ">> Attempt $attempt: Checking Vault status"
    if vault status > /dev/null 2>&1; then
        echo ">> Vault is ready!"
        break
    else
        echo ">> Vault not ready yet, waiting..."
        sleep 10
        attempt=$((attempt + 1))
    fi
done

if [ $attempt -gt $max_attempts ]; then
    echo "ERROR: Vault is not ready after $max_attempts attempts"
    exit 1
fi

echo ">> Logging in to Vault with token"
vault login $VAULT_TOKEN || {
    echo "ERROR: Failed to login to Vault"
    vault status
    exit 1
}

echo ">> Checking Vault status after login"
vault status

# =========================
# Enable KV v2 secrets engine at 'config' path
# =========================
echo ">> Enabling KV v2 secrets engine at path 'config'"
vault secrets enable -path=config kv-v2 || {
    echo ">> KV secrets engine already exists or failed to create, checking..."
    if vault secrets list | grep -q "config/"; then
        echo ">> KV secrets engine at 'config' already exists, continuing..."
    else
        echo "ERROR: Failed to enable KV secrets engine"
        exit 1
    fi
}

echo "==> Writing secrets into Vault for individual services with per-env separation..."

# ===== account-service =====
# Required keys: POSTGRES_URL, POSTGRES_USERNAME, POSTGRES_PASSWORD, REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
# --- DEV ---
echo ">> Writing DEV secrets for account-service"
vault kv put config/account-service/dev \
  POSTGRES_URL="jdbc:postgresql://localhost:5432/postgresdb" \
  POSTGRES_USERNAME="postgres" \
  POSTGRES_PASSWORD="postgres" \
  REDIS_HOST="localhost" \
  REDIS_PORT="6379" \
  REDIS_PASSWORD="redis_password" || { echo "ERROR: Failed to write DEV secrets for account-service"; exit 1; }
# --- QA ---
echo ">> Writing QA secrets for account-service"
vault kv put config/account-service/qa \
  POSTGRES_URL="jdbc:postgresql://localhost:5432/accountdb_qa" \
  POSTGRES_USERNAME="account_qa" \
  POSTGRES_PASSWORD="account_qa_pass" \
  REDIS_HOST="localhost" \
  REDIS_PORT="6379" \
  REDIS_PASSWORD="redis_qa_password" || { echo "ERROR: Failed to write QA secrets for account-service"; exit 1; }
# --- PROD ---
echo ">> Writing PROD secrets for account-service"
vault kv put config/account-service/prod \
  POSTGRES_URL="jdbc:postgresql://localhost:5432/accountdb_prod" \
  POSTGRES_USERNAME="account_prod" \
  POSTGRES_PASSWORD="account_prod_pass" \
  REDIS_HOST="localhost" \
  REDIS_PORT="6379" \
  REDIS_PASSWORD="redis_prod_password" || { echo "ERROR: Failed to write PROD secrets for account-service"; exit 1; }

# ===== notification-service =====
# Required keys: MONGO_HOST, MONGO_USERNAME, MONGO_PASSWORD, MONGO_DATABASE, MONGO_AUTH_SOURCE, KAFKA_BOOTSTRAP_SERVERS
# --- DEV ---
echo ">> Writing DEV secrets for notification-service"
vault kv put config/notification-service/dev \
  MONGO_HOST="localhost:27017" \
  MONGO_USERNAME="mongo" \
  MONGO_PASSWORD="mongo" \
  MONGO_DATABASE="mongodb" \
  MONGO_AUTH_SOURCE="admin" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write DEV secrets for notification-service"; exit 1; }
# --- QA ---
echo ">> Writing QA secrets for notification-service"
vault kv put config/notification-service/qa \
  MONGO_HOST="localhost:27017" \
  MONGO_USERNAME="notif_qa" \
  MONGO_PASSWORD="notif_qa_pass" \
  MONGO_DATABASE="notifdb_qa" \
  MONGO_AUTH_SOURCE="admin" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write QA secrets for notification-service"; exit 1; }
# --- PROD ---
echo ">> Writing PROD secrets for notification-service"
vault kv put config/notification-service/prod \
  MONGO_HOST="localhost:27017" \
  MONGO_USERNAME="notif_prod" \
  MONGO_PASSWORD="notif_prod_pass" \
  MONGO_DATABASE="notifdb_prod" \
  MONGO_AUTH_SOURCE="admin" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write PROD secrets for notification-service"; exit 1; }

# ===== payment-service =====
# Required keys: R2DBC_POSTGRES_URL, POSTGRES_USERNAME, POSTGRES_PASSWORD, KAFKA_BOOTSTRAP_SERVERS
# --- DEV ---
echo ">> Writing DEV secrets for payment-service"
vault kv put config/payment-service/dev \
  R2DBC_POSTGRES_URL="r2dbc:postgresql://localhost:5432/postgresdb" \
  POSTGRES_USERNAME="postgres" \
  POSTGRES_PASSWORD="postgres" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write DEV secrets for payment-service"; exit 1; }
# --- QA ---
echo ">> Writing QA secrets for payment-service"
vault kv put config/payment-service/qa \
  R2DBC_POSTGRES_URL="r2dbc:postgresql://localhost:5432/paymentdb_qa" \
  POSTGRES_USERNAME="payment_qa" \
  POSTGRES_PASSWORD="payment_qa_pass" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write QA secrets for payment-service"; exit 1; }
# --- PROD ---
echo ">> Writing PROD secrets for payment-service"
vault kv put config/payment-service/prod \
  R2DBC_POSTGRES_URL="r2dbc:postgresql://localhost:5432/paymentdb_prod" \
  POSTGRES_USERNAME="payment_prod" \
  POSTGRES_PASSWORD="payment_prod_pass" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write PROD secrets for payment-service"; exit 1; }

# ===== transaction-service =====
# Required keys: MONGO_HOST, MONGO_USERNAME, MONGO_PASSWORD, MONGO_DATABASE, MONGO_AUTH_SOURCE, KAFKA_BOOTSTRAP_SERVERS
# --- DEV ---
echo ">> Writing DEV secrets for transaction-service"
vault kv put config/transaction-service/dev \
  MONGO_HOST="localhost:27017" \
  MONGO_USERNAME="mongo" \
  MONGO_PASSWORD="mongo" \
  MONGO_DATABASE="mongodb" \
  MONGO_AUTH_SOURCE="admin" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write DEV secrets for transaction-service"; exit 1; }
# --- QA ---
echo ">> Writing QA secrets for transaction-service"
vault kv put config/transaction-service/qa \
  MONGO_HOST="localhost:27017" \
  MONGO_USERNAME="tx_qa" \
  MONGO_PASSWORD="tx_qa_pass" \
  MONGO_DATABASE="txdb_qa" \
  MONGO_AUTH_SOURCE="admin" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write QA secrets for transaction-service"; exit 1; }
# --- PROD ---
echo ">> Writing PROD secrets for transaction-service"
vault kv put config/transaction-service/prod \
  MONGO_HOST="localhost:27017" \
  MONGO_USERNAME="tx_prod" \
  MONGO_PASSWORD="tx_prod_pass" \
  MONGO_DATABASE="txdb_prod" \
  MONGO_AUTH_SOURCE="admin" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:29092" || { echo "ERROR: Failed to write PROD secrets for transaction-service"; exit 1; }

## ===== api-gateway (minimal) =====
## Required keys (minimal): EUREKA_SERVER_URL
#for env in dev qa prod; do
#  echo ">> Writing $(printf '%s' "$env" | tr '[:lower:]' '[:upper:]') secrets for api-gateway"
#  vault kv put config/api-gateway/$env \
#    EUREKA_SERVER_URL="http://localhost:8887/eureka/" || { echo "ERROR: Failed to write $env secrets for api-gateway"; exit 1; }
#done
#
## ===== eureka-server (minimal) =====
## Often does not require DB/Kafka creds; keep minimal or empty as needed
#for env in dev qa prod; do
#  echo ">> Writing $(printf '%s' "$env" | tr '[:lower:]' '[:upper:]') secrets for eureka-server"
#  vault kv put config/eureka-server/$env \
#    EUREKA_SERVER_URL="http://localhost:8887/eureka/" || { echo "ERROR: Failed to write $env secrets for eureka-server"; exit 1; }
#done
#
## ===== config-server (minimal) =====
## Minimal discovery URL if used
#for env in dev qa prod; do
#  echo ">> Writing $(printf '%s' "$env" | tr '[:lower:]' '[:upper:]') secrets for config-server"
#  vault kv put config/config-server/$env \
#    EUREKA_SERVER_URL="http://localhost:8887/eureka/" || { echo "ERROR: Failed to write $env secrets for config-server"; exit 1; }
#done

# =========================
# Verification
# =========================
echo ">> Verifying secrets were written correctly (showing key services)"

for svc in account-service notification-service payment-service transaction-service; do
  for env in dev qa prod; do
    echo ">> ${svc} $(printf '%s' "$env" | tr '[:lower:]' '[:upper:]') secrets:"
    vault kv get config/${svc}/${env} || echo "Failed to retrieve ${svc} ${env} secrets"
  done
done

echo ">> All secrets written successfully!"
echo ">> Vault initialization completed."