package com.example.DocsSignatureAppBE.Controller;

import com.example.DocsSignatureAppBE.Entity.Role;
import com.example.DocsSignatureAppBE.Entity.User;
import com.example.DocsSignatureAppBE.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> changeUserRole(@PathVariable Long userId, @RequestParam Role newRole) {
        User updatedUser = authService.changeUserRole(userId, newRole);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> getAdminDashboard() {
        return ResponseEntity.ok("Welcome to Admin Dashboard");
    }
}

