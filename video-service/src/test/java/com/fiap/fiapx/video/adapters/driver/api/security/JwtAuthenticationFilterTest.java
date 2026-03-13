package com.fiap.fiapx.video.adapters.driver.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtTokenValidator);

        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void deve_retornar_401_quando_nao_houver_header_authorization() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void deve_retornar_401_quando_token_for_invalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtTokenValidator.isValid("token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void deve_continuar_filtro_quando_token_for_valido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtTokenValidator.isValid("token")).thenReturn(true);
        when(jwtTokenValidator.extractUsername("token")).thenReturn("usuario");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void nao_deve_filtrar_endpoint_health() {
        when(request.getServletPath()).thenReturn("/api/videos/health");

        boolean result = filter.shouldNotFilter(request);

        assertThat(result).isTrue();
    }
}