package com.azure.microprofile.config.keyvault;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;


class NoCacheAzureKeyVaultOperation implements AzureKeyVaultOperation {
    private static final Logger log = Logger.getLogger(NoCacheAzureKeyVaultOperation.class.getName());
    private static final String DEFAULT_SECRET_NAME_REGEX = "^[0-9a-zA-Z-]+$";
    private final String secretNameRegex;
    private final SecretClient secretKeyVaultClient;

    NoCacheAzureKeyVaultOperation(String url, String regex) {
        this(AzureKeyVaultOperation.defaultSecretKeyVaultClient(url), Optional.ofNullable(regex).orElse(DEFAULT_SECRET_NAME_REGEX));
    }

    NoCacheAzureKeyVaultOperation(SecretClient secretKeyVaultClient, String secretNameRegex) {
        this.secretKeyVaultClient = secretKeyVaultClient;
        this.secretNameRegex = secretNameRegex;
    }

    public Map<String, String> getProperties() {
        Map<String, String> propertiesMap = new HashMap<>();
        secretKeyVaultClient.listPropertiesOfSecrets()
                .stream()
                .map(SecretProperties::getName)
                .forEach(prop -> propertiesMap.put(prop, secretKeyVaultClient.getSecret(prop).getValue()));

        return propertiesMap;
    }

    public Set<String> getPropertyNames() {
        Set<String> keysSet = new TreeSet<>();
        secretKeyVaultClient.listPropertiesOfSecrets()
                .stream()
                .map(SecretProperties::getName)
                .forEach(keysSet::add);

        return keysSet;
    }

    public String getValue(String secretName) {
        // Check if secretName is valid using regex secretNameRegex
        // The goal is to bypass unnecessary calls to Azure Key Vault especially there are lots of properties from other sources
        if (!secretName.matches(secretNameRegex)) {
            log.fine(() -> MessageFormat.format("getValue() failed with exception: secretName {0} does not match regex {1}",
                    secretName, secretNameRegex));
            return null;
        }

        try {
            return secretKeyVaultClient.getSecret(secretName).getValue();
        } catch (Exception e) {
            log.info(() -> "getValue() failed with exception: " + e.getMessage());
            return null;
        }
    }

}
