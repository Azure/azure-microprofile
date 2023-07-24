package com.azure.microprofile.config.keyvault;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.util.Map;
import java.util.Set;

interface AzureKeyVaultOperation {

    static SecretClient defaultSecretKeyVaultClient(String url) {
        return new SecretClientBuilder().vaultUrl(url)
                .credential(new DefaultAzureCredentialBuilder().build()).buildClient();
    }

    Set<String> getPropertyNames();

    Map<String, String> getProperties();

    String getValue(String secretName);

}
