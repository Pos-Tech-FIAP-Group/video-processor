package com.fiap.fiapx.gateway.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resposta do Auth Service GET /auth/validate.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ValidateResponse(
        @JsonProperty("valid") boolean valid,
        @JsonProperty("subject") String subject
) {}
