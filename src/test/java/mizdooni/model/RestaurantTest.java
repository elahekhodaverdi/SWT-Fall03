package mizdooni.model;

import mizdooni.exceptions.InvalidReviewRating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {
    private Restaurant restaurant;
    private User manager;

    @BeforeEach
    void setUp() {
        manager = new User("managerUsername", "password", "manager@example.com", new Address("Street", "City", "Country"), User.Role.manager);
        Address address = new Address("Street", "City", "Country");
        restaurant = new Restaurant("Test Restaurant", manager, "Italian", LocalTime.of(10, 0), LocalTime.of(22, 0), "A great place", address, "imageLink");
    }

    @Test
    void testAddReview()  {
        Rating rating = new Rating();
        rating.food = 4;
        rating.service = 4;
        rating.ambiance = 5;
        rating.overall = 4.5;
        String comment = "Great food!";
        Review review = new Review(manager, rating, comment, LocalDateTime.now());
        restaurant.addReview(review);
        assertEquals(1, restaurant.getReviews().size());
        assertEquals(review, restaurant.getReviews().get(0));
    }
}
