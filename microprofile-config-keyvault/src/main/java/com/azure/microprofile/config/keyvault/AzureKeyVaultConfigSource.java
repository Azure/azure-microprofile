package com.azure.microprofile.config.keyvault;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Custom ConfigSource for Azure Key Vault.
 */
public class AzureKeyVaultConfigSource implements ConfigSource {

    private static final Logger log = Logger.getLogger(AzureKeyVaultConfigSource.class.getName());
    private AzureKeyVaultOperation keyVaultOperation;

    private boolean isKeyVaultEnabled = false;

    /**
     * Default constructor.
     */
    public AzureKeyVaultConfigSource() {
        this(null, getConfig());
    }

    /**
     * Constructor with {@link AzureKeyVaultOperation} instance and {@link Config} instance. This constructor is used for testing.
     *
     * @param keyVaultOperation {@link AzureKeyVaultOperation} instance.
     * @param config            {@link Config} instance.
     */
    AzureKeyVaultConfigSource(AzureKeyVaultOperation keyVaultOperation, Config config) {
        init(keyVaultOperation, config);
    }

    /**
     * Get {@link Config} instance with default sources.
     *
     * @return {@link Config} instance.
     */
    static private Config getConfig() {
        return ConfigProviderResolver.instance()
                .getBuilder()
                .addDefaultSources()
                .build();
    }

    /**
     * Initialize {@link AzureKeyVaultOperation} based on configuration.
     *
     * <ul>
     *  <li>If {@code azure.keyvault.url} is not set, then {@link AzureKeyVaultOperation} will not be initialized.</li>
     *  <li>
     *      If {@code azure.keyvault.cache} is set to {@code true}, then {@link CachedAzureKeyVaultOperation} will be used.
     *      <ul>
     *          <li>If {@code azure.keyvault.cache.ttl} is set, then it will be used as TTL for cache entries.</li>
     *          <li>Otherwise, CachedAzureKeyVaultOperation.DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS will be used as TTL for cache entries.</li>
     *      </ul>
     *  </li>
     *  <li>
     *      Otherwise, {@link NoCacheAzureKeyVaultOperation} will be used.
     *      <ul>
     *          <li>If {@code azure.keyvault.secret-name-regex} is set, then it will be used to filter secret names.</li>
     *          <li>Otherwise, NoCacheAzureKeyVaultOperation.DEFAULT_SECRET_NAME_REGEX will be used to filter secret names.</li>
     *      </ul>
     *  </li>
     * </ul>
     *
     * @param keyVaultOperation {@link AzureKeyVaultOperation} instance.
     * @param config            {@link Config} instance.
     */
    private void init(AzureKeyVaultOperation keyVaultOperation, Config config) {
        if (keyVaultOperation != null) {
            this.keyVaultOperation = keyVaultOperation;
            isKeyVaultEnabled = true;
            return;
        }

        String url = config.getOptionalValue("azure.keyvault.url", String.class).orElse("");
        isKeyVaultEnabled = !url.isEmpty();

        if (!isKeyVaultEnabled) {
            log.warning("Azure Key Vault ConfigSource is not enabled. Please set 'azure.keyvault.url' in your configuration.");
            return;
        }

        boolean cached = config.getOptionalValue("azure.keyvault.cache", Boolean.class).orElse(Boolean.FALSE);
        if (cached) {
            Long ttl = config.getOptionalValue("azure.keyvault.cache.ttl", Long.class).orElse(null);
            this.keyVaultOperation = new CachedAzureKeyVaultOperation(url, ttl);
        } else {
            String regex = config.getOptionalValue("azure.keyvault.secret-name-regex", String.class).orElse(null);
            this.keyVaultOperation = new NoCacheAzureKeyVaultOperation(url, regex);
        }
    }

    /**
     * Get secrets from Azure Key Vault.
     *
     * @return Name/value {@link Map} of secrets.
     */
    @Override
    public Map<String, String> getProperties() {
        return isKeyVaultEnabled ? keyVaultOperation.getProperties() : Collections.emptyMap();
    }

    /**
     * Get secret names from Azure Key Vault.
     *
     * @return {@link Set} of secret names.
     */
    @Override
    public Set<String> getPropertyNames() {
        return isKeyVaultEnabled ? keyVaultOperation.getPropertyNames() : Collections.emptySet();
    }

    /**
     * Get secret value from Azure Key Vault.
     *
     * @param key Secret name.
     * @return Secret value.
     */
    @Override
    public String getValue(String key) {
        return isKeyVaultEnabled ? keyVaultOperation.getValue(key) : null;
    }

    /**
     * Get name of this {@link ConfigSource}.
     *
     * @return Name of this {@link ConfigSource}.
     */
    @Override
    public String getName() {
        return AzureKeyVaultConfigSource.class.getSimpleName();
    }

    /**
     * Get ordinal of this {@link ConfigSource}.
     *
     * @return Ordinal of this {@link ConfigSource}.
     */
    @Override
    public int getOrdinal() {
        return 90;
    }
}
