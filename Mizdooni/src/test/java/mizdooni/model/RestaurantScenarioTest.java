package mizdooni.model;

import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import mizdooni.MizdooniApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;


public class RestaurantScenarioTest {
    private Restaurant restaurant;
    private User manager;
    private User user;
    private Review review;
    private final Address address = new Address("Country", "City", "Street");

    private Rating createRating(double food, double service, double ambiance, double overall) {
        Rating rating = new Rating();
        rating.food = food;
        rating.service = service;
        rating.ambiance = ambiance;
        rating.overall = overall;
        return rating;
    }

    private boolean ratingEquals(Rating rating1, Rating rating2) {
        return (rating1.food == rating2.food &&
                rating1.service == rating2.service &&
                rating1.ambiance == rating2.ambiance &&
                rating1.overall == rating2.overall);
    }

    @Given("a restaurant named {string} managed by {string}")
    public void createRestaurant(String restaurantName, String managerName) {
        manager = new User(managerName, "password456", "manager@test.com", address, User.Role.manager);
        restaurant = new Restaurant(restaurantName, manager,"Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "",address, "");
    }

    @Given("a user {string} adds a review with food {int}, service {int}, ambiance {int}, and overall {int} with comment {string}")
    public void addReview(String username, int food, int service, int ambiance, int overall, String comment) {
        user = new User(username, "password123", "user@example.com", address, User.Role.client);
        review = new Review(user, createRating(food, service, ambiance, overall), comment, LocalDateTime.now());
        restaurant.addReview(review);
    }

    @When("the same user updates the review with food {int}, service {int}, ambiance {int}, and overall {int} with comment {string}")
    public void updateReview(int food, int service, int ambiance, int overall, String comment) {
        review = new Review(user, createRating(food, service, ambiance, overall), comment, LocalDateTime.now());
        restaurant.addReview(review);
    }

    @When("the restaurant manager views the reviews")
    public void viewReviews() {}

    @Then("the restaurant should have {int} review(s)")
    public void checkReviewCount(int count) {
        assertEquals(count, restaurant.getReviews().size());
    }

    @Then("the updated review should have values food {int}, service {int}, ambiance {int}, and overall {int}")
    public void checkUpdatedReview(int food, int service, int ambiance, int overall) {
        assertTrue(ratingEquals(createRating(food, service, ambiance, overall),
                restaurant.getReviews().getFirst().getRating()));
    }

    @Then("the average rating should be food {double}, service {double}, ambiance {double}, and overall {double}")
    public void checkAverageRating(double food, double service, double ambiance, double overall) {
        Rating averageRating = restaurant.getAverageRating();
        assertEquals(food, averageRating.food, 0.01);
        assertEquals(service, averageRating.service, 0.01);
        assertEquals(ambiance, averageRating.ambiance, 0.01);
        assertEquals(overall, averageRating.overall, 0.01);
    }
}
