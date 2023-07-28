package com.azure.microprofile.config.keyvault;

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
import java.util.logging.Logger;


class CachedAzureKeyVaultOperation implements AzureKeyVaultOperation {
    private static final Logger log = Logger.getLogger(CachedAzureKeyVaultOperation.class.getName());
    private static final long DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS = 180000L; // 3 minutes

    private final long cacheRefreshIntervalInMs;
    private final SecretClient secretKeyVaultClient;

    private final Map<String, String> propertiesMap = new ConcurrentHashMap<>();
    private final AtomicLong lastUpdateTime = new AtomicLong();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    CachedAzureKeyVaultOperation(String url, Long cacheRefreshIntervalInMs) {
        this(AzureKeyVaultOperation.defaultSecretKeyVaultClient(url), Optional.ofNullable(cacheRefreshIntervalInMs).orElse(DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS));
    }

    CachedAzureKeyVaultOperation(SecretClient secretKeyVaultClient, Long cacheRefreshIntervalInMs) {
        this.secretKeyVaultClient = secretKeyVaultClient;
        this.cacheRefreshIntervalInMs = cacheRefreshIntervalInMs;
    }

    public Map<String, String> getProperties() {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return Collections.unmodifiableMap(propertiesMap);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Set<String> getPropertyNames() {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return propertiesMap.keySet();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public String getValue(String secretName) {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return propertiesMap.getOrDefault(secretName, null);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void checkRefreshTimeOut() {
        // refresh periodically
        if (System.currentTimeMillis() - lastUpdateTime.get() > cacheRefreshIntervalInMs) {
            createOrUpdateHashMap();
        }
    }

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
                log.fine(() -> "createOrUpdateHashMap() updated the cache at " + DateFormat.getDateTimeInstance().format(lastUpdateTime.get()));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}
