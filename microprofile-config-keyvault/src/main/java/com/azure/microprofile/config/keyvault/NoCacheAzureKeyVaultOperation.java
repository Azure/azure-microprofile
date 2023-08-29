package com.azure.microprofile.config.keyvault;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class is used to fetch the secrets from Azure Key Vault.
 *
 * <ul>
 *     <li>It does not cache the secrets. It fetches the secrets from Azure Key Vault every time.</li>
 *     <li>A secret name regular expression is used to filter out invalid secret names to reduce unnecessary calls to Azure Key Vault.</li>
 *     <li>The default value of the secret name regular expression is "^[0-9a-zA-Z-]+$".</li>
 * </ul>
 */
class NoCacheAzureKeyVaultOperation implements AzureKeyVaultOperation {
    private static final String DEFAULT_SECRET_NAME_REGEX = "^[0-9a-zA-Z-]+$";
    private static final Logger log = Logger.getLogger(NoCacheAzureKeyVaultOperation.class.getName());
    private final String secretNameRegex;
    private final SecretClient secretKeyVaultClient;

    /**
     * Constructor of NoCacheAzureKeyVaultOperation.
     *
     * @param url   URL of Azure Key Vault
     * @param regex secret name regular expression
     */
    NoCacheAzureKeyVaultOperation(String url, String regex) {
        this(AzureKeyVaultOperation.defaultSecretKeyVaultClient(url), Optional.ofNullable(regex).orElse(DEFAULT_SECRET_NAME_REGEX));
    }

    /**
     * Constructor of NoCacheAzureKeyVaultOperation.
     *
     * @param secretKeyVaultClient SecretClient of Azure Key Vault
     * @param secretNameRegex      secret name regular expression
     */
    NoCacheAzureKeyVaultOperation(SecretClient secretKeyVaultClient, String secretNameRegex) {
        this.secretKeyVaultClient = secretKeyVaultClient;
        this.secretNameRegex = secretNameRegex;
    }

    /**
     * Get secrets from Azure Key Vault.
     *
     * @return Name/value {@link Map} of secrets.
     */
    public Map<String, String> getProperties() {
        Map<String, String> propertiesMap = new HashMap<>();
        secretKeyVaultClient.listPropertiesOfSecrets()
                .stream()
                .map(SecretProperties::getName)
                .forEach(prop -> propertiesMap.put(prop, secretKeyVaultClient.getSecret(prop).getValue()));

        return propertiesMap;
    }

    /**
     * Get secret names from Azure Key Vault.
     *
     * @return Name {@link Set} of secrets.
     */
    public Set<String> getPropertyNames() {
        Set<String> keysSet = new TreeSet<>();
        secretKeyVaultClient.listPropertiesOfSecrets()
                .stream()
                .map(SecretProperties::getName)
                .forEach(keysSet::add);

        return keysSet;
    }

    /**
     * Get secret value from Azure Key Vault.
     *
     * @param secretName Secret name.
     * @return Secret value if secretName is valid and exists; otherwise, null.
     */
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
