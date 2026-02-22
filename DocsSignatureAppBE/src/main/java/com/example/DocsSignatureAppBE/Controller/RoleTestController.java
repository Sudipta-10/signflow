package com.example.DocsSignatureAppBE.Controller;

import com.example.DocsSignatureAppBE.Entity.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class RoleTestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicAccess() {
        return ResponseEntity.ok("This is public access");
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<String> userAccess() {
        return ResponseEntity.ok("This is user access");
    }

    @GetMapping("/moderator")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<String> moderatorAccess() {
        return ResponseEntity.ok("This is moderator access");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminAccess() {
        return ResponseEntity.ok("This is admin access only");
    }

    @GetMapping("/roles-info")
    public ResponseEntity<String> rolesInfo() {
        StringBuilder info = new StringBuilder();
        for (Role role : Role.values()) {
            info.append(role.name())
                    .append(" - Authority: ").append(role.getAuthority())
                    .append(" - Description: ").append(role.getDescription())
                    .append("\n");
        }
        return ResponseEntity.ok(info.toString());
    }
}

