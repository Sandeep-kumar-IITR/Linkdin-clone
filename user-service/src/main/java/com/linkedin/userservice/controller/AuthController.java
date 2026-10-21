package com.linkedin.userservice.controller;


import com.linkedin.userservice.dto.AuthResponse;
import com.linkedin.userservice.dto.LoginRequest;
import com.linkedin.userservice.dto.ResisterRequest;
import com.linkedin.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * resister new user
     */
    @PostMapping("/resister")
    public ResponseEntity<AuthResponse> resister(
            @Valid @RequestBody ResisterRequest request
            )
    {
        log.info("resister request received: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.resister(request));
    }

    /**
     * Login user
     */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request)
    {
        log.info("login request received: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }





}
