// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.microprofile.config.keyvault;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureKeyVaultOperationTest {

    @Test
    void testToKeyVaultSecretNameWithNullInput() {
        assertNull(AzureKeyVaultOperation.toKeyVaultSecretName(null));
    }

    @Test
    void testToKeyVaultSecretNameWithValidInput() {
        assertEquals("my-secret", AzureKeyVaultOperation.toKeyVaultSecretName("my-secret"));
        assertEquals("mySecret", AzureKeyVaultOperation.toKeyVaultSecretName("mySecret"));
        assertEquals("my123Secret", AzureKeyVaultOperation.toKeyVaultSecretName("my123Secret"));
    }

    @Test
    void testToKeyVaultSecretNameWithDottedInput() {
        assertEquals("my-secret-name", AzureKeyVaultOperation.toKeyVaultSecretName("my.secret.name"));
        assertEquals("database-url", AzureKeyVaultOperation.toKeyVaultSecretName("database.url"));
    }

    @Test
    void testToKeyVaultSecretNameWithMixedSpecialCharacters() {
        assertEquals("my-secret-name", AzureKeyVaultOperation.toKeyVaultSecretName("my_secret.name"));
        assertEquals("app-config-value", AzureKeyVaultOperation.toKeyVaultSecretName("app/config@value"));
        assertEquals("special----chars", AzureKeyVaultOperation.toKeyVaultSecretName("special!@#$chars"));
    }

    @Test
    void testToKeyVaultSecretNameWithSpaces() {
        assertEquals("my-secret-name", AzureKeyVaultOperation.toKeyVaultSecretName("my secret name"));
        assertEquals("config-with-spaces", AzureKeyVaultOperation.toKeyVaultSecretName("config with spaces"));
    }

    @Test
    void testToKeyVaultSecretNameWithComplexProperty() {
        assertEquals("quarkus-datasource-default-jdbc-url", 
                AzureKeyVaultOperation.toKeyVaultSecretName("quarkus.datasource.default.jdbc.url"));
        assertEquals("mp-config-profile-dev-enabled", 
                AzureKeyVaultOperation.toKeyVaultSecretName("mp.config.profile.dev.enabled"));
    }
}