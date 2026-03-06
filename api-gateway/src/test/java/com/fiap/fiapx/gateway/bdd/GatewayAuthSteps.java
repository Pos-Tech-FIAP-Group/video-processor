package com.fiap.fiapx.gateway.bdd;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayAuthSteps {

    @Autowired
    private WebTestClient webTestClient;

    private EntityExchangeResult<byte[]> lastResponse;

    @When("I send a GET to {string} without Authorization header")
    public void whenGetWithoutAuthorization(String path) {
        lastResponse = webTestClient.get()
                .uri(path)
                .exchange()
                .expectBody()
                .returnResult();
    }

    @Then("the response status is 401")
    public void thenStatusIs401() {
        assertThat(lastResponse.getStatus().value()).isEqualTo(401);
    }

    @And("the body contains {string}")
    public void andBodyContains(String text) {
        String body = new String(lastResponse.getResponseBody());
        assertThat(body).contains(text);
    }
}
