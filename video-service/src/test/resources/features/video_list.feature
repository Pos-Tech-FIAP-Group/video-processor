Feature: List videos by user

  Scenario: User with no videos receives empty list
    Given user "550e8400-e29b-41d4-a716-446655440000" has no videos
    When I list videos for user "550e8400-e29b-41d4-a716-446655440000" with page 0 and size 10
    Then the list returns status 200
    And the response has totalItems 0
    And the response has empty items
