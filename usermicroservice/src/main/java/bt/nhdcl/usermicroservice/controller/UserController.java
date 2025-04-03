package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.entity.Role;
import bt.nhdcl.usermicroservice.service.UserService;
import bt.nhdcl.usermicroservice.service.RoleService; // Import RoleService
import bt.nhdcl.usermicroservice.service.CloudinaryService;
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

    // Update user details
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        // Extract roleId and roleName from updatedUser
        String roleId = updatedUser.getRole().getRoleId();
        String roleName = updatedUser.getRole().getName(); // Assuming getName() is the correct method for role name

        // Assuming roleService gets the role based on roleId
        Optional<Role> roleOptional = roleService.getRoleById(roleId);
        if (roleOptional.isEmpty()) {
            // Return a bad request response with an error message
            return ResponseEntity.badRequest().body(null); // Return null in the body if the role is not found
        }

        // Get the Role object from roleService
        Role role = roleOptional.get();
        role.setName(roleName); // Set the roleName (if necessary)

        // Set the updated role to the user
        updatedUser.setRole(role);

        // Now update the user
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user); // Return the updated user object in the response
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
            return ResponseEntity.badRequest().body("Email is required");
        }
        userService.generateOtp(email);
        return ResponseEntity.ok("OTP sent to email.");
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");
        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body("Email and OTP are required");
        }
        boolean isValid = userService.validateOtp(email, otp);
        return isValid ? ResponseEntity.ok("OTP is valid.") : ResponseEntity.badRequest().body("Invalid OTP.");
    }

    // Resend OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        // Check if the user exists
        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("No user found with this email.");
        }

        // Generate and send a new OTP
        userService.generateOtp(email);
        return ResponseEntity.ok("New OTP sent successfully.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");
        String newPassword = requestBody.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Email, OTP, and new password are required");
        }

        // Validate OTP first
        boolean isValidOtp = userService.validateOtp(email, otp);
        if (!isValidOtp) {
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }

        // Proceed with password reset
        String success = userService.resetPassword(email, newPassword);
        return success != null ? ResponseEntity.ok("Password reset successful.")
                : ResponseEntity.badRequest().body("Failed to reset password.");
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String oldPassword = requestBody.get("oldPassword");
        String newPassword = requestBody.get("newPassword");

        if (email == null || oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Email, old password, and new password are required.");
        }

        try {
            boolean success = userService.changePassword(email, oldPassword, newPassword);
            return success ? ResponseEntity.ok("Password changed successfully.")
                    : ResponseEntity.badRequest().body("Password change failed.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
