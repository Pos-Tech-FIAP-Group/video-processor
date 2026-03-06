Feature: Gateway validates JWT on protected routes

  Scenario: Request without token on protected route returns 401
    When I send a GET to "/api/auth/users/123" without Authorization header
    Then the response status is 401
    And the body contains "message"
