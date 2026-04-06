package com.raccord.rc.controller;

import com.raccord.rc.dto.LoginRequest;
import com.raccord.rc.dto.LoginResponse;
import com.raccord.rc.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 🔥 CORRECTO

public class AuthController {
    @GetMapping("/test")
    public String testPublico() {
        return "Endpoint público funcionando";

    }
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        boolean success = authService.login(request.getUsername(), request.getPassword());

        if(success){
            LoginResponse response = new LoginResponse("Login exitoso", "token123");
            return ResponseEntity.ok(response);
        }

        LoginResponse response = new LoginResponse("Credenciales incorrectas", null);
        return ResponseEntity.status(401).body(response);
    }
}