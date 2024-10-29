package mizdooni.controllers;
import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    private Restaurant restaurant;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Address address = new Address("Country", "City", "Street");
        User manager = new User("testManager", "password456", "manager@test.com", address, User.Role.manager);
        restaurant = new Restaurant("Test Restaurant", manager,"Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "",address, "");
    }

    @Test
    void testGetReviewsWhenRestaurantExists() throws RestaurantNotFound {
        int page = 1;

        PagedList<Review> pagedReviews = new PagedList<>(Collections.emptyList(), page, 10);
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reviewService.getReviews(restaurant.getId(), page)).thenReturn(pagedReviews);

        Response response = reviewController.getReviews(restaurant.getId(), page);
        String message = String.format("reviews for restaurant (%d): %s", restaurant.getId(), restaurant.getName());
        assertNotNull(response);
//        assertEquals(Boolean.TRUE, response.equals(Response.ok(message, pagedReviews)));

        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reviewService).getReviews(restaurant.getId(), page);
    }

    @Test
    void testGetReviewsWhenRestaurantNotFound() {
        int page = 1;

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reviewController.getReviews(restaurant.getId(), page));

        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }

    @Test
    void testAddReviewsWhenParamsAreValid() throws UserNotFound, ManagerCannotReview, UserHasNotReserved, RestaurantNotFound, InvalidReviewRating {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "Great food!");
        Map<String, Number> ratingMap = new HashMap<>();
        ratingMap.put("food", 4.5);
        ratingMap.put("service", 4.0);
        ratingMap.put("ambiance", 4.2);
        ratingMap.put("overall", 4.3);
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        Response response = reviewController.addReview(restaurant.getId(), params);
        assertNotNull(response);

//        assertEquals(HttpStatus.OK, response.getStatus());
//        assertEquals("review added successfully", response.getMessage());

        verify(reviewService).addReview(eq(restaurant.getId()), any(Rating.class), eq("Great food!"));
    }

    @Test
    void testAddReviewsWhenParamsAreMissing() {
        Map<String, Object> params = new HashMap<>();

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reviewController.addReview(restaurant.getId(), params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_MISSING, exception.getMessage());

        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }

    @Test
    void testAddReviewsWhenParamsAreInvalid() {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "Great food!");
        params.put("rating", "invalid rating");

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reviewController.addReview(restaurant.getId(), params));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());

        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }
}
