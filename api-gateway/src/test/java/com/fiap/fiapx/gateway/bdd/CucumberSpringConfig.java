package com.fiap.fiapx.gateway.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CucumberSpringConfig {

    private static final WireMockServer WIRE_MOCK_SERVER;

    static {
        WIRE_MOCK_SERVER = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        WIRE_MOCK_SERVER.start();
    }

    @DynamicPropertySource
    static void configureAuthServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("auth.service-url", () -> "http://localhost:" + WIRE_MOCK_SERVER.port());
    }
}
