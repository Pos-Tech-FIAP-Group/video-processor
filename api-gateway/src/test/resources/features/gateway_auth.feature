Feature: Gateway valida JWT em rotas protegidas

  Scenario: Requisição sem token em rota protegida retorna 401
    When eu faço um GET em "/api/auth/users/123" sem header Authorization
    Then o status da resposta é 401
    And o corpo contém "message"
