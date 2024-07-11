# Azure Extensions for MicroProfile - Integration Tests

This is the integration test for testing all Azure Extensions for MicroProfile from REST endpoints.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription

## Preparing integration tests

Firstly, you need to clone the repository and switch to the directory of integration tests.

```
git clone https://github.com/Azure/azure-microprofile.git
cd azure-microprofile/integration-tests
```

## Preparing the Azure services

The Custom ConfigSource implemented by `azure-microprofile-config-keyvault` extension needs to connect to a real Azure Key Vault instance, follow steps below to create one.

### Logging into Azure

Sign in to Azure and create a resource group for hosting different Azure services to be created.

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
    --location eastus \
    --enable-rbac-authorization false

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
mvn clean verify -Dazure.test=true
```

If you receive the similar error message `[ERROR] 'dependencies.dependency.version' for com.azure.microprofile:azure-microprofile-config-keyvault:jar is missing`, which means the `azure-microprofile-config-keyvault` extension is not available in the Maven repository, you need to build the extension locally and install it into your local Maven repository.

```
mvn clean install -DskipTests --file ../pom.xml
```

Then, run the integration test again.

```
mvn clean verify -Dazure.test=true
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
