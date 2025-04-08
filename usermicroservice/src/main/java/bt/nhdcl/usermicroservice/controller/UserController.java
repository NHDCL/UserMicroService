package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.entity.Role;
import bt.nhdcl.usermicroservice.service.UserService;
import bt.nhdcl.usermicroservice.service.RoleService; // Import RoleService
import bt.nhdcl.usermicroservice.service.CloudinaryService;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService; // Inject RoleService to handle role lookup
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserController(UserService userService, RoleService roleService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.roleService = roleService; // Initialize RoleService
        this.cloudinaryService = cloudinaryService;
    }

    // Create a new user with an image upload
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createUser(
            @RequestParam("email") @Valid @NotNull @Email String email, // Email validation
            @RequestParam("password") @Valid @NotNull String password,
            @RequestParam("name") @Valid @NotNull String name,
            @RequestParam("academyId") @Valid @NotNull String academyId,
            @RequestParam("departmentId") @Valid @NotNull String departmentId,
            @RequestParam("roleId") @Valid @NotNull String roleId, // Accept roleId as input
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            // Check if email already exists
            if (userService.isEmailDuplicate(email)) {
                return ResponseEntity.badRequest().body("Email is already in use.");
            }

            // Fetch the Role using roleId from the RoleService
            Optional<Role> roleOptional = roleService.getRoleById(roleId);
            if (roleOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Role not found.");
            }
            Role role = roleOptional.get(); // Get the Role object

            String imageUrl = null;

            // Upload image to Cloudinary (if image is provided)
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadUserImage(imageFile);
            }

            // Create and save the user with role and optional image
            User user = new User(email, password, name, academyId, departmentId, role, imageUrl); // Set the Role object
            User savedUser = userService.save(user);

            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error uploading image: " + e.getMessage());
        }
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            // Return a 400 Bad Request with a custom error message
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required"));
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(Map.of("success", true, "user", userOptional.get()));
        } else {
            // Return a 404 Not Found if the user is not found
            return ResponseEntity.notFound()
                    .build();
        }
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    // Check if an email is duplicate
    @GetMapping("/checkDuplicateEmail")
    public ResponseEntity<Boolean> checkDuplicateEmail(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    @PutMapping("/image")
    public ResponseEntity<String> updateUserImageByEmail(
            @RequestParam("email") @Valid @NotNull @Email String email,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            Optional<User> existingUserOptional = userService.getUserByEmail(email);

            if (existingUserOptional.isEmpty()) {
                return ResponseEntity.notFound().build(); // User not found
            }

            User existingUser = existingUserOptional.get();

            // If an image file is provided, upload it to Cloudinary
            String imageUrl = existingUser.getImage(); // Keep existing image URL if no new image is provided
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadUserImage(imageFile); // Update image URL if new image is uploaded
            }

            // Update the user's image (keep other details unchanged)
            existingUser.setImage(imageUrl);

            // Save the updated user with the new image
            userService.updateUser(existingUser.getUserId(), existingUser); // Save using user ID

            return ResponseEntity.ok("User image updated successfully."); // Return a success message
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error uploading image: " + e.getMessage());
        }
    }

    // Update user enabled status
    @PutMapping("/{id}/enabled")
    public ResponseEntity<?> updateUserEnabledStatus(@PathVariable String id,
            @RequestBody Map<String, Boolean> requestBody) {
        Boolean enabled = requestBody.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body("Missing 'enabled' field in request");
        }
        userService.updateUserEnabledStatus(id, enabled);
        return ResponseEntity.ok().build();
    }

    // Forgot Password - Generate OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
        }

        // Generate OTP and handle the result
        boolean otpSent = userService.generateOtp(email); // Assuming this method returns true if OTP was sent
                                                          // successfully

        if (otpSent) {
            return ResponseEntity.ok(Map.of("success", true, "message", "OTP sent to email."));
        } else {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Failed to send OTP."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");
        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and OTP are required"));
        }
        boolean isValid = userService.validateOtp(email, otp);
        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "OTP is valid."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid OTP."));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required"));
        }

        // Check if the user exists
        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No user found with this email."));
        }

        // Generate and send a new OTP
        boolean otpSent = userService.generateOtp(email);
        if (!otpSent) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to generate OTP. Please try again later."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "New OTP sent successfully."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");
        String newPassword = requestBody.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email, OTP, and new password are required"));
        }

        // Validate OTP first
        boolean isValidOtp = userService.validateOtp(email, otp);
        if (!isValidOtp) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid OTP."));
        }

        // Proceed with password reset
        boolean success = userService.resetPassword(email, newPassword);
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset successful."));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to reset password."));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String oldPassword = requestBody.get("oldPassword");
        String newPassword = requestBody.get("newPassword");

        if (email == null || oldPassword == null || newPassword == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email, old password, and new password are required."));
        }

        try {
            boolean success = userService.changePassword(email, oldPassword, newPassword);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Password change failed. Please check your credentials."));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

}
