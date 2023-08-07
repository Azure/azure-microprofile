package com.azure.microprofile.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusIntegrationTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class ConfigResourceIT {

    @Test
    public void testConfigResource() {
        given()
                .when().get("/config/secret")
                .then()
                .statusCode(200)
                .body(is("1234"));

        given()
                .when().get("/config/anotherSecret")
                .then()
                .statusCode(200)
                .body(is("5678"));

        given()
                .pathParam("name", UUID.randomUUID().toString())
                .when().get("/config/{name}")
                .then()
                .statusCode(204);
    }

}
