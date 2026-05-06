package com.example.UC_Backend.service;

import com.example.UC_Backend.Database.AdminRepository;
import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.dto.auth.*;
import com.example.UC_Backend.exception.ConflictException;
import com.example.UC_Backend.exception.UnauthorizedException;
import com.example.UC_Backend.security.JwtTokenProvider;
import com.example.UC_Backend.Users.Admin;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.Users.ServiceAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles all authentication logic:
 * - Customer/Agent/Admin registration with BCrypt password hashing
 * - Login with password verification → JWT token issuance
 * - Replaces all loginCustomer, loginAdmin, loginSA, addCustomer, addSA in Helper.java
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CustomerRepository customerRepo;
    private final ServiceAgentRepository agentRepo;
    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(CustomerRepository customerRepo,
                       ServiceAgentRepository agentRepo,
                       AdminRepository adminRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.customerRepo = customerRepo;
        this.agentRepo = agentRepo;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ─── Customer ────────────────────────────────────────────────────────────

    public LoginResponse registerCustomer(RegisterCustomerRequest req) {
        if (customerRepo.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("An account with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(req.password());
        Customer customer = new Customer(req.name(), req.email(), (int) req.phone(), hashedPassword);
        customerRepo.save(customer);
        log.info("auth.customer.register customerId={} email={}", customer.getCustomerId(), req.email());

        String accessToken = jwtTokenProvider.generateAccessToken(req.email(), "CUSTOMER", customer.getCustomerId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(req.email());
        return new LoginResponse(accessToken, refreshToken, "CUSTOMER", customer.getCustomerId(), customer.getName(), req.email());
    }

    public LoginResponse loginCustomer(LoginRequest req) {
        Optional<Customer> customerOpt = customerRepo.findByEmail(req.email());
        if (customerOpt.isEmpty()) {
            throw new UnauthorizedException("No account found with this email.");
        }

        Customer customer = customerOpt.get();
        if (!passwordEncoder.matches(req.password(), customer.getPassword())) {
            log.warn("auth.customer.login.failed email={} reason=wrong_password", req.email());
            throw new UnauthorizedException("Incorrect password.");
        }

        log.info("auth.customer.login.success customerId={}", customer.getCustomerId());
        String accessToken = jwtTokenProvider.generateAccessToken(req.email(), "CUSTOMER", customer.getCustomerId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(req.email());
        return new LoginResponse(accessToken, refreshToken, "CUSTOMER", customer.getCustomerId(), customer.getName(), req.email());
    }

    // ─── Service Agent ────────────────────────────────────────────────────────

    public LoginResponse registerAgent(RegisterAgentRequest req) {
        if (agentRepo.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("An agent account with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(req.password());
        ServiceAgent agent = new ServiceAgent(req.name(), req.email(), hashedPassword,
                req.skill(), req.range(), req.location());
        agentRepo.save(agent);
        log.info("auth.agent.register agentId={} email={}", agent.getAgentId(), req.email());

        String accessToken = jwtTokenProvider.generateAccessToken(req.email(), "AGENT", agent.getAgentId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(req.email());
        return new LoginResponse(accessToken, refreshToken, "AGENT", agent.getAgentId(), agent.getName(), req.email());
    }

    public LoginResponse loginAgent(LoginRequest req) {
        Optional<ServiceAgent> agentOpt = agentRepo.findByEmail(req.email());
        if (agentOpt.isEmpty()) {
            throw new UnauthorizedException("No agent account found with this email.");
        }

        ServiceAgent agent = agentOpt.get();
        if (!passwordEncoder.matches(req.password(), agent.getPassword())) {
            log.warn("auth.agent.login.failed email={} reason=wrong_password", req.email());
            throw new UnauthorizedException("Incorrect password.");
        }

        log.info("auth.agent.login.success agentId={}", agent.getAgentId());
        String accessToken = jwtTokenProvider.generateAccessToken(req.email(), "AGENT", agent.getAgentId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(req.email());
        return new LoginResponse(accessToken, refreshToken, "AGENT", agent.getAgentId(), agent.getName(), req.email());
    }

    public LoginResponse registerAdmin(RegisterAgentRequest req) {
        if (adminRepo.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("An admin account with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(req.password());
        int newAdminId = Math.abs(java.util.UUID.randomUUID().hashCode());
        Admin admin = new Admin(newAdminId, req.name(), req.email(), hashedPassword, "");
        adminRepo.save(admin);
        log.info("auth.admin.register adminId={} email={}", admin.getAdminId(), req.email());

        String accessToken = jwtTokenProvider.generateAccessToken(req.email(), "ADMIN", admin.getAdminId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(req.email());
        return new LoginResponse(accessToken, refreshToken, "ADMIN", admin.getAdminId(), admin.getName(), req.email());
    }

    public LoginResponse loginAdmin(LoginRequest req) {
        Optional<Admin> adminOpt = adminRepo.findByEmail(req.email());
        if (adminOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid credentials.");
        }

        Admin admin = adminOpt.get();
        if (!passwordEncoder.matches(req.password(), admin.getPassword())) {
            log.warn("auth.admin.login.failed email={}", req.email());
            throw new UnauthorizedException("Invalid credentials.");
        }

        log.info("auth.admin.login.success adminId={}", admin.getAdminId());
        String accessToken = jwtTokenProvider.generateAccessToken(req.email(), "ADMIN", admin.getAdminId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(req.email());
        return new LoginResponse(accessToken, refreshToken, "ADMIN", admin.getAdminId(), admin.getName(), req.email());
    }
}
