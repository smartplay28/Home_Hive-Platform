package com.example.UC_Backend.HelperFunctionIO.getAllSA;

/**
 * Function is used to give response when getAllSA used
 */

import com.example.UC_Backend.Users.ServiceAgent;
import java.util.ArrayList;

public class getAllSAResponse {
    private String message;
    private ArrayList<ServiceAgent> agents;

    public getAllSAResponse(String message, ArrayList<ServiceAgent> agents) {
        this.message = message;
        this.agents = agents;
    }

    public ArrayList<ServiceAgent> getAgents() {
        return agents;
    }

    public void setAgents(ArrayList<ServiceAgent> agents) {
        this.agents = agents;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
