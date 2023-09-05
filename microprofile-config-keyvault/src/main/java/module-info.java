// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Module for implementing Azure Key Vault MicroProfile Custom ConfigSource.
 */

module com.azure.microprofile.config.keyvault {
    requires microprofile.config.api;
    requires com.azure.core;
    requires com.azure.identity;
    requires com.azure.security.keyvault.secrets;
    requires java.logging;

    exports com.azure.microprofile.config.keyvault;

    provides org.eclipse.microprofile.config.spi.ConfigSource with com.azure.microprofile.config.keyvault.AzureKeyVaultConfigSource;
}