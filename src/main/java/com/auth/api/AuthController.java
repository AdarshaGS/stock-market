package com.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.data.ForgotPasswordRequest;
import com.auth.data.LoginRequest;
import com.auth.data.LoginResponse;
import com.auth.data.RefreshTokenRequest;
import com.auth.data.RefreshTokenResponse;
import com.auth.security.JwtUtil;
import com.auth.service.RefreshTokenService;
import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;
import com.users.service.UserWriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final UserWriteService userWriteService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UsersRepository usersRepository, UserWriteService userWriteService, PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usersRepository = usersRepository;
        this.userWriteService = userWriteService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

            Users user = usersRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LoginResponse response = LoginResponse.builder()
                    .userId(user.getId())
                    .token(token)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .name(user.getName())
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder().message("Invalid email or password").build());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with hashed password")
    @ApiResponse(responseCode = "201", description = "Successfully registered")
    @ApiResponse(responseCode = "400", description = "User already exists")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(LoginResponse.builder().message("User with this email already exists").build());
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            Users savedUser = userWriteService.createUser(user);

            LoginResponse response = LoginResponse.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .name(savedUser.getName())
                    .message("User registered successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.builder().message("Registration failed: " + e.getMessage()).build());
        }
    }

    // Forgot password API
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email")
    @ApiResponse(responseCode = "200", description = "Successfully sent password reset email")
    public LoginResponse forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        // Call upon checking if the user exists
        userWriteService.updateUserPassword(forgotPasswordRequest);
        return LoginResponse.builder().email(forgotPasswordRequest.getEmail()).message("Password updated successfully")
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Clears the security context and invalidates the current session")
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = null;
        // Extract username and blacklist access token if present
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
                refreshTokenService.blacklistAccessToken(token);
            } catch (Exception e) {
                // Token is invalid or expired, but we'll still proceed with logout
            }
        }
        // Clear the security context
        SecurityContextHolder.clearContext();

        // Delete refresh token if provided in a custom header or extracted from request
        if (authHeader != null && authHeader.startsWith("Bearer-Refresh ")) {
            String refreshToken = authHeader.substring(15);
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        LoginResponse response = LoginResponse.builder()
                .email(username)
                .message("Logout successful")
                .build();

        return ResponseEntity.ok(response);
    }

    // update user
    @PostMapping("/update-user")
    @Operation(summary = "Update user", description = "Update user details")
    @ApiResponse(responseCode = "200", description = "Successfully updated user")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<?> updateUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Users user) {
        try {
            String email = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    email = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(LoginResponse.builder().message("Invalid token").build());
                }
            }
            userWriteService.updateUser(user);
            return ResponseEntity.ok(LoginResponse.builder().message("User updated successfully").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.builder().message("Update user failed: " + e.getMessage()).build());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate a new access token using a valid refresh token")
    @ApiResponse(responseCode = "200", description = "Successfully refreshed token")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            String email = refreshTokenService.validateAndGetUserEmail(request.getRefreshToken());

            // Rotate refresh token
            refreshTokenService.deleteRefreshToken(request.getRefreshToken());
            String newRefreshToken = refreshTokenService.createRefreshToken(email);

            String newAccessToken = jwtUtil.generateToken(email);

            return ResponseEntity.ok(RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .message("Token refreshed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RefreshTokenResponse.builder().message(e.getMessage()).build());
        }
    }

    // does user exists
    @GetMapping("/does-user-exists")
    @Operation(summary = "Does user exists", description = "Check if user exists")
    @ApiResponse(responseCode = "200", description = "Successfully checked if user exists")
    @ApiResponse(responseCode = "404", description = "User not found")
    public boolean doesUserExists(@RequestBody final String email) throws UserNotFoundException {
        if (usersRepository.findByEmail(email).isPresent()) {
            return true;
        } else {
            throw new UserNotFoundException();
        }
    }
}
