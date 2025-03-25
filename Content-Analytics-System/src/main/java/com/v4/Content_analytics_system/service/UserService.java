package com.v4.Content_analytics_system.service;

import com.v4.Content_analytics_system.model.entity.sql.User;
import com.v4.Content_analytics_system.repository.sql.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // To create a new user
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // To get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get USer By ID
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Find by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Save or update user
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // For security if the user exists (by username
    public boolean existsByUsername(String username) {
        System.out.println("Checking username: '" + username + "'");
        List<User> users = userRepository.findAll();
        System.out.println("All usernames in DB:");
        for (User user : users) {
            System.out.println("- '" + user.getUsername() + "'");
        }
        boolean exists = userRepository.existsByUsername(username);
        System.out.println("Exists result: " + exists);
        return exists;
    }

    // For security if the user exists (by email)
    public boolean existsByEmail(String email) {
        userRepository.existsByEmail(email);
        return true;
    }

    // To create new user
    public User createUser(String username, String password, String email, String name) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password) );
        user.setEmail(email);
        user.setName(name);

        // Creation date
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Setting default roles
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);

        // Save to database
        return userRepository.save(user);
    }
}
