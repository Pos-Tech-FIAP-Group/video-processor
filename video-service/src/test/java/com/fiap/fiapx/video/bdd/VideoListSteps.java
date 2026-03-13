package com.fiap.fiapx.video.bdd;

import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.adapters.driven.infra.persistence.repository.PagedResponse;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class VideoListSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private ResponseEntity<PagedResponse<VideoResponse>> lastListResponse;

    @Given("user {string} has no videos")
    public void givenUserHasNoVideos(String userId) {
        // Banco inicia vazio; usuário não tem vídeos.
    }

    @When("I list videos for user {string} with page {int} and size {int}")
    public void whenIListVideosForUser(String userId, int page, int size) {
        UUID userUuid = UUID.fromString(userId);

        lastListResponse = restTemplate.exchange(
                "/api/videos?userId={userId}&page={page}&size={size}",
                HttpMethod.GET,
                entidadeComToken(),
                new ParameterizedTypeReference<>() {},
                userUuid,
                page,
                size
        );
    }

    @Then("the list returns status 200")
    public void thenListReturns200() {
        assertThat(lastListResponse).isNotNull();
        assertThat(lastListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @And("the response has totalItems {int}")
    public void andResponseHasTotalItems(int expected) {
        assertThat(lastListResponse.getBody()).isNotNull();
        assertThat(lastListResponse.getBody().totalItems()).isEqualTo(expected);
    }

    @And("the response has empty items")
    public void andResponseHasEmptyItems() {
        assertThat(lastListResponse.getBody()).isNotNull();
        assertThat(lastListResponse.getBody().items()).isEmpty();
    }

    private HttpEntity<Void> entidadeComToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(gerarTokenValido());
        return new HttpEntity<>(headers);
    }

    private String gerarTokenValido() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject("usuario-teste")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}