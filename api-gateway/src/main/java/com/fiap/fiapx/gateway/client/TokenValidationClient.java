package com.fiap.fiapx.gateway.client;

/**
 * Port para validação de token JWT (ex.: chamada ao Auth Service).
 * Permite mockar em testes unitários do filtro.
 */
public interface TokenValidationClient {

    /**
     * Valida o token e retorna a resposta do serviço de auth.
     *
     * @param token token Bearer (sem o prefixo "Bearer ")
     * @return resposta com valid e subject; nunca null
     */
    ValidateResponse validate(String token);
}
