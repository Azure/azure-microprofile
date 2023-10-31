# Azure Extensions for MicroProfile - Integration Tests

This is the integration test for testing all Azure Extensions for MicroProfile from REST endpoints.

## Installing dependencies

Firstly, you need to make sure the right version of dependencies are installed.

### Use development iteration version

By default, the integration tests depend on the development iteration version. To install the development iteration version, you
need to build it locally.

```
# Switch to the root directory of Azure Extensions for MicroProfile.
# For example, if you are in the directory of azure-microprofile/integration-tests
cd ..

# Install all Azure Extensions for MicroProfile locally.
mvn clean install -DskipTests

# Switch back to the directory of integration-tests
cd integration-tests
```

### Use release version

If you want to use the release version, you need to update the version of **azure-microprofile-bom** in the [`pom.xml`](https://github.com/Azure/azure-microprofile/blob/main/integration-tests/pom.xml#L25) file.

1. Find out the available release version of the Azure MicroProfile extensions from [releases](https://github.com/Azure/azure-microprofile/releases), for example, `1.0.0-beta.1`.
1. Update the **azure-microprofile-bom** in the [`pom.xml`](https://github.com/Azure/azure-microprofile/blob/main/integration-tests/pom.xml#L25) file to your selected version, for example, `1.0.0-beta.1`.
   1. Make sure you are in the directory of **azure-microprofile/integration-tests**.
   1. Open to edit the **pom.xml** file in your favorite editor and update the version of **azure-microprofile-bom**, e.g., `<azure-microprofile-bom.version>1.0.0-beta.1</azure-microprofile-bom.version>`.
   1. Save the changes and close the editor.

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
    --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}"
az keyvault purge \
    --name "${KEY_VAULT_NAME}" \
    --no-wait
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes \
    --no-wait
```
