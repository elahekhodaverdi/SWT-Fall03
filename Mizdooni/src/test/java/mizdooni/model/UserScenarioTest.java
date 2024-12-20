package mizdooni.model;

import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import mizdooni.MizdooniApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(classes = MizdooniApplication.class)
public class UserScenarioTest {
    private User user;
    private Restaurant restaurant;
    private Reservation reservation;
    private final Address address = new Address("Country", "City", "Street");

    @Given("a user with username {string} and role {string}")
    public void createUser(String username, String role) {
        User.Role userRole = User.Role.valueOf(role);
        user = new User(username, "password123", "user@test.com", address, userRole);
    }

    @Given("a restaurant named {string}")
    public void createRestaurant(String restaurantName) {
        User manager = new User("testManager", "password456", "manager@test.com", address, User.Role.manager);
        restaurant = new Restaurant(restaurantName, manager,"Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "",address, "");
    }

    @When("the user adds a reservation for {string} on {string}")
    public void addReservation(String restaurantName, String dateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.parse(dateTime);
        Table table = new Table(1, restaurant.getId(), 1);
        reservation = new Reservation(user, restaurant, table, reservationDateTime);
        user.addReservation(reservation);
    }

    @Then("the reservation list should contain {int} reservation(s)")
    public void checkReservationCount(int count) {
        assertEquals(count, user.getReservations().size());
    }

    @Then("the reservations should contain number {int}")
    public void checkReservationNumber(int reservationNumber) {
        assertTrue(user.getReservations().stream().anyMatch(item ->
                item.getReservationNumber() == reservationNumber));
    }
}
