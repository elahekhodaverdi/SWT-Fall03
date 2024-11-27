package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.exceptions.DuplicatedRestaurantName;
import mizdooni.exceptions.InvalidWorkingTime;
import mizdooni.exceptions.UserNotManager;
import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.RestaurantSearchFilter;
import mizdooni.response.PagedList;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.*;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(RestaurantController.class)
class RestaurantControllerApiTest {

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
                null,
                "Food Type",
                LocalTime.of(7, 0),
                LocalTime.of(19, 0),
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
                .andExpect(jsonPath("$.data.name").value("Test"));
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
        when(restaurantService.getRestaurants(eq(1), any(RestaurantSearchFilter.class))).thenReturn(new PagedList<>(restaurantList, 1, 12));
        mockMvc.perform(get("/restaurants")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurants listed"))
                .andExpect(jsonPath("$.data.pageList[0].name").value("Test"))
                .andExpect(jsonPath("$.data.pageList[0].type").value("Food Type"))
                .andExpect(jsonPath("$.data.pageList[0].description").value("Test Description"));
    }

    @Test
    void testGetRestaurantsWithFilter() throws Exception {
        when(restaurantService.getRestaurants(eq(1), any(RestaurantSearchFilter.class))).thenReturn(null);
        mockMvc.perform(get("/restaurants")
                        .param("page", "1")
                        .param("type", "Irani"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurants listed"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetRestaurantsWhenBadRequest() throws Exception {
        when(restaurantService.getRestaurants(eq(1), any(RestaurantSearchFilter.class)))
                .thenThrow(new RuntimeException("test"));

        mockMvc.perform(get("/restaurants")
                        .param("page", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testAddRestaurantSuccessfullyWithOrWithoutImage(boolean includeImage) throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));

        if (includeImage) {
            requestParams.put("image", "/sample-image.jpg");
        }

        when(restaurantService.addRestaurant(
                eq("Test"),
                eq("Food Type"),
                any(),
                any(),
                eq("Test Description"),
                any(),
                eq(includeImage ? "/sample-image.jpg" : ControllerUtils.PLACEHOLDER_IMAGE)
        )).thenReturn(1);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant added"))
                .andExpect(jsonPath("$.data").value(1));

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant added"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "type", "startTime", "endTime", "description", "address"})
    void testAddRestaurantWithMissingParameter(String missingParam) throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));

        requestParams.remove(missingParam);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PARAMS_MISSING));
    }

    @ParameterizedTest
    @CsvSource({
            "country, empty",
            "city, empty",
            "street, empty",
            "country, missing",
            "city, missing",
            "street, missing"
    })
    void testAddRestaurantWithMissingAddressParameters(String field, String scenario) throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "Country");
        addressParams.put("city", "City");
        addressParams.put("street", "Street");

        if ("empty".equals(scenario)) {
            addressParams.put(field, "");
        } else if ("missing".equals(scenario)) {
            addressParams.remove(field);
        }

        requestParams.put("address", addressParams);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PARAMS_MISSING));
    }

    @ParameterizedTest
    @ValueSource(strings = {"startTime", "endTime"})
    void testAddRestaurantWithBadParameter(String timeField) throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));

        requestParams.put(timeField, "invalid-time");

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PARAMS_BAD_TYPE));
    }

    @Test
    void testAddRestaurantWhenRestaurantAlreadyExists() throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));
        doThrow(new DuplicatedRestaurantName()).when(restaurantService).addRestaurant(Mockito.anyString(),
                Mockito.anyString(),
                any(),
                any(),
                Mockito.anyString(),
                any(),
                Mockito.anyString());

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddRestaurantWhenUserIsNotManager() throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));
        doThrow(new UserNotManager()).when(restaurantService).addRestaurant(Mockito.anyString(),
                Mockito.anyString(),
                any(),
                any(),
                Mockito.anyString(),
                any(),
                Mockito.anyString());

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddRestaurantWhenWorkingTimeIsNotValid() throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("name", "Test");
        requestParams.put("type", "Food Type");
        requestParams.put("startTime", "07:00");
        requestParams.put("endTime", "19:00");
        requestParams.put("description", "Test Description");
        requestParams.put("address", Map.of("country", "Country", "city", "City", "street", "Street"));
        doThrow(new InvalidWorkingTime()).when(restaurantService).addRestaurant(Mockito.anyString(),
                Mockito.anyString(),
                any(),
                any(),
                Mockito.anyString(),
                any(),
                Mockito.anyString());

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestParams)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testValidateRestaurantNameWhenIsAvailable() throws Exception {
        when(restaurantService.restaurantExists(any())).thenReturn(false);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", "any-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant name is available"));
    }

    @Test
    void testValidateRestaurantWhenNameIsTaken() throws Exception {
        when(restaurantService.restaurantExists(any())).thenReturn(true);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", "any-name"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("restaurant name is taken"));
    }

    @Test
    void testGetRestaurantTypesSuccessfully() throws Exception {
        Set<String> mockTypes = new HashSet<>(Arrays.asList(sampleRestaurant.getType()));

        when(restaurantService.getRestaurantTypes()).thenReturn(mockTypes);

        mockMvc.perform(get("/restaurants/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant types"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("Food Type"));
    }

    @Test
    void testGetRestaurantTypesWhenFails() throws Exception {
        when(restaurantService.getRestaurantTypes()).thenThrow(new RuntimeException("test"));

        mockMvc.perform(get("/restaurants/types"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test"));

    }

    @Test
    void testGetRestaurantLocationsSuccessfully() throws Exception {
        Map<String, Set<String>> mockLocations = new HashMap<>();
        mockLocations.put("Country", new HashSet<>(Collections.singletonList("City")));

        when(restaurantService.getRestaurantLocations()).thenReturn(mockLocations);

        mockMvc.perform(get("/restaurants/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant locations"))
                .andExpect(jsonPath("$.data.Country").isArray())
                .andExpect(jsonPath("$.data.Country[0]").value("City"));
    }

    @Test
    void testGetRestaurantLocationsWhenFails() throws Exception {
        when(restaurantService.getRestaurantLocations()).thenThrow(new RuntimeException("test"));

        mockMvc.perform(get("/restaurants/locations"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test"));

    }


    @Test
    void testGetManagerRestaurantsSuccessfully() throws Exception {
        List<Restaurant> restaurantList = Collections.singletonList(sampleRestaurant);
        when(restaurantService.getManagerRestaurants(eq(1))).thenReturn(restaurantList);
        mockMvc.perform(get("/restaurants/manager/{managerId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("manager restaurants listed"))
                .andExpect(jsonPath("$.data[0].name").value("Test"))
                .andExpect(jsonPath("$.data[0].type").value("Food Type"))
                .andExpect(jsonPath("$.data[0].description").value("Test Description"));
    }

    @Test
    void testGetManagerRestaurantsWhenBadRequest() throws Exception {
        when(restaurantService.getManagerRestaurants(eq(1)))
                .thenThrow(new RuntimeException("test"));

        mockMvc.perform(get("/restaurants/manager/{managerId}", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("test"));
    }
}
