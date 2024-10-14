package mizdooni.model;

import mizdooni.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class TableTest {

    private Reservation reservation1;
    private Reservation reservation2;
    private Table table;

    @BeforeEach
    void setUp() {
        Address address = new Address("Country", "City", "Street");
        User user = new User("testUser", "password123", "user@test.com", address,
                User.Role.client);
        User manager = new User("testManager", "password456", "manager@test.com", address,
                User.Role.manager);
        Restaurant restaurant = new Restaurant("Test Restaurant", manager,"Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "",address, "");
        table = new Table(1, restaurant.getId(), 1);

        reservation1 = new Reservation(user, restaurant, table, LocalDateTime.now().plusHours(3));
        reservation2 = new Reservation(user, restaurant, table, LocalDateTime.now().plusHours(-2));
    }

    @Test
    void testAddReservation() {
        table.addReservation(reservation1);
        assertEquals(1, table.getReservations().size());
        assertEquals(reservation1, table.getReservations().getFirst());
    }

    @Test
    void testIsReserved() {
        table.addReservation(reservation1);
        table.addReservation(reservation2);

        assertTrue(table.isReserved(reservation1.getDateTime()));

        assertTrue(table.isReserved(reservation2.getDateTime()));

        assertFalse(table.isReserved(LocalDateTime.now().plusDays(10)));

        reservation2.cancel();
        assertFalse(table.isReserved(reservation2.getDateTime()));
    }
}
