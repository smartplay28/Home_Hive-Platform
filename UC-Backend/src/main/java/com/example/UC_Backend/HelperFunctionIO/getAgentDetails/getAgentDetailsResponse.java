package com.example.UC_Backend.HelperFunctionIO.getAgentDetails;

/**
 * For giving back response to the frontend in json format
 */
import com.example.UC_Backend.Users.ServiceAgent;

public class getAgentDetailsResponse {
    private String message;
    private ServiceAgent agent;

    public getAgentDetailsResponse(String message, ServiceAgent agent) {
        this.message = message;
        this.agent = agent;
    }

    public String getMessage() {
        return message;
    }

    public ServiceAgent getAgent() {
        return this.agent;
    }

}