// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.microprofile.config.keyvault;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to fetch and cache the secrets from Azure Key Vault.
 *
 * <ul>
 *  <li>The cache is refreshed if it is hit but expired after the specified cacheRefreshIntervalInMs.</li>
 *  <li>The default value of cacheRefreshIntervalInMs is 3 minutes.</li>
 * </ul>
 */
class CachedAzureKeyVaultOperation implements AzureKeyVaultOperation {
    private static final long DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS = 180000L; // 3 minutes
    private static final ClientLogger LOGGER = new ClientLogger(CachedAzureKeyVaultOperation.class);
    private final long cacheRefreshIntervalInMs;
    private final SecretClient secretKeyVaultClient;

    private final Map<String, String> propertiesMap = new ConcurrentHashMap<>();
    private final AtomicLong lastUpdateTime = new AtomicLong();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Constructor of CachedAzureKeyVaultOperation.
     *
     * @param url                      URL of Azure Key Vault
     * @param cacheRefreshIntervalInMs cache refresh interval in milliseconds
     */
    CachedAzureKeyVaultOperation(String url, Long cacheRefreshIntervalInMs) {
        this(AzureKeyVaultOperation.defaultSecretKeyVaultClient(url), Optional.ofNullable(cacheRefreshIntervalInMs).orElse(DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS));
    }

    /**
     * Constructor of CachedAzureKeyVaultOperation.
     *
     * @param secretKeyVaultClient     SecretClient of Azure Key Vault
     * @param cacheRefreshIntervalInMs cache refresh interval in milliseconds
     */
    CachedAzureKeyVaultOperation(SecretClient secretKeyVaultClient, Long cacheRefreshIntervalInMs) {
        this.secretKeyVaultClient = secretKeyVaultClient;
        this.cacheRefreshIntervalInMs = cacheRefreshIntervalInMs;
    }

    /**
     * Get secrets from Azure Key Vault.
     *
     * @return Name/value {@link Map} of secrets.
     * @implNote This method is thread-safe. It uses {@link ReadWriteLock} to protect the {@link #propertiesMap}. This method will refresh the cache if the cache is expired.
     */
    public Map<String, String> getProperties() {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return Collections.unmodifiableMap(propertiesMap);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Get secret names from Azure Key Vault.
     *
     * @return Name {@link Set} of secrets.
     * @implNote This method is thread-safe. It uses {@link ReadWriteLock} to protect the {@link #propertiesMap}. This method will refresh the cache if the cache is expired.
     */
    public Set<String> getPropertyNames() {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return propertiesMap.keySet();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Get secret value from Azure Key Vault.
     *
     * @param secretName Secret name.
     * @return Secret value.
     * @implNote This method is thread-safe. It uses {@link ReadWriteLock} to protect the {@link #propertiesMap}. This method will refresh the cache if the cache is expired.
     */
    public String getValue(String secretName) {
        if (secretName == null) {
            return null;
        }

        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            
            // Try exact match first
            String value = propertiesMap.get(secretName);
            if (value != null) {
                return value;
            }

            // Try remapped key (replace non-alphanumeric/dash characters with dash)
            String remappedSecretName = AzureKeyVaultOperation.toKeyVaultSecretName(secretName);
            if (!remappedSecretName.equals(secretName)) {
                return propertiesMap.get(remappedSecretName);
            }

            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Check if the cache is expired. If it is expired, refresh the cache.
     */
    private void checkRefreshTimeOut() {
        // refresh periodically
        if (System.currentTimeMillis() - lastUpdateTime.get() > cacheRefreshIntervalInMs) {
            createOrUpdateHashMap();
        }
    }

    /**
     * Refresh the cache.
     *
     * @implNote This method is thread-safe. It uses {@link ReadWriteLock} to protect the {@link #propertiesMap}.
     */
    private void createOrUpdateHashMap() {
        try {
            rwLock.writeLock().lock();
            if (System.currentTimeMillis() - lastUpdateTime.get() > cacheRefreshIntervalInMs) {
                propertiesMap.clear();

                secretKeyVaultClient.listPropertiesOfSecrets()
                        .stream()
                        .map(SecretProperties::getName)
                        .forEach(key -> propertiesMap.put(key, secretKeyVaultClient.getSecret(key).getValue()));
                lastUpdateTime.set(System.currentTimeMillis());
                LOGGER.log(LogLevel.VERBOSE, () -> "createOrUpdateHashMap() updated the cache at " + DateFormat.getDateTimeInstance().format(lastUpdateTime.get()));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}
