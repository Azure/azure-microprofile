#!/usr/bin/env bash

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - KEY_VAULT_NAME

az keyvault delete --name "${KEY_VAULT_NAME}" --resource-group "${RESOURCE_GROUP_NAME}"
az keyvault purge --name "${KEY_VAULT_NAME}"
az group delete --name "${RESOURCE_GROUP_NAME}" --yes
