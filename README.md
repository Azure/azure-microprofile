# Azure Extensions for MicroProfile

[![Build](https://github.com/Azure/azure-microprofile/workflows/Build/badge.svg?branch=main)](https://github.com/Azure/azure-microprofile/actions?query=workflow%3ABuild)

This repository hosts Azure extensions for [MicroProfile](https://microprofile.io/), which are implemented using different Azure services.

## Azure Services

The following extensions allows you to interact with some of the Azure Services:

* [Config Source for Azure Key Vault](config-keyvault): [Azure Key Vault](https://azure.microsoft.com/services/key-vault/) is a cloud service for securely storing and accessing secrets.

## Sample applications

Sample applications can be found inside the [integration-tests](integration-tests) folder:

* [Open Liberty sample](integration-tests/open-liberty-sample): This sample demonstrates how to configure an [Open Liberty](https://openliberty.io/) application to retrieve secrets from Azure Key Vault using the [MicroProfile Config APIs](https://microprofile.io/specifications/microprofile-config/) to create a direct connection to Azure Key Vault.

* [Quarkus sample](integration-tests/quarkus-sample): This sample demonstrates how to configure a [Quarkus](https://quarkus.io/) application to retrieve secrets from Azure Key Vault using the [MicroProfile Config APIs](https://microprofile.io/specifications/microprofile-config/) to create a direct connection to Azure Key Vault.

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## License

Azure Extensions for MicroProfile is licensed under the [MIT](https://github.com/Azure/azure-microprofile/blob/main/LICENSE) license.
