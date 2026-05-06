package com.example.UC_Backend.HelperFunctionIO.loginServiceAgent;

/**
 * Accepts data from frontend in json format
 */

public class LoginSARequest {
    private String email;
    private String password;

    public LoginSARequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
