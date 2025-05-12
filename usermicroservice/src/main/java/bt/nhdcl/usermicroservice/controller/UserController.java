package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.exception.UserNotFoundException;
import bt.nhdcl.usermicroservice.entity.Role;
import bt.nhdcl.usermicroservice.service.UserService;
import bt.nhdcl.usermicroservice.service.RoleService;
import bt.nhdcl.usermicroservice.service.CloudinaryService;
import bt.nhdcl.usermicroservice.service.EmailService; // Add email service

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
    private final RoleService roleService;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService; // Inject EmailService
    private static final String DEFAULT_PASSWORD = "Password"; // Define default password constant

    @Autowired
    public UserController(UserService userService, RoleService roleService,
            CloudinaryService cloudinaryService, EmailService emailService) {
        this.userService = userService;
        this.roleService = roleService;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService; // Initialize EmailService
    }

    // Create a new user with an image upload
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createUser(
            @RequestParam("email") @Valid @NotNull @Email String email,
            @RequestParam("password") @Valid @NotNull String password,
            @RequestParam("name") @Valid @NotNull String name,
            @RequestParam("employeeId") @Valid @NotNull String employeeId,
            @RequestParam("academyId") @Valid @NotNull String academyId,
            @RequestParam("departmentId") @Valid @NotNull String departmentId,
            @RequestParam("roleId") @Valid @NotNull String roleId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            // Check if email already exists and user is ACTIVE
            Optional<User> existingUserOpt = userService.getUserByEmail(email);
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                if (existingUser.isEnabled()) {
                    // User exists and is active - reject registration
                    return ResponseEntity.badRequest().body("Email is already in use by an active user.");
                } else {
                    // User exists but is soft-deleted - hard delete them to allow re-registration
                    userService.permanentlyDeleteUser(existingUser.getUserId());
                }
            }

            // Check if employeeId already exists
            if (userService.isEmployeeIdDuplicate(employeeId)) {
                return ResponseEntity.badRequest().body("Employee ID is already in use.");
            }

            // Fetch the Role using roleId from the RoleService
            Optional<Role> roleOptional = roleService.getRoleById(roleId);
            if (roleOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Role not found.");
            }
            Role role = roleOptional.get();

            String imageUrl = null;

            // Upload image to Cloudinary (if image is provided)
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadUserImage(imageFile);
            }

            // Create and save the user with role and optional image
            User user = new User(email, password, name, employeeId, academyId, departmentId, role, imageUrl);
            user.setEnabled(true);
            User savedUser = userService.save(user);

            String subject = "🎉 Account Created - Welcome to the System";

            String message = "<div style='font-family: Arial, sans-serif; max-width: 600px; padding: 20px; " +
                    "border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;'>" +
                    "<h2 style='color: #4A7F68;'>Welcome, " + name + "!</h2>" +
                    "<p>We are pleased to inform you that your account has been successfully created.</p>" +
                    "<p style='font-size: 16px;'>Your <strong>Temporary Password</strong> is:</p>" +
                    "<div style='font-size: 18px; font-weight: bold; color: #ffffff; background: #4A7F68; " +
                    "padding: 10px; border-radius: 5px; text-align: center;'>" + password + "</div>" +
                    "<p>Please log in using the above credentials and update your password at your earliest convenience to ensure your account remains secure.</p>"
                    +
                    "<p>If you encounter any issues or have questions, feel free to contact our support team.</p>" +
                    "<hr style='border: none; border-top: 1px solid #ddd;'/>" +
                    "<p style='font-size: 14px; color: #777;'>Best regards,<br/><strong>NHDCL</strong></p>" +
                    "</div>";

            boolean emailSent = emailService.sendEmail(email, subject, message);

            if (!emailSent) {
                // Log the failure but don't prevent user creation
                System.err.println("Failed to send welcome email to " + email);
            }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteUser(@PathVariable String id) {
        try {
            // Call the soft delete service method
            userService.deleteUserById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
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

    // Check if an email is duplicate
    @GetMapping("/checkDuplicateEmail")
    public ResponseEntity<Boolean> checkDuplicateEmail(@RequestParam String email) {
        // Only check for ACTIVE users with this email
        Optional<User> userOptional = userService.getUserByEmail(email);
        boolean isDuplicate = userOptional.isPresent() && userOptional.get().isEnabled();
        return ResponseEntity.ok(isDuplicate);
    }

    @PutMapping("/image")
    public ResponseEntity<Map<String, String>> updateUserImageByEmail(
            @RequestParam("email") @Valid @NotNull @Email String email,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            Optional<User> existingUserOptional = userService.getUserByEmail(email);

            if (existingUserOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.SC_NOT_FOUND)
                        .body(Map.of("error", "User not found."));
            }

            User existingUser = existingUserOptional.get();

            // Keep existing image URL unless new one is provided
            String imageUrl = existingUser.getImage();
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadUserImage(imageFile);
            }

            // Update user image
            existingUser.setImage(imageUrl);
            userService.updateUser(existingUser.getUserId(), existingUser);

            return ResponseEntity.ok(Map.of("message", "User image updated successfully."));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading image: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    // Update user enabled status
    @PutMapping("/{id}/enabled")
    public ResponseEntity<?> updateUserEnabledStatus(@PathVariable String id,
            @RequestBody Map<String, Boolean> requestBody) {
        Boolean enabled = requestBody.get("enabled");
        if (enabled == null) {
            // Return a failure response with success: false
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing 'enabled' field in request"));
        }
        try {
            userService.updateUserEnabledStatus(id, enabled);
            // Return a success message with success: true
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (UserNotFoundException e) {
            // Return a failure response if the user is not found
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Forgot Password - Generate OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
        }

        // Generate OTP and handle the result
        boolean otpSent = userService.generateOtp(email);

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