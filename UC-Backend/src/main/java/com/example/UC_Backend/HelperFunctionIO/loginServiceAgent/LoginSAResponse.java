package com.example.UC_Backend.HelperFunctionIO.loginServiceAgent;

public class LoginSAResponse {
    private String message;
    private int agentId;

    public LoginSAResponse(String message, int agentId) {
        this.message = message;
        this.agentId = agentId;
    }
    public String getMessage() {
        return message;
    }
    public int getAgentId() {
        return agentId;
    }
}
