package com.fiap.fiapx.auth.bdd;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthRegistrationLoginSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> lastRegisterResponse;
    private ResponseEntity<String> lastLoginResponse;

    @Given("there is no user with username {string}")
    public void givenNoUserWithUsername(String username) {
        // MongoDB container starts empty; no cleanup needed for this scenario.
    }

    @When("I register with username {string} email {string} and password {string}")
    public void whenIRegister(String username, String email, String password) {
        String body = """
                {"username":"%s","email":"%s","password":"%s"}
                """.formatted(username, email, password);
        lastRegisterResponse = restTemplate.postForEntity(
                "/api/auth/register",
                request(body),
                String.class
        );
    }

    @Then("the registration returns status 201")
    public void thenRegistrationReturns201() {
        assertThat(lastRegisterResponse).isNotNull();
        assertThat(lastRegisterResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @And("the response contains {string}")
    public void andResponseContains(String text) {
        if ("userUuid".equals(text)) {
            assertThat(lastRegisterResponse.getBody()).contains(text);
        } else if ("token".equals(text)) {
            assertThat(lastLoginResponse.getBody()).contains(text);
        }
    }

    @When("I log in with username {string} and password {string}")
    public void whenILogIn(String username, String password) {
        String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
        lastLoginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                request(body),
                String.class
        );
    }

    @Then("the login returns status 200")
    public void thenLoginReturns200() {
        assertThat(lastLoginResponse).isNotNull();
        assertThat(lastLoginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private static HttpEntity<String> request(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }
}
