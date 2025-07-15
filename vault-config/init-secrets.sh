#!/bin/bash

# Wait for Vault to be ready
echo "Waiting for Vault to be ready..."
sleep 10

# Set Vault address
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=dev-root-token

echo "Setting up Vault secrets for Payment Platform..."

# Enable KV secrets engine
vault secrets enable -path=payment kv-v2

# Database secrets
vault kv put payment/database \
  postgres_user=payment_user \
  postgres_password=payment_password \
  postgres_url=jdbc:postgresql://postgres:5432/payment_platform \
  mongo_user=mongo_user \
  mongo_password=mongo_password \
  redis_password=redis_password

# Application secrets
vault kv put payment/account-service \
  jwt_secret=account-service-jwt-secret-key-2024 \
  encryption_key=account-service-encryption-key \
  api_key=account-service-api-key

vault kv put payment/payment-service \
  jwt_secret=payment-service-jwt-secret-key-2024 \
  encryption_key=payment-service-encryption-key \
  api_key=payment-service-api-key

vault kv put payment/transaction-service \
  jwt_secret=transaction-service-jwt-secret-key-2024 \
  encryption_key=transaction-service-encryption-key \
  api_key=transaction-service-api-key

vault kv put payment/notification-service \
  jwt_secret=notification-service-jwt-secret-key-2024 \
  email_api_key=email-service-api-key \
  sms_api_key=sms-service-api-key

# External API secrets
vault kv put payment/external-apis \
  stripe_secret_key=sk_test_your_stripe_secret \
  paypal_client_id=your_paypal_client_id \
  paypal_client_secret=your_paypal_client_secret

echo "Vault secrets setup completed!"
echo "Access Vault UI at: http://localhost:8200"
echo "Root token: dev-root-token"
