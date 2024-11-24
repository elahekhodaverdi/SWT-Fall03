package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.response.PagedList;
import mizdooni.service.RestaurantService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    private Restaurant sampleRestaurant;


    private Restaurant getAnonymousRestaurant() {
        return new Restaurant(
                "Test",
                null, // Manager will be mocked or set in specific tests
                "Fast Food",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                "Test Description",
                new Address("Country", "City", "Street"),
                "/sample-image.jpg"
        );
    }

    @BeforeEach
    void setUp() {
        sampleRestaurant = getAnonymousRestaurant();
    }

    @Test
    void testGetRestaurantWhenRestaurantExists() throws Exception {
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(sampleRestaurant);

        mockMvc.perform(get("/restaurants/{restaurantId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant found"))
                .andExpect(jsonPath("$.data.name").value("Test Restaurant"));
    }

    @Test
    void testGetRestaurantWhenRestaurantDoesNotExists() throws Exception {
        when(restaurantService.getRestaurant(Mockito.anyInt())).thenReturn(null);

        mockMvc.perform(get("/restaurants/{restaurantId}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("restaurant not found"));
    }

    @Test
    void testGetRestaurantsWithoutFilter() throws Exception {
        List<Restaurant> restaurantList = Collections.singletonList(sampleRestaurant);

        when(restaurantService.getRestaurants(1, null)).thenReturn(new PagedList<>(restaurantList, 1, 12));
        mockMvc.perform(get("/restaurants")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurants listed"));
    }

    @Test
    void testGetRestaurantsWithFilter() throws Exception {
        List<Restaurant> restaurantList = Collections.singletonList(sampleRestaurant);

        when(restaurantService.getRestaurants(1, null)).thenReturn(new PagedList<>(restaurantList, 1, 12));
        mockMvc.perform(get("/restaurants")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurants listed"));
    }

    @Test
    void testGetRestaurantsWhenBadRequestAccured() throws Exception {
        List<Restaurant> restaurantList = Collections.singletonList(sampleRestaurant);

        when(restaurantService.getRestaurants(1, null)).thenReturn(new PagedList<>(restaurantList, 1, 12));
        mockMvc.perform(get("/restaurants")
                        .param("page", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddRestaurantSuccessfully() throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Fast Food");
        requestParams.put("startTime", "09:00");
        requestParams.put("endTime", "22:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));
        requestParams.put("image", "/sample-image.jpg");

        when(restaurantService.addRestaurant(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(LocalTime.class),
                Mockito.any(LocalTime.class),
                Mockito.anyString(),
                Mockito.any(Address.class),
                Mockito.anyString()
        )).thenReturn(1);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant added"))
                .andExpect(jsonPath("$.data").value(1));
    }


}
