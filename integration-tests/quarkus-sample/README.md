# Quarkus sample

This is a Quarkus sample about implementing REST endpoints using the Custom ConfigSource to get the secrets stored in
Azure Key Vault.

## Prerequisites

To successfully run this sample, you need:

* JDK 11+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription

You also need to make sure the right version of dependencies are installed.

### Use development iteration version

By default, the sample depends on the development iteration version. To install the development iteration version, you
need to build it locally.

```
# Switch to the root directory of this repository.
# For example, if you are in the directory of azure-microprofile/integration-tests/quarkus-sample
cd ../..

# Install all Azure Extensions for MicroProfile locally.
mvn clean install -DskipTests

# Switch back to the directory of integration-tests/quarkus-sample
cd integration-tests/quarkus-sample
```

## Preparing the Azure services

The Custom ConfigSource implemented by `microprofile-config-keyvault` extension needs to connect to a real Azure Key Vault instance, follow steps below to create one.

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

Run the following commands to create an Azure Key Vault instance, add a few secrets, and export its uri as environment variables.

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
echo $AZURE_KEYVAULT_URL
```

If you just run the sample in the same shell session without further manual configuration, just go ahead to section [Running the sample](#running-the-sample). Otherwise, if you prefer to run the sample in another shell session, you need to edit the file `src/main/resources/META-INF/microprofile-config.properties` to update the value of the config property `azure.keyvault.url` to the value of `$AZURE_KEYVAULT_URL`.

* Open to edit the file `src/main/resources/META-INF/microprofile-config.properties`. 
* Uncomment line 2. Make it so the value of the config property `azure.keyvault.url` is the value of `$AZURE_KEYVAULT_URL`.
* You can also uncomment line 3 and line 4 which is optional.
  * Line 3 is to cache the secrets fetched from Azure Key Vault locally by setting `true` as the value of config property `azure.keyvault.cache`.
  * Line 4 is to config the time-to-live value (in milliseconds) for cache entries by setting config property `azure.keyvault.cache.ttl`. It only takes effect when `azure.keyvault.cache` is set to `true`.
* Save the file.

## Running the sample

Now you can launch the sample with:

```
mvn package quarkus:run
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Get the value of secret "secret" stored in the Azure key vault. You should see `1234` in the response.
echo $(curl http://localhost:8080/config/value/secret -X GET)

# Get the value of secret "anotherSecret" stored in the Azure key vault. You should see `5678` in the response.
echo $(curl http://localhost:8080/config/value/anotherSecret -X GET)

# Get the names of secrets stored in the Azure key vault. You should see `["anotherSecret","secret"]` in the response.
echo $(curl http://localhost:8080/config/propertyNames -X GET)

# Get the name-value paris of secrets stored in the Azure key vault. You should see `{"anotherSecret":"5678","secret":"1234"}` in the response.
echo $(curl http://localhost:8080/config/properties -X GET)
```

Press `Ctrl + C` to stop the sample once you complete the try and test.

## Cleaning up Azure resources

Run the following command to clean up the Azure resources created before:

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
