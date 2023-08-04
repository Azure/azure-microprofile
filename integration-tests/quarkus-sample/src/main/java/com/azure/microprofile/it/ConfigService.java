package com.azure.microprofile.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConfigService {

    @Inject
    @ConfigProperty(name = "secret", defaultValue = "UNKNOWN")
    Provider<String> secret;

    @Inject
    @ConfigProperty(name = "anotherSecret", defaultValue = "UNKNOWN")
    String anotherSecret;

    public String getSecret() {
        return secret.get();
    }

    public String getAnotherSecret() {
        return anotherSecret;
    }

}
