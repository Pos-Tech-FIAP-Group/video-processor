Feature: User registration and login

  Scenario: New user registers and logs in successfully
    Given there is no user with username "bdd_user"
    When I register with username "bdd_user" email "bdd@example.com" and password "senha123"
    Then the registration returns status 201
    And the response contains "userUuid"
    When I log in with username "bdd_user" and password "senha123"
    Then the login returns status 200
    And the response contains "token"
