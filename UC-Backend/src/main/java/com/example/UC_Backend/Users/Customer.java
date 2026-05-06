package com.example.UC_Backend.Users;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;
import java.util.ArrayList;

import com.example.UC_Backend.Extra.ExtraFunctions;

/**
 * Contains details of a customer
 */
@Document(collection = "customers")
public class Customer extends User {
    ExtraFunctions func = new ExtraFunctions();

    @Id
    private String id = UUID.randomUUID().toString(); // Unique value for each document;
    private int customerId;
    private int phone;
    private ArrayList<String> shoppingCart = new ArrayList<String>();
    private String location;

    public Customer(String name, String email, int phone, String password) {
        super(name, email, password);
        this.customerId = func.generateID();
        this.phone = phone;
    }

    public String getName() {
        return super.name;
    }

    public int getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return super.password;
    }

    public int getCustomerId() {
        return customerId;
    }

    public ArrayList<String> getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ArrayList<String> shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public void addServiveToCart(String ServiceID) {
        this.shoppingCart.add(ServiceID);
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
