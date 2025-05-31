package com.example.service;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserService - Business Logic for User Management
 * 
 * Features:
 * - User registration with encrypted password hashing
 * - User authentication with password validation
 * - Role-based access control (USER, ADMIN, MODERATOR)
 * - Account status management (enabled, locked, expired)
 * - Audit trail with creation and modification timestamps
 * 
 * Security:
 * - Password encoder interface for password encryption
 * - Email uniqueness validation
 * - Username uniqueness validation
 * - Transaction-safe operations
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user with encrypted password
     * 
     * @param username Unique username
     * @param email Unique email address
     * @param password Raw password (will be encrypted)
     * @param role User role (default: USER)
     * @return Created user entity
     * @throws RuntimeException if username or email already exists
     */
    public User registerUser(String username, String email, String password, Role role) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists: " + email);
        }        // Create new user with encrypted password
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt encryption
        
        // Set role (use Set<Role> instead of single role)
        if (role != null) {
            user.addRole(role);
        } else {
            user.addRole(Role.USER); // Default to USER role
        }
        
        // Set account status (all enabled by default)
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        
        // Audit fields are auto-populated by JPA @CreatedDate/@LastModifiedDate
        
        return userRepository.save(user);
    }

    /**
     * Register a new user with default USER role
     */
    public User registerUser(String username, String email, String password) {
        return registerUser(username, email, password, Role.USER);
    }

    /**
     * Authenticate user with username and password
     * 
     * @param username Username or email
     * @param password Raw password
     * @return User entity if authentication successful
     * @throws RuntimeException if authentication fails
     */
    public User authenticateUser(String username, String password) {
        // Try to find by username first, then by email
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(username);
        }

        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found: " + username);
        }

        User user = userOpt.get();        // Check account status
        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled: " + username);
        }
        if (!user.getAccountNonLocked()) {
            throw new RuntimeException("User account is locked: " + username);
        }
        if (!user.getAccountNonExpired()) {
            throw new RuntimeException("User account is expired: " + username);
        }
        if (!user.getCredentialsNonExpired()) {
            throw new RuntimeException("User credentials are expired: " + username);
        }

        // Validate password with BCrypt
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password for user: " + username);
        }

        return user;
    }

    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get all users (admin function)
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }    /**
     * Update user role (admin function)
     */
    public User updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.getRoles().clear(); // Clear existing roles
        user.addRole(newRole);   // Add new role
        return userRepository.save(user);
    }

    /**
     * Update user password with BCrypt encryption
     */
    public User updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }    /**
     * Lock/unlock user account (admin function)
     */
    public User setAccountLocked(Long userId, boolean locked) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setAccountNonLocked(!locked);
        return userRepository.save(user);
    }

    /**
     * Enable/disable user account (admin function)
     */
    public User setAccountEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    /**
     * Delete user (admin function)
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Check if username is available
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.findByUsername(username).isPresent();
    }

    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.findByEmail(email).isPresent();
    }

    /**
     * Count total users
     */
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    /**
     * Find users by role
     */
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
}
