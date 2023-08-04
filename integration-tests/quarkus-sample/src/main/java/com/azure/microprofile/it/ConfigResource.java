package com.azure.microprofile.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/config")
public class ConfigResource {

    @Inject
    ConfigService configService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{name}")
    public String getConfigValue(String name) {
        if (name.equals("secret")) {
            return configService.getSecret();
        } else if (name.equals("anotherSecret")) {
            return configService.getAnotherSecret();
        } else {
            throw new NotFoundException("Not found for " + name);
        }
    }
}