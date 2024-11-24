package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TableController.class)
public class TableControllerTest {
    private Restaurant restaurant;
    private Table table;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private TableService tableService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Address address = new Address("Country", "City", "Street");
        User manager = new User("testManager", "password456", "manager@test.com", address, User.Role.manager);
        restaurant = new Restaurant("Test Restaurant", manager, "Fast food", LocalTime.now(),
                LocalTime.now().plusHours(10), "", address, "");
        table = new Table(1, restaurant.getId(), 1);
        when(userService.getCurrentUser()).thenReturn(manager);
    }

    @Test
    void testGetTablesWithSuccess() throws Exception {
        when(tableService.getTables(restaurant.getId())).thenReturn(List.of(table));
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);
        mockMvc.perform(get("/tables/{restaurantId}", restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("tables listed"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tableNumber").value(table.getTableNumber()))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(table.getSeatsNumber()));
    }

    @Test
    void testGetTablesWhenBadRequest() throws Exception {
        when(tableService.getTables(restaurant.getId())).thenThrow(new RuntimeException("test"));
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);

        mockMvc.perform(get("/tables/{restaurantId}", restaurant.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @Test
    void testGetTablesWhenRestaurantNotFound() throws Exception {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(null);


        mockMvc.perform(get("/tables/{restaurantId}", restaurant.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("restaurant not found"));
    }

    @Test
    void testAddTableWithSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "4");
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);

        mockMvc.perform(post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("table added"));
    }

    @Test
    void testAddTableWhenMissingSeatsNumber() throws Exception {
        Map<String, String> params = new HashMap<>();
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);

        mockMvc.perform(post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ControllerUtils.PARAMS_MISSING));
    }

    @Test
    void testAddTableWhenInvalidSeatsNumber() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "invalid");
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);

        mockMvc.perform(post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ControllerUtils.PARAMS_BAD_TYPE));
    }

    @Test
    void testAddTableWhenUserIsNotManager() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "4");
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);

        doThrow(new UserNotManager())
                .when(tableService)
                .addTable(restaurant.getId(), 4);

        mockMvc.perform(post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not a manager."));
    }

    @Test
    void testAddTableWhenUserIsNotRestaurantManager() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "4");
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(restaurant);

        doThrow(new InvalidManagerRestaurant())
                .when(tableService)
                .addTable(restaurant.getId(), 4);

        mockMvc.perform(post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The manager is not valid for this restaurant."));
    }
}
