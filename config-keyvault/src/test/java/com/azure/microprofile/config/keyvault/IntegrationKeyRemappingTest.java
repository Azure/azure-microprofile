// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.microprofile.config.keyvault;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationKeyRemappingTest {

    @Mock
    private AzureKeyVaultOperation mockOperation;

    @Test
    void testEndToEndKeyRemappingWithConfigSource() {
        // Mock the operation to return values for remapped keys
        Map<String, String> mockProperties = new HashMap<>();
        mockProperties.put("my-secret-name", "my-secret-value");
        mockProperties.put("database-url", "jdbc:postgresql://localhost:5432/mydb");
        mockProperties.put("app-config-value", "production");

        when(mockOperation.getValue("my.secret.name")).thenReturn("my-secret-value");
        when(mockOperation.getValue("database.url")).thenReturn("jdbc:postgresql://localhost:5432/mydb");
        when(mockOperation.getValue("app/config@value")).thenReturn("production");
        when(mockOperation.getValue("nonexistent.key")).thenReturn(null);

        // Create config source with the mocked operation
        AzureKeyVaultConfigSource configSource = new AzureKeyVaultConfigSource(mockOperation, null);

        // Test that dotted property names work
        assertEquals("my-secret-value", configSource.getValue("my.secret.name"));
        assertEquals("jdbc:postgresql://localhost:5432/mydb", configSource.getValue("database.url"));
        assertEquals("production", configSource.getValue("app/config@value"));
        assertNull(configSource.getValue("nonexistent.key"));
    }

    @Test
    void testKeyRemappingExamples() {
        // Test the key remapping utility method with the examples from the documentation
        assertEquals("my-secret-name", AzureKeyVaultOperation.toKeyVaultSecretName("my.secret.name"));
        assertEquals("database-url", AzureKeyVaultOperation.toKeyVaultSecretName("database.url"));
        assertEquals("app-config-value", AzureKeyVaultOperation.toKeyVaultSecretName("app/config@value"));
        assertEquals("quarkus-datasource-default-jdbc-url", 
            AzureKeyVaultOperation.toKeyVaultSecretName("quarkus.datasource.default.jdbc.url"));
    }
}