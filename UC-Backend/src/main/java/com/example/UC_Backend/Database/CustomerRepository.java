package com.example.UC_Backend.Database;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.UC_Backend.Users.Customer;

@Repository
// Repository used for accessing Customer data
public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByEmail(String email); // Find customer by email

    Optional<Customer> findByPhone(int phone); // Find customer by phone number

    Optional<Customer> findByCustomerId(int customerId);
}