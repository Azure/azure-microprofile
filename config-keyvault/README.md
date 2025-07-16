# Azure extension for MicroProfile Key Vault Custom ConfigSource

This library creates a config source (using the MicroProfile Config API) for Azure Key Vault. Developers can focus on configuration of the library and retrieving secrets stored in Azure Key Vault by using this library, and don't need to write any Azure-specific code.

## Configuration properties

The only required property to configure the library is `azure.keyvault.url`, which is the URL of the Azure Key Vault instance. Here are all supported properties:

Property name | Description | Type | Default value | Required
--- | --- | --- | --- | ---
azure.keyvault.url | The URL of the Azure Key Vault instance. | String |  | true
azure.keyvault.cache | Whether to cache the secrets fetched from Azure Key Vault locally. | Boolean | true | false
azure.keyvault.cache.ttl | The time-to-live value (in milliseconds) for cache entries. It only takes effect when `azure.keyvault.cache` is set to `true`. | Long | 180000 (3 minutes) | false
azure.keyvault.secret-name-regex | The regular expression for matching the secret names to be fetched from Azure Key Vault. It only takes effect when `azure.keyvault.cache` is set to `false`. | String | ^[0-9a-zA-Z-]+$ | false

## Key name mapping

Azure Key Vault secret names can only contain alphanumeric characters (0-9, a-z, A-Z) and dashes (-). To support property names with other characters (like dots, underscores, etc.), the library automatically maps property names to Key Vault compatible secret names using the following rules:

1. **Exact match**: First tries to find a secret with the exact property name
2. **Character replacement**: If no exact match is found, replaces all non-alphanumeric and non-dash characters with dashes and tries again

For example:
- `my.secret.name` → `my-secret-name`
- `database.url` → `database-url`
- `app/config@value` → `app-config-value`
- `quarkus.datasource.default.jdbc.url` → `quarkus-datasource-default-jdbc-url`

This allows you to use standard configuration property naming conventions (like dotted names) while still being able to store and retrieve the values from Azure Key Vault.

## Authentication

The library uses `DefaultAzureCredential` to authenticate with Azure Key Vault. See [Default Azure credential](https://learn.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth#default-azure-credential) for more details on how to configure the authentication.

## Usage guide

See guide [Configure MicroProfile with Azure Key Vault](https://learn.microsoft.com/azure/developer/java/eclipse-microprofile/configure-microprofile-with-keyvault) for how to use the library in a sample app, run locally and run on Azure Container Apps.
