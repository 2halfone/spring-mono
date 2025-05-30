package com.example.dto;

/**
 * DTO for JWT authentication response.
 */
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String roles;
    private Long expiresIn;

    // Constructors
    public JwtResponse() {}

    public JwtResponse(String token, String username, String roles, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='[PROTECTED]'" +
                ", type='" + type + '\'' +
                ", username='" + username + '\'' +
                ", roles='" + roles + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
