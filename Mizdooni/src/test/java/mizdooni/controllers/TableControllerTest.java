package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.model.*;
import mizdooni.response.Response;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TableController.class)
public class TableControllerTest {
    private Restaurant restaurant;
    private Table table;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private TableService tableService;

    @InjectMocks
    private TableController tableController;

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
        when(tableService.getTables(restaurant.getId())).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/tables/{restaurantId}", restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("tables listed"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tableNumber").value(table.getTableNumber()))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(table.getSeatsNumber()));
    }

    @Test
    void testGetTablesWhenBadRequest() throws Exception {
        when(tableService.getTables(restaurant.getId())).thenThrow(new RuntimeException("test"));

        mockMvc.perform(MockMvcRequestBuilders.get("/tables/{restaurantId}", restaurant.getId()))
                .andExpect(status().isBadRequest());
        //TODO::have to check error message
    }

    @Test
    void testGetTablesWhenRestaurantNotFound() throws Exception {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/tables/{restaurantId}", restaurant.getId()))
                .andExpect(status().isBadRequest());
        //TODO::have to check error message
    }

    @Test
    void testAddTableWithSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "4");

        mockMvc.perform(MockMvcRequestBuilders.post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("table added"));
    }

    @Test
    void testAddTableWhenMissingSeatsNumber() throws Exception {
        Map<String, String> params = new HashMap<>();

        mockMvc.perform(MockMvcRequestBuilders.post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ControllerUtils.PARAMS_MISSING));
    }

    @Test
    void testAddTable_Fail_InvalidSeatsNumber() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "invalid");

        mockMvc.perform(MockMvcRequestBuilders.post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ControllerUtils.PARAMS_BAD_TYPE));
    }

    @Test
    void testAddTableWhenInternalError() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("seatsNumber", "4");

//        when(tableService.addTable(restaurant.getId(), 4)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.post("/tables/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest());
    }
}
