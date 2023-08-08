package com.azure.microprofile.it.openliberty;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class ConfigResourceIT {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:9080";
    }

    @Test
    public void testGetConfigValue() {
        given()
                .when().get("/config/value/secret")
                .then()
                .statusCode(200)
                .body(is("1234"));

        given()
                .when().get("/config/value/anotherSecret")
                .then()
                .statusCode(200)
                .body(is("5678"));

        given()
                .pathParam("name", UUID.randomUUID().toString())
                .when().get("/config/value/{name}")
                .then()
                .statusCode(204);
    }

    @Test
    public void testGetConfigPropertyNames() {
        given()
                .when().get("/config/propertyNames")
                .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body("", hasItems("secret", "anotherSecret"));
    }

    @Test
    public void testGetConfigProperties() {
        given()
                .when().get("/config/properties")
                .then()
                .statusCode(200)
                .body("keySet().size()", is(2))
                .body("secret", equalTo("1234"))
                .body("anotherSecret", equalTo("5678"));
    }

}
