package com.azure.microprofile.it.openliberty;

import com.azure.microprofile.config.keyvault.AzureKeyVaultConfigSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

@Path("/config")
public class ConfigResource {

    @Inject
    private Config config;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/value/{name}")
    public String getConfigValue(@PathParam("name") String name) {
        return config.getConfigValue(name).getValue();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/propertyNames")
    public Set<String> getConfigPropertyNames() {
        ConfigSource configSource = getConfigSource(AzureKeyVaultConfigSource.class.getSimpleName());
        return configSource.getPropertyNames();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/properties")
    public Map<String, String> getConfigProperties() {
        ConfigSource configSource = getConfigSource(AzureKeyVaultConfigSource.class.getSimpleName());
        return configSource.getProperties();
    }

    private ConfigSource getConfigSource(String name) {
        return StreamSupport.stream(config.getConfigSources().spliterator(), false)
                .filter(source -> source.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConfigSource not found: " + name));
    }
}
