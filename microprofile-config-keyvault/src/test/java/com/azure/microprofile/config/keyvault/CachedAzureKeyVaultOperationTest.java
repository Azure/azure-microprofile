package com.azure.microprofile.config.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachedAzureKeyVaultOperationTest {

    private static final Long CACHE_REFRESH_INTERVAL = 10000L;
    private static final String SECRET_NAME = "my-secret";
    private static final String SECRET_VALUE = "my-secret-value";
    private static final String SECRET_VALUE_UPDATED = "my-secret-value-updated";

    @Mock
    private SecretClient secretClient;

    private KeyVaultSecret keyVaultSecret = new KeyVaultSecret(SECRET_NAME, SECRET_VALUE);

    @Mock
    private SecretProperties secretProperties;

    @Mock
    private PagedIterable<SecretProperties> secretPropertiesPagedIterable;

    private CachedAzureKeyVaultOperation operation;

    @BeforeEach
    void setUp() {
        operation = new CachedAzureKeyVaultOperation(secretClient, CACHE_REFRESH_INTERVAL);
    }

    @Test
    void testGetProperties() {
        when(secretProperties.getName()).thenReturn(SECRET_NAME);
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(secretProperties));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);

        Map<String, String> properties = operation.getProperties();
        assertEquals(Collections.singletonMap(SECRET_NAME, SECRET_VALUE), properties);

        verify(secretClient).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable).stream();
        verify(secretClient).getSecret(SECRET_NAME);
    }

    @Test
    void testGetPropertyNames() {
        when(secretProperties.getName()).thenReturn(SECRET_NAME);
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(secretProperties));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);

        Set<String> propertyNames = operation.getPropertyNames();
        assertEquals(Collections.singleton(SECRET_NAME), propertyNames);

        verify(secretClient).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable).stream();
        verify(secretClient).getSecret(SECRET_NAME);
    }

    @Test
    void testGetValue() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        when(secretProperties.getName()).thenReturn(SECRET_NAME);
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(secretProperties));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);

        String value = operation.getValue(SECRET_NAME);

        assertEquals(SECRET_VALUE, value);
        verify(secretClient).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable).stream();
        verify(secretClient).getSecret(SECRET_NAME);

        // Update the secret value
        Field valueField = KeyVaultSecret.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(keyVaultSecret, SECRET_VALUE_UPDATED);

        // Second call before cache expires should not update the cache and return the same value
        when(secretProperties.getName()).thenReturn(SECRET_NAME);
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(secretProperties));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);

        value = operation.getValue(SECRET_NAME);

        assertEquals(SECRET_VALUE, value);
        verify(secretClient).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable).stream();
        verify(secretClient).getSecret(SECRET_NAME);

        // Wait for cache to expire
        Thread.sleep(CACHE_REFRESH_INTERVAL);

        // Third call after cache expires should update the cache and return the updated value
        when(secretProperties.getName()).thenReturn(SECRET_NAME);
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(secretProperties));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);

        value = operation.getValue(SECRET_NAME);
        
        assertEquals(SECRET_VALUE_UPDATED, value);
        verify(secretClient, times(2)).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable, times(2)).stream();
        verify(secretClient, times(2)).getSecret(SECRET_NAME);
    }
}