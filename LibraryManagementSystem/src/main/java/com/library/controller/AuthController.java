package com.library.controller;

import com.library.dto.JwtResponse;
import com.library.dto.LoginRequest;
import com.library.dto.MessageResponse;
import com.library.dto.RegisterRequest;
import com.library.model.Role;
import com.library.model.User;
import com.library.security.JwtUtils;
import com.library.security.UserDetailsImpl;
import com.library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            User user = User.builder()
                    .username(signUpRequest.getUsername())
                    .password(signUpRequest.getPassword())
                    .email(signUpRequest.getEmail())
                    .phone(signUpRequest.getPhone())
                    .role(Role.MEMBER) // Default role
                    .build();

            userService.registerUser(user);

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/admin/signup")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            User adminUser = User.builder()
                    .username(signUpRequest.getUsername())
                    .password(signUpRequest.getPassword())
                    .email(signUpRequest.getEmail())
                    .phone(signUpRequest.getPhone())
                    .role(Role.ADMIN) // Admin role
                    .build();

            userService.registerUser(adminUser);

            return ResponseEntity.ok(new MessageResponse("Admin registered successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
