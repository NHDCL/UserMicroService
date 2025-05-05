package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.service.AuthService;
import bt.nhdcl.usermicroservice.service.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user, HttpServletResponse response) {
        UserDetails userDetails = authService.login(user.getEmail(), user.getPassword());

        // Generate JWT
        String jwt = jwtUtil.generateToken(userDetails);

        // Create and set JWT cookie
        Cookie jwtCookie = new Cookie("JWT-TOKEN", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // ensure HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (jwtUtil.getTokenExpiration() / 1000));
        response.addCookie(jwtCookie);

        // Prepare JSON response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", jwt);
        responseMap.put("user", userDetails);

        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        // Expire JWT cookie
        Cookie jwtCookie = new Cookie("JWT-TOKEN", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);

        response.addCookie(jwtCookie);

        Map<String, String> logoutResponse = new HashMap<>();
        logoutResponse.put("message", "Logout successful");

        return ResponseEntity.ok(logoutResponse);
    }
}
