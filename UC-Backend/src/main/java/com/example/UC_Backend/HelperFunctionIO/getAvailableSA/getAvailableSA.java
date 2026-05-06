package com.example.UC_Backend.HelperFunctionIO.getAvailableSA;

/**
 * This class is used to get all those agents which have the required skills
 */

import com.example.UC_Backend.Users.ServiceAgent;
import java.util.ArrayList;
import java.util.Arrays;

public class getAvailableSA {

    private ArrayList<ServiceAgent> agents = new ArrayList<>();

    public getAvailableSA() {
    }

    public getAvailableSA(ArrayList<ServiceAgent> agents) {

        this.agents = agents;
    }

    public ArrayList<ServiceAgent> findAvailableSA(String cartItem) {
        String service = cartItem;
        ArrayList<ServiceAgent> availableSA = new ArrayList<>();

        // TECHNICIAN
        if (service.substring(0, 3).equals("ACA")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Technician")) {
                    availableSA.add(agent);
                }
            }
        }
        // ELECTRICIAN
        else if (service.substring(0, 3).equals("ACE")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Electrician")) {
                    availableSA.add(agent);
                }
            }

        }
        // CARPENTER
        else if (service.substring(0, 2).equals("CP")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Carpenter")) {
                    availableSA.add(agent);
                }
            }
        }

        // GARDENER
        else if (service.substring(0, 2).equals("GA") || service.substring(0, 2).equals("PL")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Gardener")) {
                    availableSA.add(agent);
                }
            }
        }

        // PEST CONTROLLER
        else if (service.substring(0, 2).equals("PC")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Pest Controller")) {
                    availableSA.add(agent);
                }
            }
        }

        // MENS STYLER
        else if (service.substring(0, 2).equals("SM")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Men's Styler")) {
                    availableSA.add(agent);
                }
            }
        }

        // WOMENS STYLER
        else if (service.substring(0, 2).equals("SW")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Women's Styler")) {
                    availableSA.add(agent);
                }
            }
        }

        /*
         * else if(service.substring(0,2).equals("PD")) {
         * for(ServiceAgent agent: agents){
         * if(Arrays.asList(agent.getSkill()).contains("Painter"))
         * {
         * availableSA.add(agent);
         * }
         * }
         * }
         */

        // CLEANER
        else if (service.substring(0, 3).equals("CLE")) {
            for (ServiceAgent agent : agents) {
                if (Arrays.asList(agent.getSkill()).contains("Cleaner")) {
                    availableSA.add(agent);
                }
            }
        }
        return availableSA;
    }

}