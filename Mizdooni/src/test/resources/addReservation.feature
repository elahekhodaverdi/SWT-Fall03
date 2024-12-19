Feature: Add Reservation
  As a user
  I want to add reservations to my account
  So that I can keep track of my dining plans

  Scenario: Adding a valid reservation
    Given a user with username "Mahdi" and role "client"
    And a restaurant named "Test"
    When the user adds a reservation for "Test" on "2024-12-10T10:00"
    Then the reservation list should contain 1 reservation
    And the reservation number should be 0

  Scenario: Adding multiple reservations
    Given a user with username "jane_doe" and role "client"
    And a restaurant named "Cozy Diner"
    When the user adds a reservation for "Cozy Diner" on "2024-12-10T10:00"
    And the user adds a reservation for "Cozy Diner" on "2024-12-11T10:00"
    Then the reservation list should contain 2 reservations
    And the reservation numbers should be 0 and 1
