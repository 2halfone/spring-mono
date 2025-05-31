package com.example.repository;

import com.example.model.User;
import com.example.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository for Database Operations
 * 
 * PHASE 2 - DATABASE INTEGRATION
 * JPA Repository for User entity operations
 * 
 * Features:
 * - Standard CRUD operations
 * - Custom queries for authentication
 * - Role-based queries
 * - Account status queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (for authentication)
     * @param username Username to search
     * @return Optional User
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email Email to search
     * @return Optional User
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     * @param username Username to search
     * @param email Email to search
     * @return Optional User
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     * @param username Username to check
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email Email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     * @param role Role to search
     * @return List of users with the role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    /**
     * Find users by multiple roles
     * @param roles List of roles to search
     * @return List of users with any of the roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN :roles")
    List<User> findByRolesIn(@Param("roles") List<Role> roles);

    /**
     * Find enabled users
     * @return List of enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find disabled users
     * @return List of disabled users
     */
    List<User> findByEnabledFalse();

    /**
     * Find users with verified email
     * @return List of users with verified email
     */
    List<User> findByEmailVerifiedTrue();

    /**
     * Find users with unverified email
     * @return List of users with unverified email
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * Find active users (enabled, non-expired, non-locked)
     * @return List of active users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.accountNonExpired = true AND u.accountNonLocked = true")
    List<User> findActiveUsers();

    /**
     * Count users by role
     * @param role Role to count
     * @return Number of users with the role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") Role role);

    /**
     * Count active users
     * @return Number of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.accountNonExpired = true AND u.accountNonLocked = true")
    long countActiveUsers();

    /**
     * Find users by partial username (case-insensitive)
     * @param username Partial username
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);

    /**
     * Find users by partial email (case-insensitive)
     * @param email Partial email
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);
}
