package com.syfe.pfm.controller;

import com.syfe.pfm.dto.LoginRequest;
import com.syfe.pfm.dto.MessageResponse;
import com.syfe.pfm.dto.RegisterRequest;
import com.syfe.pfm.dto.RegisterResponse;
import com.syfe.pfm.model.User;
import com.syfe.pfm.repository.UserRepository;
import com.syfe.pfm.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller handling user registration and JWT-based login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, 
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            // Return 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Username/Email already exists"));
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhoneNumber()
        );

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse("User registered successfully", savedUser.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, 
                                   jakarta.servlet.http.HttpServletRequest httpRequest, 
                                   jakarta.servlet.http.HttpServletResponse httpResponse) {
        // Authenticate credentials
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        // Save to Security Context for Session Cookies
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        org.springframework.security.web.context.HttpSessionSecurityContextRepository securityContextRepository = new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        
        String jwt = tokenProvider.generateToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", jwt);

        return ResponseEntity.ok(response);
    }
}
