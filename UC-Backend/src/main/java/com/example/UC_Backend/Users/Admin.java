package com.example.UC_Backend.Users;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * Contains details of Admin
 */
@Document(collection = "admins")
public class Admin extends User {
    @Id
    private String id = UUID.randomUUID().toString(); // Unique value for each document;
    private int adminId;
    private String accesscode;

    public Admin(int adminId, String name, String email, String password, String accesscode) {
        super(name, email, password);
        this.accesscode = accesscode;
        this.adminId = adminId;

    }

    public String getName() {
        return super.name;
    }

    public String getEmail() {
        return super.email;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessCode() {
        return accesscode;
    }

    public int getAdminId() {
        return adminId;
    }
}