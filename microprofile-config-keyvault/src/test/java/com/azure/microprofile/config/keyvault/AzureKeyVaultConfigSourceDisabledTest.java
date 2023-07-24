package com.azure.microprofile.config.keyvault;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class AzureKeyVaultConfigSourceDisabledTest {

    @Mock
    AzureKeyVaultOperation keyVaultOperation;
    @InjectMocks
    AzureKeyVaultConfigSource azureKeyVaultConfigSource;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field isKeyVaultEnabledField = AzureKeyVaultConfigSource.class.getDeclaredField("isKeyVaultEnabled");
        isKeyVaultEnabledField.setAccessible(true);
        isKeyVaultEnabledField.setBoolean(azureKeyVaultConfigSource, false);
    }

    @Test
    void testGetProperties() {
        Assertions.assertEquals(Collections.emptyMap(), azureKeyVaultConfigSource.getProperties());
    }

    @Test
    void testGetPropertyNames() {
        Assertions.assertEquals(Collections.emptySet(), azureKeyVaultConfigSource.getPropertyNames());
    }

    @Test
    void testGetValue() {
        Assertions.assertEquals(null, azureKeyVaultConfigSource.getValue("key"));
    }

    @Test
    void testGetName() {
        Assertions.assertEquals(AzureKeyVaultConfigSource.class.getSimpleName(), azureKeyVaultConfigSource.getName());
    }

    @Test
    void testGetOrdinal() {
        Assertions.assertEquals(90, azureKeyVaultConfigSource.getOrdinal());
    }
}