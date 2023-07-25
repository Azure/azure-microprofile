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
    private static final String SECRET_NAME_UPDATED = "my-secret-updated";
    private static final String SECRET_VALUE_UPDATED = "my-secret-value-updated";

    @Mock
    private SecretClient secretClient;

    private KeyVaultSecret keyVaultSecret = new KeyVaultSecret(SECRET_NAME, SECRET_VALUE);

    @Mock
    private PagedIterable<SecretProperties> secretPropertiesPagedIterable;

    private CachedAzureKeyVaultOperation operation;

    @BeforeEach
    void setUp() {
        operation = new CachedAzureKeyVaultOperation(secretClient, CACHE_REFRESH_INTERVAL);
    }

    @Test
    void testGetProperties() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        whenStubbing();
        Map<String, String> properties = operation.getProperties();
        assertEquals(Collections.singletonMap(SECRET_NAME, SECRET_VALUE), properties);
        verifyInvocation(1);

        // Update the secret value
        Field valueField = KeyVaultSecret.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(keyVaultSecret, SECRET_VALUE_UPDATED);

        // Second call before cache expires should not update the cache and return the same value
        properties = operation.getProperties();
        assertEquals(Collections.singletonMap(SECRET_NAME, SECRET_VALUE), properties);
        verifyInvocation(1);

        // Wait for cache to expire
        Thread.sleep(CACHE_REFRESH_INTERVAL);

        // Third call after cache expires should update the cache and return the updated value
        whenStubbing();
        properties = operation.getProperties();
        assertEquals(Collections.singletonMap(SECRET_NAME, SECRET_VALUE_UPDATED), properties);
        verifyInvocation(2);
    }

    @Test
    void testGetPropertyNames() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        whenStubbing();
        Set<String> propertyNames = operation.getPropertyNames();
        assertEquals(Collections.singleton(SECRET_NAME), propertyNames);
        verifyInvocation(1);

        // Update the secret properties
        KeyVaultSecret updated = new KeyVaultSecret(SECRET_NAME_UPDATED, SECRET_VALUE_UPDATED);
        Field propertiesField = KeyVaultSecret.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        propertiesField.set(keyVaultSecret, updated.getProperties());

        // Second call before cache expires should not update the cache and return the same property name
        propertyNames = operation.getPropertyNames();
        assertEquals(Collections.singleton(SECRET_NAME), propertyNames);
        verifyInvocation(1);

        // Wait for cache to expire
        Thread.sleep(CACHE_REFRESH_INTERVAL);

        // Third call after cache expires should update the cache and return the updated property name
        whenStubbing();
        propertyNames = operation.getPropertyNames();
        assertEquals(Collections.singleton(SECRET_NAME_UPDATED), propertyNames);
        verify(secretClient, times(2)).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable, times(2)).stream();
        verify(secretClient, times(1)).getSecret(SECRET_NAME);
        verify(secretClient, times(1)).getSecret(SECRET_NAME_UPDATED);
    }

    @Test
    void testGetValue() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        whenStubbing();
        String value = operation.getValue(SECRET_NAME);
        assertEquals(SECRET_VALUE, value);
        verifyInvocation(1);

        // Update the secret value
        Field valueField = KeyVaultSecret.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(keyVaultSecret, SECRET_VALUE_UPDATED);

        // Second call before cache expires should not update the cache and return the same value
        value = operation.getValue(SECRET_NAME);
        assertEquals(SECRET_VALUE, value);
        verifyInvocation(1);

        // Wait for cache to expire
        Thread.sleep(CACHE_REFRESH_INTERVAL);

        // Third call after cache expires should update the cache and return the updated value
        whenStubbing();
        value = operation.getValue(SECRET_NAME);
        assertEquals(SECRET_VALUE_UPDATED, value);
        verifyInvocation(2);
    }

    private void whenStubbing() {
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(keyVaultSecret.getProperties()));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(keyVaultSecret.getName())).thenReturn(keyVaultSecret);
    }

    private void verifyInvocation(int cnt) {
        verify(secretClient, times(cnt)).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable, times(cnt)).stream();
        verify(secretClient, times(cnt)).getSecret(SECRET_NAME);
    }
}