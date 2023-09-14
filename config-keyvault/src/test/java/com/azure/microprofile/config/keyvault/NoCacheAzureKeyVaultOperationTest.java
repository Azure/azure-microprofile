// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoCacheAzureKeyVaultOperationTest {

    private static final String SECRET_NAME_REGEX = "^[0-9a-zA-Z-]+$";
    private static final String SECRET_NAME = "my-secret";
    private static final String SECRET_VALUE = "my-secret-value";

    @Mock
    private SecretClient secretClient;

    @Mock
    private KeyVaultSecret keyVaultSecret;

    @Mock
    private SecretProperties secretProperties;

    @Mock
    private PagedIterable<SecretProperties> secretPropertiesPagedIterable;

    private NoCacheAzureKeyVaultOperation operation;

    @BeforeEach
    void setUp() {
        operation = new NoCacheAzureKeyVaultOperation(secretClient, SECRET_NAME_REGEX);
    }

    @Test
    void testGetProperties() {
        when(secretProperties.getName()).thenReturn(SECRET_NAME);
        when(secretPropertiesPagedIterable.stream()).thenReturn(Stream.of(secretProperties));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(secretPropertiesPagedIterable);
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);
        when(keyVaultSecret.getValue()).thenReturn(SECRET_VALUE);

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

        Set<String> propertyNames = operation.getPropertyNames();

        assertEquals(Collections.singleton(SECRET_NAME), propertyNames);
        verify(secretClient).listPropertiesOfSecrets();
        verify(secretPropertiesPagedIterable).stream();
    }

    @Test
    void testGetValueWithValidSecretName() {
        when(secretClient.getSecret(SECRET_NAME)).thenReturn(keyVaultSecret);
        when(keyVaultSecret.getValue()).thenReturn(SECRET_VALUE);

        String value = operation.getValue(SECRET_NAME);

        assertEquals(SECRET_VALUE, value);
        verify(secretClient).getSecret(SECRET_NAME);
        verify(keyVaultSecret).getValue();
    }

    @Test
    void testGetValueWithInvalidSecretName() {
        String invalidSecretName = "invalid.secret.name";

        String value = operation.getValue(invalidSecretName);

        assertEquals(null, value);
        verify(secretClient, never()).getSecret(invalidSecretName);
    }
}