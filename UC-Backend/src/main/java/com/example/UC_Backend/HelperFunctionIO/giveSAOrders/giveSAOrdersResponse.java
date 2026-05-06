package com.example.UC_Backend.HelperFunctionIO.giveSAOrders;

/**
 * Function is used to give response when getAllSA used
 */
import com.example.UC_Backend.Users.ServiceAgent;

public class giveSAOrdersResponse {
    private String message;
    private ServiceAgent agent;

    public giveSAOrdersResponse() {
    } // for deserialization

    public giveSAOrdersResponse(String message, ServiceAgent agent) {
        this.message = message;
        this.agent = agent;
    }

    public String getMessage() {
        return message;
    }

    public ServiceAgent getAgent() {
        return agent;
    }

    public void setAgents(ServiceAgent agent) {
        this.agent = agent;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}