package com.fiap.fiapx.video.bdd;

import com.fiap.fiapx.video.adapters.driver.api.dto.response.VideoResponse;
import com.fiap.fiapx.video.adapters.driven.infra.persistence.repository.PagedResponse;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class VideoListSteps {

    @Autowired
    private TestRestTemplate restTemplate;

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
                null,
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
}
