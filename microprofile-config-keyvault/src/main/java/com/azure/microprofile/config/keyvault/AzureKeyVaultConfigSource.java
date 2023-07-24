package com.azure.microprofile.config.keyvault;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AzureKeyVaultConfigSource implements ConfigSource {

    private AzureKeyVaultOperation keyVaultOperation;

    private boolean isKeyVaultEnabled = true;

    public AzureKeyVaultConfigSource() {
        // no op
    }

    private void init() {
        if (keyVaultOperation != null) {
            return;
        }

        Config config = getConfig();
        String url = config.getOptionalValue("azure.keyvault.url", String.class).orElse("");
        isKeyVaultEnabled = !url.isEmpty();

        if (!isKeyVaultEnabled) {
            return;
        }

        boolean cached = config.getOptionalValue("azure.keyvault.cache", Boolean.class).orElse(Boolean.FALSE);
        if (cached) {
            Long ttl = config.getOptionalValue("azure.keyvault.cache.ttl", Long.class).orElse(null);
            keyVaultOperation = new CachedAzureKeyVaultOperation(url, ttl);
        } else {
            String regex = config.getOptionalValue("azure.keyvault.secret-name-regex", String.class).orElse(null);
            keyVaultOperation = new NoCacheAzureKeyVaultOperation(url, regex);
        }
    }

    private Config getConfig() {
        return ConfigProviderResolver.instance()
                .getBuilder()
                .addDefaultSources()
                .build();
    }

    @Override
    public Map<String, String> getProperties() {
        init();
        return isKeyVaultEnabled ? keyVaultOperation.getProperties() : Collections.emptyMap();
    }

    @Override
    public Set<String> getPropertyNames() {
        init();
        return isKeyVaultEnabled ? keyVaultOperation.getPropertyNames() : Collections.emptySet();
    }

    @Override
    public String getValue(String key) {
        init();
        return isKeyVaultEnabled ? keyVaultOperation.getValue(key) : null;
    }

    @Override
    public String getName() {
        return AzureKeyVaultConfigSource.class.getSimpleName();
    }

    @Override
    public int getOrdinal() {
        return 90;
    }
}
