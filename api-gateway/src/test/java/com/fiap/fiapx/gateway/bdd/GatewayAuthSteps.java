package com.fiap.fiapx.gateway.bdd;

import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayAuthSteps {

    @Autowired
    private WebTestClient webTestClient;

    private EntityExchangeResult<byte[]> lastResponse;

    @Quando("eu faço um GET em {string} sem header Authorization")
    public void quando_get_sem_authorization(String path) {
        lastResponse = webTestClient.get()
                .uri(path)
                .exchange()
                .expectBody()
                .returnResult();
    }

    @Então("o status da resposta é 401")
    public void entao_status_401() {
        assertThat(lastResponse.getStatus().value()).isEqualTo(401);
    }

    @E("o corpo contém {string}")
    public void e_corpo_contem(String texto) {
        String body = new String(lastResponse.getResponseBody());
        assertThat(body).contains(texto);
    }
}
