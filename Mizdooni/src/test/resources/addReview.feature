Feature: Add Reviews to a Restaurant
  As a user
  I want to add reviews to a restaurant and update them
  So that the restaurant can collect feedback

  Scenario: Adding a new review for a restaurant
    Given a restaurant named "Test" managed by "Mahdi"
    And a user "Elahe" adds a review with food 4, service 5, ambiance 3, and overall 4 with comment "Great"
    When the restaurant manager views the reviews
    Then the restaurant should have 1 review

  Scenario: Updating an existing review for a restaurant
    Given a restaurant named "Test" managed by "Mahdi"
    And a user "Elahe" adds a review with food 3, service 4, ambiance 5, and overall 4 with comment "Great"
    When the same user updates the review with food 5, service 5, ambiance 5, and overall 5 with comment "Good"
    Then the restaurant should have 1 review
    And the updated review should have values food 5, service 5, ambiance 5, and overall 5

  Scenario: Adding multiple reviews for a restaurant
    Given a restaurant named "Test" managed by "Mahdi"
    And a user "Elahe" adds a review with food 4, service 5, ambiance 3, and overall 4 with comment "Great"
    And a user "Test" adds a review with food 3, service 2, ambiance 3, and overall 1 with comment "Bad"
    When the restaurant manager views the reviews
    Then the restaurant should have 2 review
