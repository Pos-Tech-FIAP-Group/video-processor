package com.fiap.fiapx.notification.core.application.ports;

import com.fiap.fiapx.notification.core.domain.model.UserInfo;

import java.util.Optional;

/**
 * Porta para obter dados do usuario pelo UUID (chamada ao auth-service).
 * Usado em eventos de falha para obter o e-mail e enviar a notificacao.
 */
public interface GetUserByUuidPort {

    /**
     * Busca usuario pelo UUID (userUuid).
     *
     * @param userUuid UUID do usuario (string)
     * @return Optional com UserInfo (email, username) se encontrado; empty se 404 ou erro
     */
    Optional<UserInfo> findByUserUuid(String userUuid);
}
