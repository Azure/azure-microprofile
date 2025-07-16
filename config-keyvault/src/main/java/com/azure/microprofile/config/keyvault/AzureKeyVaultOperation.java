// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.microprofile.config.keyvault;

import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.util.Map;
import java.util.Set;

/**
 * Azure Key Vault operation interface.
 */
interface AzureKeyVaultOperation {
    /**
     * Azure MicroProfile Key Vault Secret application id.
     */
    String AZURE_MICROPROFILE_KEY_VAULT_SECRETS = "az-mp-kv-secrets";

    /**
     * Create a default secret key vault client.
     *
     * @param url the key vault url
     * @return the secret key vault client
     * @implNote The default secret key vault client will use the {@link com.azure.identity.DefaultAzureCredential} for authentication.
     */
    static SecretClient defaultSecretKeyVaultClient(String url) {
        return new SecretClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AZURE_MICROPROFILE_KEY_VAULT_SECRETS))
                .vaultUrl(url)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    /**
     * Convert a property name to a Key Vault compatible secret name.
     * Key Vault secret names can only contain 0-9, a-z, A-Z, and - characters.
     * This method replaces all other characters with dashes.
     *
     * @param propertyName the property name to convert
     * @return the Key Vault compatible secret name
     */
    static String toKeyVaultSecretName(String propertyName) {
        if (propertyName == null) {
            return null;
        }
        return propertyName.replaceAll("[^0-9a-zA-Z-]", "-");
    }

    /**
     * Get secret names from Azure Key Vault.
     *
     * @return Name {@link Set} of secrets.
     */
    Set<String> getPropertyNames();

    /**
     * Get secrets from Azure Key Vault.
     *
     * @return Name/value {@link Map} of secrets.
     */
    Map<String, String> getProperties();

    /**
     * Get secret value from Azure Key Vault.
     *
     * @param secretName Secret name.
     * @return Secret value.
     */
    String getValue(String secretName);

}
