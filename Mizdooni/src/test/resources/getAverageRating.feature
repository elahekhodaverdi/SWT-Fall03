Feature: Calculate Average Rating of a Restaurant
  As a user or manager
  I want to calculate the average rating of a restaurant
  So that I can assess overall satisfaction

  Scenario Outline: Calculating the average rating for a single review
    Given a restaurant with no reviews
    And a user that adds a review with food <food>, service <service>, ambiance <ambiance>, and overall <overall> with comment "<comment>"
    When the restaurant manager views the reviews
    Then the average rating should be food <average_food>, service <average_service>, ambiance <average_ambiance>, and overall <average_overall>

    Examples:
      | food | service | ambiance | overall | comment   | average_food | average_service | average_ambiance | average_overall |
      | 4    | 5       | 3        | 4       | Great     | 4.0          | 5.0             | 3.0              | 4.0             |
      | 3    | 3       | 3        | 3       | Average   | 3.0          | 3.0             | 3.0              | 3.0             |
      | 5    | 4       | 4        | 5       | Excellent | 5.0          | 4.0             | 4.0              | 5.0             |

  Scenario Outline: Calculating the average rating for reviews
    Given a restaurant with no reviews
    And a user that adds a review with food <food1>, service <service1>, ambiance <ambiance1>, and overall <overall1> with comment "<comment1>"
    And another user that adds a review with food <food2>, service <service2>, ambiance <ambiance2>, and overall <overall2> with comment "<comment2>"
    When the restaurant manager views the reviews
    Then the average rating should be food <average_food>, service <average_service>, ambiance <average_ambiance>, and overall <average_overall>

    Examples:
      | food1 | service1 | ambiance1 | overall1 | comment1 | food2 | service2 | ambiance2 | overall2 | comment2 | average_food | average_service | average_ambiance | average_overall |
      | 4     | 5        | 3         | 4        | Great    | 5     | 1        | 3         | 4        | Mid      | 4.5          | 3.0             | 3.0              | 4.0             |
      | 3     | 4        | 2         | 3        | Good     | 4     | 5        | 4         | 5        | Excellent| 3.5          | 4.5             | 3.0              | 4.0             |
      | 2     | 2        | 3         | 2        | Poor     | 3     | 3        | 3         | 3        | Fair     | 2.5          | 2.5             | 3.0              | 2.5             |
