package com.example.UC_Backend.HelperFunctionIO.loginAdmin;

/**
 * For giving back response to the frontend in json format
 */
public class loginAdminResponse {
    private String message;
    private int adminId;

    public loginAdminResponse(String message, int adminId) {
        this.message = message;
        this.adminId = adminId;
    }

    public String getMessage() {
        return message;
    }

    public int getAdminId() {
        return adminId;
    }
}