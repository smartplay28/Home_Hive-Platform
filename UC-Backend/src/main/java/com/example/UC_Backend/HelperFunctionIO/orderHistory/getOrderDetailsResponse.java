package com.example.UC_Backend.HelperFunctionIO.orderHistory;

/**
 * Function is used to give response when getAllSA used
 */
import com.example.UC_Backend.Order;
import java.util.ArrayList;

public class getOrderDetailsResponse {
    private String message;
    private ArrayList<Order> orders;

    public getOrderDetailsResponse(String message, ArrayList<Order> orders) {
        this.message = message;
        this.orders = orders;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setAgents(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
