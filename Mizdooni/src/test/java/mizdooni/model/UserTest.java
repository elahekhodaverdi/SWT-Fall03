package mizdooni.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;
    private Reservation reservation1;
    private Reservation reservation2;
    private Restaurant restaurant;


    @BeforeEach
    void setUp() {
        Address address = new Address("Country", "City", "Street");
        user = new User("testUser", "password123", "user@test.com", address, User.Role.client);
        User manager = new User("testManager", "password456", "manager@test.com", address, User.Role.manager);
        restaurant = new Restaurant("Test Restaurant", manager,"Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "",address, "");
        Table table1 = new Table(1, restaurant.getId(), 1);
        Table table2 = new Table(2, restaurant.getId(), 2);

        reservation1 = new Reservation(user, restaurant, table1, LocalDateTime.now().plusHours(3));
        reservation2 = new Reservation(user, restaurant, table2, LocalDateTime.now().plusHours(-2));
    }

    @Test
    void testAddReservation() {
        user.addReservation(reservation1);
        assertEquals(1, user.getReservations().size());
        assertEquals(user.getReservations().getFirst().getTable().getTableNumber(), 1);
        assertEquals(true, user.getReservations().getFirst().getUser().getUsername().equals("testUser"));
        assertEquals(true, user.getReservations().getFirst().getRestaurant().getName().equals("Test Restaurant"));
    }

    @Test
    void testCheckReserved() {
        user.addReservation(reservation1);
        assertFalse(user.checkReserved(restaurant));

        reservation1.cancel();
        assertFalse(user.checkReserved(restaurant));

        user.addReservation(reservation2);
        assertTrue(user.checkReserved(restaurant));
    }

    @Test
    void testGetReservation() {
        user.addReservation(reservation1);
        Reservation fetchedReservation = user.getReservation(reservation1.getReservationNumber());
        assertNotNull(fetchedReservation);
        assertEquals(user.getReservations().getFirst().getTable().getTableNumber(), 1);
        assertEquals(true, fetchedReservation.getUser().getUsername().equals("testUser"));
        assertEquals(true, fetchedReservation.getRestaurant().getName().equals("Test Restaurant"));

        reservation1.cancel();
        fetchedReservation = user.getReservation(reservation1.getReservationNumber());
        assertNull(fetchedReservation);

        Reservation nonExistent = user.getReservation(999);
        assertNull(nonExistent);
    }

    @Test
    void testCheckPassword() {
        assertTrue(user.checkPassword("password123"));
        assertFalse(user.checkPassword("wrong"));
    }
}
