Feature: Add Reservation
  As a user
  I want to add reservations to my account
  So that I can keep track of my dining plans

  Scenario: Adding a valid reservation
    Given a user with role client
    And a restaurant to reserve in
    When the user adds a reservation for the restaurant on "2024-12-10T10:00"
    Then the reservation list should contain 1 reservation
    And the reservations should contain number 0

  Scenario: Adding multiple reservations
    Given a user with role client
    And a restaurant to reserve in
    When the user adds a reservation for the restaurant on "2024-12-10T10:00"
    And the user adds a reservation for the restaurant on "2024-12-11T10:00"
    Then the reservation list should contain 2 reservations
    And the reservations should contain number 0
    And the reservations should contain number 1
