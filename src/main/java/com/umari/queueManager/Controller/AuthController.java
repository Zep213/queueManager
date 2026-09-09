package com.umari.queueManager.Controller;

import com.umari.queueManager.config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.get("username"), body.get("password"))
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        String role = userDetails.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", userDetails.getUsername(),
                "role", role
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(401).build();
        }
        String role = userDetails.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "");
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "role", role
        ));
    }
}
