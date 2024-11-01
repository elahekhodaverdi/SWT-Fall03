package mizdooni.controllers;

import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mizdooni.controllers.ControllerUtils.DATETIME_FORMATTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationControllerTest {
    private User user;
    private Reservation reservation;
    private Restaurant restaurant;
    private Table table;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReservationService reserveService;

    @InjectMocks
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Address address = new Address("Country", "City", "Street");
        user = new User("testUser", "password123", "user@test.com", address, User.Role.client);
        User manager = new User("testManager", "password456", "manager@test.com", address, User.Role.manager);
        restaurant = new Restaurant("Test Restaurant", manager, "Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "", address, "");
        table = new Table(1, restaurant.getId(), 1);
        reservation = new Reservation(user, restaurant, table, LocalDateTime.now().plusHours(-2));
    }

    @Test
    void testGetReservationsWhenParamsAreValid() throws UserNotManager, TableNotFound, InvalidManagerRestaurant, RestaurantNotFound {

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reserveService.getReservations(restaurant.getId(), table.getTableNumber(), reservation.getDateTime().toLocalDate()))
                .thenReturn(Collections.singletonList(reservation));

        Response response = reservationController.getReservations(restaurant.getId(), table.getTableNumber(),
                reservation.getDateTime().toLocalDate().toString());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals("restaurant table reservations", response.getMessage());
        assertEquals(Collections.singletonList(reservation), response.getData());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verify(reserveService, times(1)).getReservations(restaurant.getId(), table.getTableNumber(),
                reservation.getDateTime().toLocalDate());
    }

    @Test
    void testGetReservationsWhenParamsAreInvalid() {
        String invalidDate = "not a valid date";

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> reservationController.getReservations(restaurant.getId(), table.getTableNumber(), invalidDate)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testGetReservationsWhenDateIsMissing() {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> reservationController.getReservations(restaurant.getId(), table.getTableNumber(), null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testGetReservationsWhenRestaurantNotFound() {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> reservationController.getReservations(restaurant.getId(), table.getTableNumber(), reservation.getDateTime().toLocalDate().toString())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testGetReservationsWhenReservationNotFound() throws UserNotManager, TableNotFound, InvalidManagerRestaurant, RestaurantNotFound {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        doThrow(new RuntimeException("test")).when(reserveService).getReservations(restaurant.getId(),
                table.getTableNumber(), reservation.getDateTime().toLocalDate());

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reservationController.getReservations(restaurant.getId(), table.getTableNumber(),
                        reservation.getDateTime().toLocalDate().toString()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verify(reserveService, times(1)).getReservations(restaurant.getId(), table.getTableNumber(),
                reservation.getDateTime().toLocalDate());
    }

    @Test
    void testGetCustomerReservationWhenCustomerExists() throws UserNotFound, UserNoAccess {

        when(reserveService.getCustomerReservations(user.getId())).thenReturn(Collections.singletonList(reservation));

        Response response = reservationController.getCustomerReservations(user.getId());

        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals("user reservations", response.getMessage());
        assertEquals(Collections.singletonList(reservation), response.getData());
        assertNotNull(response);

        verify(reserveService, times(1)).getCustomerReservations(user.getId());
    }

    @Test
    void testGetCustomerReservationWhenCustomerNotFound() throws UserNotFound, UserNoAccess {

        doThrow(new RuntimeException("test")).when(reserveService).getCustomerReservations(user.getId());

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reservationController.getCustomerReservations(user.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(reserveService, times(1)).getCustomerReservations(user.getId());
    }

    @Test
    void testGetAvailableTimesWhenParamsAreValid() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int people = 4;
        LocalDate date = LocalDate.of(2003, 1, 17);
        List<LocalTime> availableTimes = List.of(LocalTime.of(18, 30), LocalTime.of(22, 0));

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reserveService.getAvailableTimes(restaurant.getId(), people, date)).thenReturn(availableTimes);

        Response response = reservationController.getAvailableTimes(restaurant.getId(), people, date.toString());

        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals("available times", response.getMessage());
        assertEquals(availableTimes, response.getData());
        assertNotNull(response);

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verify(reserveService, times(1)).getAvailableTimes(restaurant.getId(), people, date);
    }

    @Test
    void testGetAvailableTimesWhenParamsAreInvalid() {
        String invalidDate = "not a valid date";
        int people = 4;

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> reservationController.getAvailableTimes(restaurant.getId(), people, invalidDate)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testGetAvailableTimesWhenReservationNotFound() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int people = 4;
        LocalDate date = LocalDate.of(2003, 1, 17);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        doThrow(new RuntimeException("test")).when(reserveService).getAvailableTimes(restaurant.getId(),
                people, date);

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reservationController.getAvailableTimes(restaurant.getId(), people,
                        date.toString()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verify(reserveService).getAvailableTimes(restaurant.getId(), people,
                date);
    }

    @Test
    void testAddReservationWhenParamsAreValid() throws UserNotFound, DateTimeInThePast, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, RestaurantNotFound, InvalidWorkingTime {
        Map<String, String> params = new HashMap<>();
        params.put("people", String.valueOf(4));
        String datetimeString = "2024-10-07 15:24";
        LocalDateTime dateTime = LocalDateTime.parse(datetimeString, DATETIME_FORMATTER);
        params.put("datetime", datetimeString);
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reserveService.reserveTable(restaurant.getId(), 4, dateTime)).thenReturn(reservation);

        Response response = reservationController.addReservation(restaurant.getId(), params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals("reservation done", response.getMessage());
        assertTrue(reservation.equals(response.getData()));

        assertNotNull(response);

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verify(reserveService, times(1)).reserveTable(restaurant.getId(), 4, dateTime);
    }

    @Test
    void testAddReservationWhenParamsAreMissing() {
        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("people", String.valueOf(4));
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> reservationController.addReservation(restaurant.getId(), invalidParams)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_MISSING, exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testAddReservationWhenParamsAreInvalid() {
        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("people", String.valueOf("test"));
        LocalDateTime dateTime = LocalDateTime.now();
        invalidParams.put("datetime", dateTime.toString());

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> reservationController.addReservation(restaurant.getId(), invalidParams)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ControllerUtils.PARAMS_BAD_TYPE, exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testAddReservationWhenReserveTableFailed() throws DateTimeInThePast, RestaurantNotFound, UserNotFound, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, InvalidWorkingTime {
        Map<String, String> params = new HashMap<>();
        params.put("people", String.valueOf(4));
        String datetimeString = "2024-10-07 15:24";
        LocalDateTime dateTime = LocalDateTime.parse(datetimeString, DATETIME_FORMATTER);
        params.put("datetime", datetimeString);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        doThrow(new RuntimeException("test")).when(reserveService).reserveTable(restaurant.getId(), 4, dateTime);

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reservationController.addReservation(restaurant.getId(), params));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verify(reserveService).reserveTable(restaurant.getId(), 4, dateTime);
    }

    @Test
    void testAddReservationWhenRestaurantNotFound() throws DateTimeInThePast, RestaurantNotFound, UserNotFound, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, InvalidWorkingTime {
        Map<String, String> params = new HashMap<>();
        params.put("people", String.valueOf(4));
        String datetimeString = "2024-10-07 15:24";
        LocalDateTime dateTime = LocalDateTime.parse(datetimeString, DATETIME_FORMATTER);
        params.put("datetime", datetimeString);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reservationController.addReservation(restaurant.getId(), params));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());

        verify(restaurantService, times(1)).getRestaurant(restaurant.getId());
        verifyNoInteractions(reserveService);
    }

    @Test
    void testCancelReservationWhenReservationExists() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        doNothing().when(reserveService).cancelReservation(reservation.getReservationNumber());

        Response response = reservationController.cancelReservation(reservation.getReservationNumber());

        assertTrue(response.getMessage().equals("reservation cancelled"));

        verify(reserveService, times(1)).cancelReservation(reservation.getReservationNumber());
    }

    @Test
    void testCancelReservationWhenReservationNotFound() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        doThrow(new RuntimeException("test")).when(reserveService).cancelReservation(reservation.getReservationNumber());

        ResponseException exception = assertThrows(ResponseException.class,
                () -> reservationController.cancelReservation(reservation.getReservationNumber()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(reserveService, times(1)).cancelReservation(reservation.getReservationNumber());
    }
}
