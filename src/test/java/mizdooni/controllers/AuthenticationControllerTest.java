package mizdooni.controllers;

import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ServiceUtils;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private User mockUser;
    private Address mockUserAddress;
    private static String USERNAME = "user";
    private static String PASSWORD = "pass";
    private static String EMAIL = "user@gmail.com";

    @BeforeEach
    void setup() {
        mockUserAddress = new Address("Country", "City", null);
        mockUser = new User(USERNAME, PASSWORD, EMAIL, mockUserAddress, User.Role.client);
    }

    @Test
    void testUserLoggedIn() {
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authenticationController.user();

        assertEquals("current user", response.getMessage());
        assertEquals(mockUser, response.getData());

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testNoUserLoggedIn() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.user();
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testSuccessfulLogin() {
        when(userService.login(USERNAME, PASSWORD)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", USERNAME);
        loginParams.put("password", PASSWORD);

        Response response = authenticationController.login(loginParams);

        assertEquals("login successful", response.getMessage());
        assertEquals(mockUser, response.getData());

        verify(userService, times(1)).login(USERNAME, PASSWORD);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testInvalidCredentialsForLogin() {
        Map<String, String> params = new HashMap<>();
        params.put("username", USERNAME);
        params.put("password", PASSWORD);

        when(userService.login(USERNAME, PASSWORD)).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.login(params);
        });

        assertEquals("invalid username or password", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(userService, times(1)).login(USERNAME, PASSWORD);
        verify(userService, never()).getCurrentUser();
    }

    @Test
    void testEmptyParamsForLogin() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "");
        params.put("password", "");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.login(params);
        });

        assertEquals(PARAMS_MISSING, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(userService, never()).login(anyString(), anyString());
        verify(userService, never()).getCurrentUser();
    }

    @Test
    void testSuccessfulSignup() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", USERNAME);
        params.put("password", PASSWORD);
        params.put("email", EMAIL);
        params.put("address", Map.of("country", "Country", "city", "City"));
        params.put("role", "client");

        doNothing().when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
        when(userService.login(USERNAME, PASSWORD)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authenticationController.signup(params);

        assertEquals("signup successful", response.getMessage());
        assertEquals(mockUser, response.getData());

        verify(userService, times(1)).signup(eq(USERNAME), eq(PASSWORD), eq(EMAIL), any(Address.class), eq(User.Role.client));
        verify(userService, times(1)).login(USERNAME, PASSWORD);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testMissingParamsAtSignup() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", USERNAME);
        params.put("password", "");
        params.put("email", EMAIL);
        params.put("address", Map.of("country", "Country", "city", "City"));
        params.put("role", "client");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(params);
        });

        assertEquals(PARAMS_MISSING, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(userService, never()).signup(any(), any(), any(), any(), any());
        verify(userService, never()).login(any(), any());
        verify(userService, never()).getCurrentUser();
    }

    @Test
    void testBadTypeParamsAtSignup() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", USERNAME);
        params.put("password", PASSWORD);
        params.put("email", EMAIL);
        params.put("address", "");
        params.put("role", "client");


        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(params);
        });

        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(userService, never()).signup(any(), any(), any(), any(), any());
        verify(userService, never()).login(any(), any());
        verify(userService, never()).getCurrentUser();
    }

    @Test
    void testThrowExceptionWhileSignup() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", USERNAME);
        params.put("password", PASSWORD);
        params.put("email", EMAIL);
        params.put("address", Map.of("country", "Country", "city", "City"));
        params.put("role", "client");

        doThrow(new ResponseException(HttpStatus.BAD_REQUEST, new Exception())).when(userService).signup(anyString(), anyString(), anyString(), any(), any());

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.signup(params);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(userService, times(1)).signup(any(), any(), any(), any(), any());
        verify(userService, never()).login(any(), any());
        verify(userService, never()).getCurrentUser();
    }

    @Test
    void testSuccessfulLogout() {
        when(userService.logout()).thenReturn(true);

        Response response = authenticationController.logout();

        assertEquals("logout successful", response.getMessage());

        verify(userService, times(1)).logout();
    }

    @Test
    void testLogoutWithNoUserLoggedIn() throws Exception {
        when(userService.logout()).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authenticationController.logout();
        });

        assertEquals("no user logged in", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(userService, times(1)).logout();
    }

    @Test
    void testAvailableUsername() throws Exception {
        try (MockedStatic<ServiceUtils> utilities = mockStatic(ServiceUtils.class)) {
            utilities.when(() -> ServiceUtils.validateUsername(USERNAME)).thenReturn(true);
            when(userService.usernameExists(USERNAME)).thenReturn(false);

            Response response = authenticationController.validateUsername(USERNAME);

            assertEquals("username is available", response.getMessage());

            verify(userService, times(1)).usernameExists(USERNAME);
        }
    }

    @Test
    void testInvalidUsername() throws Exception {
        try (MockedStatic<ServiceUtils> utilities = mockStatic(ServiceUtils.class)) {
            utilities.when(() -> ServiceUtils.validateUsername(USERNAME)).thenReturn(false);

            ResponseException response = assertThrows(ResponseException.class, () -> {
                authenticationController.validateUsername(USERNAME);
            });

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
            assertEquals("invalid username format", response.getMessage());

            verify(userService, never()).usernameExists(any());
        }
    }

    @Test
    void testUsernameTaken() {
        try (MockedStatic<ServiceUtils> utilities = mockStatic(ServiceUtils.class)) {
            utilities.when(() -> ServiceUtils.validateUsername(USERNAME)).thenReturn(true);
            when(userService.usernameExists(USERNAME)).thenReturn(true);

            ResponseException response = assertThrows(ResponseException.class, () -> {
                authenticationController.validateUsername(USERNAME);
            });

            assertEquals(HttpStatus.CONFLICT, response.getStatus());
            assertEquals("username already exists", response.getMessage());

            verify(userService, times(1)).usernameExists(USERNAME);
        }
    }

    @Test
    void testEmailAvailable() {
        try (MockedStatic<ServiceUtils> utilities = mockStatic(ServiceUtils.class)) {
            utilities.when(() -> ServiceUtils.validateEmail(EMAIL)).thenReturn(true);
            when(userService.emailExists(EMAIL)).thenReturn(false);

            Response response = authenticationController.validateEmail(EMAIL);

            assertEquals("email not registered", response.getMessage());

            verify(userService, times(1)).emailExists(EMAIL);
        }
    }

    @Test
    void testInvalidTaken() {
        try (MockedStatic<ServiceUtils> utilities = mockStatic(ServiceUtils.class)) {
            utilities.when(() -> ServiceUtils.validateEmail(EMAIL)).thenReturn(false);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.validateEmail(EMAIL);
            });

            assertEquals("invalid email format", exception.getMessage());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

            verify(userService, never()).emailExists(any());
        }
    }

    @Test
    void testEmailTaken() {
        try (MockedStatic<ServiceUtils> utilities = mockStatic(ServiceUtils.class)) {
            utilities.when(() -> ServiceUtils.validateEmail(EMAIL)).thenReturn(true);
            when(userService.emailExists(EMAIL)).thenReturn(true);

            ResponseException exception = assertThrows(ResponseException.class, () -> {
                authenticationController.validateEmail(EMAIL);
            });

            assertEquals("email already registered", exception.getMessage());
            assertEquals(HttpStatus.CONFLICT, exception.getStatus());

            verify(userService).emailExists(EMAIL);
        }
    }
}
