# Azure Extensions for MicroProfile - Integration Tests

This is the integration test for testing all Azure Extensions for MicroProfile from REST endpoints.

## Installing dependencies locally in development iteration

You need to install Azure Extensions for MicroProfile locally before running the test.

```
# Switch to the root directory of Azure Extensions for MicroProfile.
# For example, if you are in the directory of azure-microprofile/integration-tests
cd ..

# Install all Azure Extensions for MicroProfile locally.
mvn clean install -DskipTests

# Switch back to the directory of integration-tests
cd integration-tests
```

## Running the test with Azure services

Then create the dependent Azure services after logging into Azure.

### Logging into Azure

Log into Azure and create a resource group for hosting different Azure services to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location eastus
```

### Creating Azure Key Vault

Run the following commands to create an Azure Key Vault instance, add a few secrets, and export its uri as environment
variables.

```
KEY_VAULT_NAME=<unique-key-vault-name>
az keyvault create \
    --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus

az keyvault secret set \
    --vault-name "${KEY_VAULT_NAME}" \
    --name secret \
    --value 1234
az keyvault secret set \
    --vault-name "${KEY_VAULT_NAME}" \
    --name anotherSecret \
    --value 5678

export AZURE_KEYVAULT_URL=$(az keyvault show \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --name "${KEY_VAULT_NAME}" \
    --query properties.vaultUri -o tsv)
```

The values of environment variable `AZURE_KEYVAULT_URL` will be fed into config property `azure.keyvault.url`
of `microprofile-config-keyvault` extension in order to set up the connection to the Azure Key Vault instance.

### Running the test

Finally, launch the integration test with:

```
mvn verify -Dazure.test=true
```

### Cleaning up Azure resources

Once you complete the test, run the following command to clean up the Azure resources used in the test:

```
az keyvault delete \
    --name "${KEY_VAULT_NAME}"
az keyvault purge \
    --name "${KEY_VAULT_NAME}" \
    --no-wait
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes \
    --no-wait
```
