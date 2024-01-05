# Azure extension for MicroProfile Key Vault Custom ConfigSource

This library creates a config source (using the MicroProfile Config API) for Azure Key Vault. Developers can focus on configuration of the library and retrieving secrets stored in Azure Key Vault by using this library, and don't need to write any Azure-specific code.

## Configuration properties

The only required property to configure the library is `azure.keyvault.url`, which is the URL of the Azure Key Vault instance. Here are all supported properties:

Property name | Description | Type | Default value | Required
--- | --- | --- | ---
azure.keyvault.url | The URL of the Azure Key Vault instance. | String |  | true
azure.keyvault.cache | Whether to cache the secrets fetched from Azure Key Vault locally. | Boolean | false | false
azure.keyvault.cache.ttl | The time-to-live value (in milliseconds) for cache entries. It only takes effect when `azure.keyvault.cache` is set to `true`. | Long | 180000 (3 minutes) | false
azure.keyvault.secret-name-regex | The regular expression for matching the secret names to be fetched from Azure Key Vault. | String | ^[0-9a-zA-Z-]+$ | false

## Authentication

The library uses `DefaultAzureCredential` to authenticate with Azure Key Vault. See [Default Azure credential](https://learn.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth#default-azure-credential) for more details.

## Usage guide

TODO.
