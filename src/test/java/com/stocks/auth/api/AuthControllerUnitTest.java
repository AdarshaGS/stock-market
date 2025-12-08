package com.stocks.auth.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.api.AuthController;
import com.auth.data.LoginRequest;
import com.auth.data.LoginResponse;
import com.auth.security.JwtUtil;
import com.users.data.Users;
import com.users.repo.UsersRepository;
import com.users.service.UserWriteService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserWriteService userWriteService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private Users testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = Users.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .build();

        userDetails = User.builder()
                .username("test@example.com")
                .password("$2a$10$hashedPassword")
                .roles("USER")
                .build();
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("test@example.com"))
                .thenReturn("mock-jwt-token");
        when(usersRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertNotNull(loginResponse);
        assertEquals("mock-jwt-token", loginResponse.getToken());
        assertEquals("test@example.com", loginResponse.getEmail());
        assertEquals("Test User", loginResponse.getName());
        assertEquals("Login successful", loginResponse.getMessage());
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertNotNull(loginResponse);
        assertEquals("Invalid email or password", loginResponse.getMessage());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        Users newUser = Users.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        Users savedUser = Users.builder()
                .id(2L)
                .name("New User")
                .email("newuser@example.com")
                .password("$2a$10$hashedPassword")
                .build();

        when(usersRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("$2a$10$hashedPassword");
        when(userWriteService.createUser(any(Users.class)))
                .thenReturn(savedUser);

        // Act
        ResponseEntity<?> response = authController.register(newUser);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertNotNull(loginResponse);
        assertEquals("newuser@example.com", loginResponse.getEmail());
        assertEquals("New User", loginResponse.getName());
        assertEquals("User registered successfully", loginResponse.getMessage());
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // Arrange
        Users newUser = Users.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        when(usersRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.register(newUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertNotNull(loginResponse);
        assertEquals("User with this email already exists", loginResponse.getMessage());
    }
}
