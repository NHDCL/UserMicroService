package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.service.UserService;
import bt.nhdcl.usermicroservice.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserController(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    // Create a new user with an image upload
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createUser(
            @RequestParam("email") @Valid @NotNull String email,
            @RequestParam("password") @Valid @NotNull String password,
            @RequestParam("name") @Valid @NotNull String name,
            @RequestParam("academyId") @Valid @NotNull String academyId,
            @RequestParam("departmentId") @Valid @NotNull String departmentId,
            @RequestParam("roleId") @Valid @NotNull String roleId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            String imageUrl = null;

            // Upload image to Cloudinary under "user_images" folder
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadUserImage(imageFile);
            }

            // Create and save the user
            User user = new User(email, password, name, academyId, departmentId, roleId, imageUrl);
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
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
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
}
