#!/bin/sh
# =========================
# Vault Setup for Account Service - CORRECTED
# =========================
set -e

VAULT_ADDR=${VAULT_ADDR:-http://localhost:8200}
VAULT_TOKEN=${VAULT_DEV_ROOT_TOKEN_ID:-dev-root-token}

echo ">> Setting up Vault secrets for account-service..."
export VAULT_ADDR=$VAULT_ADDR

echo ">> Login to Vault"
vault login $VAULT_TOKEN

echo ">> Enable KV v2 secrets engine at 'config' path"
vault secrets enable -path=config kv-v2 || echo "KV engine already exists"

echo ">> Creating secrets for account-service/prod"
vault kv put config/account-service/prod \
  POSTGRES_USERNAME="postgres" \
  POSTGRES_PASSWORD="postgres" \
  REDIS_HOST="localhost" \
  REDIS_PORT="6379" \
  REDIS_PASSWORD="redis_password"

echo ">> Creating secrets for account-service/dev"
vault kv put config/account-service/dev \
  POSTGRES_USERNAME="postgres" \
  POSTGRES_PASSWORD="postgres" \
  REDIS_HOST="localhost" \
  REDIS_PORT="6379" \
  REDIS_PASSWORD="redis_password"

echo ">> Verifying secrets"
echo "Production secrets:"
vault kv get config/account-service/prod

echo "Development secrets:"
vault kv get config/account-service/dev

echo ">> Vault setup completed!"
