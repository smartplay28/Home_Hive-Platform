package com.example.UC_Backend.HelperFunctionIO.giveSAOrders;

/**
 * Function is used to give request when getAllSA used
 */

public class giveSAOrdersRequest {
    private int agentId;

    public giveSAOrdersRequest() {
    } // Default constructor for deserialization

    public giveSAOrdersRequest(int agentId) {
        this.agentId = agentId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }
}
