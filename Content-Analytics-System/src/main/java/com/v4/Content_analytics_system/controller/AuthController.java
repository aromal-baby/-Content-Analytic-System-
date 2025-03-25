package com.v4.Content_analytics_system.controller;

import com.v4.Content_analytics_system.exception.ResourceNotFoundException;
import com.v4.Content_analytics_system.model.entity.sql.User;
import com.v4.Content_analytics_system.security.JwtTokenProvider;
import com.v4.Content_analytics_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    // For login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        System.out.println("Login attempt: " + loginRequest.getUsername());
        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Set the authentication in the context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // generating JWT token
            String jwtToken = jwtTokenProvider.generateToken(authentication);
            System.out.println("Generated JWT token for user: " + loginRequest.getUsername());

            User user = userService.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();

            response.put("token", jwtToken);

            // Add user details
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("name", user.getName());
            response.put("user", userMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    // To register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUprequest signupRequest) {

        System.out.println("Register attempt: " + signupRequest.getUsername());

        boolean usernameExists = userService.existsByUsername(signupRequest.getUsername());
        System.out.println("Username exists check: " + usernameExists);

        if (usernameExists) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = userService.createUser(
                signupRequest.getUsername(),
                signupRequest.getPassword(),
                signupRequest.getEmail(),
                signupRequest.getName()
        );

        return ResponseEntity.ok("User registered successfully!");
    }

    // Profile page
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("name", user.getName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user data: " + e.getMessage());
        }
    }

    /** Inner classes for Helping the endpoint methods
     * Instead of creating an authentication service class due to the time constraints
     */

    // For responses and request

    public static class LoginRequest {
        private String username;
        private String password;

        // GETTERS & SETTERS

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class SignUprequest {
        private String username;
        private String password;
        private String email;
        private String name;

        // GETTERS & SETTERS
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    /* For summing up the user details
    public static class UserSummary {
        private Long id;
        private String username;
        private String name;

        public UserSummary(Long id, String username, String name) {
            this.id = id;
            this.username = username;
            this.name = name;
        }

        // GETTERS
        public Long getId() {
            return id;
        }
        public String getUsername() {
            return username;
        }
        public String getName() {
            return name;
        }
    }

     */
}
