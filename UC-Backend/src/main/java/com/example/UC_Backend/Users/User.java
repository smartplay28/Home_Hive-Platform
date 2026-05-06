package com.example.UC_Backend.Users;

import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

/**
 * Base class for all user types.
 * Password is stored as BCrypt hash — never plain text.
 * createdAt is auto-set on construction for audit trail.
 */
public abstract class User {
    protected String name;

    @Indexed(unique = true)
    protected String email;

    protected String password; // BCrypt hashed

    protected Instant createdAt = Instant.now();
    protected boolean active = true;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isActive()   { return active; }
    public void setActive(boolean active) { this.active = active; }
}