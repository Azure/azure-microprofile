package com.azure.microprofile.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.Config;

@Path("/config")
public class ConfigResource {

    @Inject
    private Config config;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{name}")
    public String getConfigValue(String name) {
        return config.getConfigValue(name).getValue();
    }
}