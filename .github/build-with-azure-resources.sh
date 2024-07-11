#!/usr/bin/env bash
set -Eeuo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - KEY_VAULT_NAME

# Create a resource group
az group create \
    --name "${RESOURCE_GROUP_NAME}" \
    --location eastus

# Create an Azure key vault
az keyvault create \
    --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus \
    --enable-rbac-authorization false

# Add a few secrets to the key vault
az keyvault secret set \
    --vault-name "${KEY_VAULT_NAME}" \
    --name secret \
    --value 1234
az keyvault secret set \
    --vault-name "${KEY_VAULT_NAME}" \
    --name anotherSecret \
    --value 5678

# Retrieve the key vault uri and export it as an environment variable
export AZURE_KEYVAULT_URL=$(az keyvault show \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${KEY_VAULT_NAME}" \
  --query properties.vaultUri -o tsv)

# Build and run the integration tests against the Azure services
mvn -version
mvn -B clean install -Dazure.test=true
