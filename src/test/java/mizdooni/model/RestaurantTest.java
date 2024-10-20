package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestaurantTest {
    private Restaurant restaurant;
    private User manager;
    private User client;
    private static Address DEFAULT_ADDRESS = new Address("Street", "City", "Country");
    private static String DEFAULT_PASSWORD = "password";

    @BeforeEach
    void setUp() {
        manager = createUserWithDefaultAddressAndPass("managerUsername", "manager@example.com", User.Role.manager);
        client = createUserWithDefaultAddressAndPass("clientUsername", "client@example.com", User.Role.client);
        restaurant = new Restaurant("Test", manager, "Italian", LocalTime.of(10, 0), LocalTime.of(22, 0), "A great place", DEFAULT_ADDRESS, "imageLink");
    }

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

    private User createUserWithDefaultAddressAndPass(String username, String email, User.Role role) {
        return new User(username, DEFAULT_PASSWORD, email, DEFAULT_ADDRESS, role);
    }

    @Test
    void testAddFirstReviewForNewUser() {
        int initialReviewCount = restaurant.getReviews().size();
        User anotherClient = createUserWithDefaultAddressAndPass("anotherClientUsername", "anotherClient@example.com", User.Role.client);

        Rating rating = createRating(5, 5, 5, 5);
        Review review = new Review(anotherClient, rating, "Great food!", LocalDateTime.now());
        restaurant.addReview(review);

        assertEquals(initialReviewCount + 1, restaurant.getReviews().size());
        assertEquals(review, restaurant.getReviews().get(restaurant.getReviews().size() - 1));
        assertEquals(anotherClient.getId(), restaurant.getReviews().get(restaurant.getReviews().size() - 1).getUser().getId());
        Review newReview = restaurant.getReviews().get(restaurant.getReviews().size() - 1);
        assertTrue(ratingEquals(newReview.getRating(), createRating(5,5,5,5)));
    }

    @Test
    void testReplaceExistingReviewOnNewSubmission() {
        Rating rating1 = createRating(5, 5, 5, 5);
        Review review1 = new Review(client, rating1, "Great food!", LocalDateTime.now());
        restaurant.getReviews().add(review1);

        int reviewCountBeforeUpdate = restaurant.getReviews().size();
        Rating rating2 = createRating(4, 4, 4, 4);
        Review review2 = new Review(client, rating2, "Great place!", LocalDateTime.now());
        restaurant.addReview(review2);

        assertEquals(reviewCountBeforeUpdate, restaurant.getReviews().size());
        assertEquals(review2, restaurant.getReviews().get(restaurant.getReviews().size() - 1));
        assertEquals(client.getId(), restaurant.getReviews().get(restaurant.getReviews().size() - 1).getUser().getId());
        Review newReview = restaurant.getReviews().get(restaurant.getReviews().size() - 1);
        assertTrue(ratingEquals(newReview.getRating(), createRating(4,4,4,4)));

    }

    @Test
    void testGetAverageRatingWithReviews() {
        restaurant.getReviews().add(new Review(client, createRating(5, 5, 5, 5), "Excellent!", LocalDateTime.now()));

        User anotherClient = createUserWithDefaultAddressAndPass("anotherClientUsername", "anotherClient@example.com", User.Role.client);
        restaurant.getReviews().add(new Review(anotherClient, createRating(4, 4, 4, 4), "Excellent!", LocalDateTime.now()));

        Rating averageRating = restaurant.getAverageRating();

        assertTrue(ratingEquals(averageRating, createRating(4.5,4.5,4.5,4.5)));

    }

    @Test
    void testGetAverageRatingWithNoReviews() {
        Rating averageRating = restaurant.getAverageRating();
        assertTrue(ratingEquals(averageRating, createRating(0,0,0,0)));
    }

    @Test
    void testGetStarCount() {
        restaurant.addReview(new Review(client, createRating(3, 3, 4, 3), "Great food!", LocalDateTime.now()));
        assertEquals(3, restaurant.getStarCount());
    }

    @Test
    void testAddTable() {
        int initialTableCount = restaurant.getTables().size();
        Table table = new Table(0, restaurant.getId(), 6);
        restaurant.addTable(table);

        assertEquals(initialTableCount + 1, restaurant.getTables().size());
        assertEquals(table, restaurant.getTables().get(restaurant.getTables().size() - 1));
        assertEquals(initialTableCount + 1, restaurant.getTables().get(restaurant.getTables().size() - 1).getTableNumber());
        assertEquals(6, restaurant.getTables().get(restaurant.getTables().size() - 1).getSeatsNumber());
    }

    @Test
    void testGetTable() {
        Table table1 = new Table(1, restaurant.getId(), 6);
        Table table2 = new Table(2, restaurant.getId(), 3);
        Table table3 = new Table(3, restaurant.getId(), 4);
        restaurant.getTables().add(table1);
        restaurant.getTables().add(table2);
        restaurant.getTables().add(table3);

        assertEquals(table2, restaurant.getTable(2));
        assertEquals( 2, restaurant.getTable(2).getTableNumber());
        assertEquals(3, restaurant.getTable(2).getSeatsNumber());
    }

    @Test
    void testGetMaxSeatsNumberWithTables() {
        Table table1 = new Table(0, restaurant.getId(), 6);
        Table table2 = new Table(0, restaurant.getId(), 3);

        restaurant.addTable(table1);
        restaurant.addTable(table2);

        assertEquals(6, restaurant.getMaxSeatsNumber());
    }

    @Test
    void testGetMaxSeatsNumberWithNoTables() {
        assertEquals(0, restaurant.getMaxSeatsNumber());
    }
}
