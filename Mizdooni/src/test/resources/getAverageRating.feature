Feature: Calculate Average Rating of a Restaurant
  As a user or manager
  I want to calculate the average rating of a restaurant
  So that I can assess overall satisfaction

  Scenario: Calculating the average rating for a single review
    Given a restaurant named "Test" managed by "Mahdi"
    And a user "Elahe" adds a review with food 4, service 5, ambiance 3, and overall 4 with comment "Great"
    When the restaurant manager views the reviews
    Then the average rating should be food 4.0, service 5.0, ambiance 3.0, and overall 4.0

  Scenario: Calculating the average rating for multiple reviews
    Given a restaurant named "Test" managed by "Mahdi"
    And a user "Elahe" adds a review with food 4, service 5, ambiance 3, and overall 4 with comment "Great"
    And a user "Test" adds a review with food 5, service 1, ambiance 3, and overall 4 with comment "Mid"
    When the restaurant manager views the reviews
    Then the average rating should be food 4.5, service 3.0, ambiance 3.0, and overall 4.0
