package com.example.UC_Backend.HelperFunctionIO.loginAdmin;

/**
 * For taking input from the frontend in json format and converting it into java
 * object of this class.
 */

public class loginAdminRequest {
    private String email;
    private String password;
    private String accesscode;

    public loginAdminRequest(String email, String password, String accesscode) {
        this.email = email;
        this.password = password;
        this.accesscode = accesscode;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessCode() {
        return accesscode;
    }
}