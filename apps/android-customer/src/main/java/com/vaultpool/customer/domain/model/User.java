package com.vaultpool.customer.domain.model;

import java.util.Set;

/**
 * Domain model representing a User.
 * This is the core business entity used in the Domain Layer.
 */
public class User {

    private Long id;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Set<String> roles;
    private String createdAt;
    private String updatedAt;

    // Default constructor
    public User() {
    }

    // Builder pattern
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Check if user is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Builder class for User
     */
    public static class UserBuilder {
        private User user = new User();

        public UserBuilder id(Long id) {
            user.id = id;
            return this;
        }

        public UserBuilder firebaseUid(String firebaseUid) {
            user.firebaseUid = firebaseUid;
            return this;
        }

        public UserBuilder fullName(String fullName) {
            user.fullName = fullName;
            return this;
        }

        public UserBuilder email(String email) {
            user.email = email;
            return this;
        }

        public UserBuilder phone(String phone) {
            user.phone = phone;
            return this;
        }

        public UserBuilder status(String status) {
            user.status = status;
            return this;
        }

        public UserBuilder roles(Set<String> roles) {
            user.roles = roles;
            return this;
        }

        public UserBuilder createdAt(String createdAt) {
            user.createdAt = createdAt;
            return this;
        }

        public UserBuilder updatedAt(String updatedAt) {
            user.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return user;
        }
    }
}
