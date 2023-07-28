package com.azure.microprofile.config.keyvault;

import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.util.Map;
import java.util.Set;

interface AzureKeyVaultOperation {
    String AZURE_MICROPROFILE_KEY_VAULT_SECRETS = "az-mp-kv-secrets";

    static SecretClient defaultSecretKeyVaultClient(String url) {
        return new SecretClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AZURE_MICROPROFILE_KEY_VAULT_SECRETS))
                .vaultUrl(url)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    Set<String> getPropertyNames();

    Map<String, String> getProperties();

    String getValue(String secretName);

}
