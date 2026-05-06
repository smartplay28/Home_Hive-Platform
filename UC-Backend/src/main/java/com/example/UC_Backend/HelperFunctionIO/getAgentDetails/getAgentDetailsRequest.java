package com.example.UC_Backend.HelperFunctionIO.getAgentDetails;

/**
 * Gets agent id from frontend
 */
public class getAgentDetailsRequest {
    int agentId;

    public getAgentDetailsRequest() {
    }

    public getAgentDetailsRequest(int agentId) {
        this.agentId = agentId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }
}