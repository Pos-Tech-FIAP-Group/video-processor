Feature: Notification on processing failure

  Scenario: When processing fails, user receives failure email
    Given the notification service is ready
    When a processing failure event is published for video "video-bdd-1" and user "550e8400-e29b-41d4-a716-446655440000" with message "Falha no processamento"
    Then an email is sent to the user with the failure details for video "video-bdd-1"
