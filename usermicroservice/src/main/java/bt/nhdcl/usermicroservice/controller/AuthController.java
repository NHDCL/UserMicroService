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
import jakarta.servlet.http.HttpServletRequest;
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

        String jwt = jwtUtil.generateToken(userDetails);

        Cookie jwtCookie = new Cookie("JWT-TOKEN", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour

        response.addCookie(jwtCookie);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("jwt", jwt);
        responseMap.put("user", userDetails);

        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/loginWithCookie")
    public ResponseEntity<Map<String, Object>> loginWithCookie(@RequestBody User user, HttpServletResponse response) {
        UserDetails userDetails = authService.login(user.getEmail(), user.getPassword());

        String jwt = jwtUtil.generateToken(userDetails);

        Cookie jwtCookie = new Cookie("JWT-TOKEN", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour

        response.addCookie(jwtCookie);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", userDetails);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        // Expire the JWT cookie
        Cookie jwtCookie = new Cookie("JWT-TOKEN", null);
        jwtCookie.setMaxAge(0); // Set expiration to immediately remove it
        jwtCookie.setPath("/"); // Ensure it's cleared for all paths
        jwtCookie.setHttpOnly(true); // Prevent JavaScript access
        jwtCookie.setSecure(true); // Send over HTTPS only

        response.addCookie(jwtCookie);

        // Return logout success message
        Map<String, String> logoutResponse = new HashMap<>();
        logoutResponse.put("message", "Logout successful");

        return ResponseEntity.ok(logoutResponse);
    }

}
