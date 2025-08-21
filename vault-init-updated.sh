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

echo "==> Writing secrets into Vault for individual services..."

# Define all your microservices
SERVICES="account-service payment-service notification-service transaction-service api-gateway eureka-server config-server"

# ===== DEV ENVIRONMENT =====
echo ">> Writing DEV environment secrets for all services"
for service in $SERVICES; do
    echo ">> Writing DEV secrets for $service"
    vault kv put config/$service/dev \
      POSTGRES_HOST="localhost:5432" \
      POSTGRES_USERNAME="postgres" \
      POSTGRES_PASSWORD="postgres" \
      POSTGRES_URL="jdbc:postgresql://localhost:5432/postgresdb" \
      MONGO_HOST="localhost:27017" \
      MONGO_USERNAME="mongo" \
      MONGO_PASSWORD="mongo" \
      MONGO_AUTH_SOURCE="admin" \
      REDIS_HOST="localhost" \
      REDIS_PORT="6379" \
      REDIS_PASSWORD="redis_password" \
      KAFKA_BOOTSTRAP_SERVERS="localhost:29092" \
      EUREKA_SERVER_URL="http://localhost:8887/eureka/" || {
        echo "ERROR: Failed to write DEV secrets for $service"
        exit 1
    }
done

# ===== QA ENVIRONMENT =====
echo ">> Writing QA environment secrets for all services"
for service in $SERVICES; do
    echo ">> Writing QA secrets for $service"
    vault kv put config/$service/qa \
      POSTGRES_HOST="localhost:5432" \
      POSTGRES_USERNAME="postgres" \
      POSTGRES_PASSWORD="postgres" \
      POSTGRES_URL="jdbc:postgresql://localhost:5432/postgresdb" \
      MONGO_HOST="localhost:27017" \
      MONGO_USERNAME="mongo" \
      MONGO_PASSWORD="mongo" \
      MONGO_AUTH_SOURCE="admin" \
      REDIS_HOST="localhost" \
      REDIS_PORT="6379" \
      REDIS_PASSWORD="redis_password" \
      KAFKA_BOOTSTRAP_SERVERS="localhost:29092" \
      EUREKA_SERVER_URL="http://localhost:8887/eureka/" || {
        echo "ERROR: Failed to write QA secrets for $service"
        exit 1
    }
done

# ===== PROD ENVIRONMENT =====
echo ">> Writing PROD environment secrets for all services"
for service in $SERVICES; do
    echo ">> Writing PROD secrets for $service"
    vault kv put config/$service/prod \
      POSTGRES_HOST="localhost:5432" \
      POSTGRES_USERNAME="postgres" \
      POSTGRES_PASSWORD="postgres" \
      POSTGRES_URL="jdbc:postgresql://localhost:5432/postgresdb" \
      MONGO_HOST="localhost:27017" \
      MONGO_USERNAME="mongo" \
      MONGO_PASSWORD="mongo" \
      MONGO_AUTH_SOURCE="admin" \
      REDIS_HOST="localhost" \
      REDIS_PORT="6379" \
      REDIS_PASSWORD="redis_password" \
      KAFKA_BOOTSTRAP_SERVERS="localhost:29092" \
      EUREKA_SERVER_URL="http://localhost:8887/eureka/" || {
        echo "ERROR: Failed to write PROD secrets for $service"
        exit 1
    }
done

echo ">> Verifying secrets were written correctly"

echo ">> account-service DEV secrets:"
vault kv get config/account-service/dev || echo "Failed to retrieve account-service DEV secrets"

echo ">> account-service QA secrets:"
vault kv get config/account-service/qa || echo "Failed to retrieve account-service QA secrets"

echo ">> account-service PROD secrets:"
vault kv get config/account-service/prod || echo "Failed to retrieve account-service PROD secrets"

echo ">> All secrets written successfully!"
echo ">> Vault initialization completed."
