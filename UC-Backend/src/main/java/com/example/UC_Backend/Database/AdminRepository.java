package com.example.UC_Backend.Database;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.UC_Backend.Users.Admin;

@Repository
// Repository used for accessing Admin Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findByEmail(String email); // Find customer by email
}