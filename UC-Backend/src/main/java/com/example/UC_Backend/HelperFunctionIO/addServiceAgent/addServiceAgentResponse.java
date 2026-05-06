package com.example.UC_Backend.HelperFunctionIO.addServiceAgent;

/**
 * For giving back response to the frontend in json format
 */
public class addServiceAgentResponse {
    private String message;
    private int agentId;

    public addServiceAgentResponse(String message, int agentId) {
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
