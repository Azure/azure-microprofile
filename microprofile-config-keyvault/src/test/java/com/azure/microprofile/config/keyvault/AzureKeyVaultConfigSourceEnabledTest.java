package com.azure.microprofile.config.keyvault;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureKeyVaultConfigSourceEnabledTest {

    @Mock
    AzureKeyVaultOperation keyVaultOperation;

    @Mock
    Config config;
    
    @InjectMocks
    AzureKeyVaultConfigSource azureKeyVaultConfigSource;

    @Test
    void testGetProperties() {
        when(keyVaultOperation.getProperties()).thenReturn(new HashMap<>() {{
            put("String", "String");
        }});

        Assertions.assertEquals(new HashMap<String, String>() {{
            put("String", "String");
        }}, azureKeyVaultConfigSource.getProperties());
        verify(keyVaultOperation).getProperties();
    }

    @Test
    void testGetPropertyNames() {
        when(keyVaultOperation.getPropertyNames()).thenReturn(new HashSet<>(List.of("String")));

        Assertions.assertEquals(new HashSet<>(Collections.singletonList("String")), azureKeyVaultConfigSource.getPropertyNames());
        verify(keyVaultOperation).getPropertyNames();
    }

    @Test
    void testGetValue() {
        when(keyVaultOperation.getValue(anyString())).thenReturn("getValueResponse");

        Assertions.assertEquals("getValueResponse", azureKeyVaultConfigSource.getValue("key"));
        verify(keyVaultOperation).getValue(anyString());
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
