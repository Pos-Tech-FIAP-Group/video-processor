Feature: Video processing message consumption

  O processing-service consome mensagens da fila de processamento e publica eventos
  de conclusão (sucesso ou falha) na exchange de eventos.

  Scenario: When processing request has non-existent file then failed event is published
    Given the processing queue is ready
    When I publish a video processing request with videoId "bdd-video-001" input path "/nonexistent/video.mp4" and userId "bdd-user"
    Then within 15 seconds a failed event is published for videoId "bdd-video-001"
    And the failed event contains an error message
