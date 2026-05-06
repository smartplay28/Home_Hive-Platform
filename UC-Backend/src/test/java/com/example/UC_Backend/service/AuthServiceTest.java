package com.example.UC_Backend.service;

import com.example.UC_Backend.Database.AdminRepository;
import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Users.Admin;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.Users.ServiceAgent;
import com.example.UC_Backend.dto.auth.LoginRequest;
import com.example.UC_Backend.dto.auth.LoginResponse;
import com.example.UC_Backend.dto.auth.RegisterAgentRequest;
import com.example.UC_Backend.dto.auth.RegisterCustomerRequest;
import com.example.UC_Backend.exception.ConflictException;
import com.example.UC_Backend.exception.UnauthorizedException;
import com.example.UC_Backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private CustomerRepository customerRepo;
    @Mock private ServiceAgentRepository agentRepo;
    @Mock private AdminRepository adminRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {}

    @Test
    void registerCustomer_Success() {
        RegisterCustomerRequest req = new RegisterCustomerRequest("Test", "test@example.com", 1234567890, "password");
        when(customerRepo.findByEmail(req.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.password())).thenReturn("hashed_password");
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyInt())).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh_token");

        LoginResponse response = authService.registerCustomer(req);

        assertNotNull(response);
        assertEquals("CUSTOMER", response.role());
        assertEquals("access_token", response.accessToken());
        verify(customerRepo, times(1)).save(any(Customer.class));
    }

    @Test
    void registerCustomer_EmailAlreadyExists_ThrowsConflict() {
        RegisterCustomerRequest req = new RegisterCustomerRequest("Test", "test@example.com", 1234567890, "password");
        Customer existingCustomer = new Customer("Test", "test@example.com", 1234567890, "hashed");
        when(customerRepo.findByEmail(req.email())).thenReturn(Optional.of(existingCustomer));

        assertThrows(ConflictException.class, () -> authService.registerCustomer(req));
        verify(customerRepo, never()).save(any(Customer.class));
    }

    @Test
    void loginCustomer_Success() {
        LoginRequest req = new LoginRequest("test@example.com", "password");
        Customer customer = new Customer("Test", "test@example.com", 1234567890, "hashed_password");
        
        when(customerRepo.findByEmail(req.email())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(req.password(), customer.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyInt())).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh_token");

        LoginResponse response = authService.loginCustomer(req);

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("CUSTOMER", response.role());
    }

    @Test
    void loginCustomer_WrongPassword_ThrowsUnauthorized() {
        LoginRequest req = new LoginRequest("test@example.com", "wrong_password");
        Customer customer = new Customer("Test", "test@example.com", 1234567890, "hashed_password");
        
        when(customerRepo.findByEmail(req.email())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(req.password(), customer.getPassword())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.loginCustomer(req));
    }

    @Test
    void loginAdmin_Success() {
        LoginRequest req = new LoginRequest("admin@example.com", "password");
        Admin admin = new Admin(1, "Admin", "admin@example.com", "hashed_password", "SECRET123");
        
        when(adminRepo.findByEmail(req.email())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(req.password(), admin.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyInt())).thenReturn("access_token");

        LoginResponse response = authService.loginAdmin(req, "SECRET123");

        assertNotNull(response);
        assertEquals("ADMIN", response.role());
    }

    @Test
    void loginAdmin_WrongAccessCode_ThrowsUnauthorized() {
        LoginRequest req = new LoginRequest("admin@example.com", "password");
        Admin admin = new Admin(1, "Admin", "admin@example.com", "hashed_password", "SECRET123");
        
        when(adminRepo.findByEmail(req.email())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(req.password(), admin.getPassword())).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> authService.loginAdmin(req, "WRONG"));
    }
}
