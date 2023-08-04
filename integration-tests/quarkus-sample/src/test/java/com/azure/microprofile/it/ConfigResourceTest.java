package com.azure.microprofile.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ConfigResourceTest {

    @Test
    public void testGetSecret() {
        given()
                .when().get("/config/secret")
                .then()
                .statusCode(200)
                .body(is("UNKNOWN"));
    }

    @Test
    public void testGetAnotherSecret() {
        given()
                .when().get("/config/anotherSecret")
                .then()
                .statusCode(200)
                .body(is("UNKNOWN"));
    }

    @Test
    public void testGetUnknownSecret() {
        given()
                .when().get("/config/" + UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

}
