package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.Academy;
import bt.nhdcl.usermicroservice.entity.Role;
import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.service.AcademyService;
import bt.nhdcl.usermicroservice.service.CloudinaryService;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/academies")
public class AcademyController {

    private final AcademyService academyService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public AcademyController(AcademyService academyService, CloudinaryService cloudinaryService) {
        this.academyService = academyService;
        this.cloudinaryService = cloudinaryService;
    }

    // Create an academy with optional image upload
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Academy> createAcademy(
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            String image = null;

            // Upload image to Cloudinary in "academy_images" folder if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                image = cloudinaryService.uploadAcademyImage(imageFile); // Changed method call
            }

            // Create Academy object
            Academy academy = new Academy();
            academy.setName(name);
            academy.setLocation(location);
            academy.setDescription(description);
            academy.setImage(image); // Setting the correct field

            Academy savedAcademy = academyService.addAcademy(academy);
            return ResponseEntity.ok(savedAcademy);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // Get all academies
    @GetMapping
    public ResponseEntity<List<Academy>> getAllAcademies() {
        List<Academy> academies = academyService.getAllAcademies();
        return ResponseEntity.ok(academies);
    }

    // Get academy by ID
    @GetMapping("/{id}")
    public ResponseEntity<Academy> getAcademyById(@PathVariable String id) {
        Optional<Academy> academy = academyService.getAcademyById(id);
        return academy.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete an academy
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAcademy(@PathVariable String id) {
        academyService.deleteAcademyById(id);
        return ResponseEntity.noContent().build();
    }

    // // Update academy details
    // @PutMapping("/{id}")
    // public ResponseEntity<Academy> updateAcademy(@PathVariable String id,
    // @RequestBody Academy updatedAcademy) {
    // // Extract roleId and roleName from updatedUser
    // String name = updatedAcademy.getName(); // Assuming getName() is the

    // // Now update the user
    // Academy academy = academyService.updateAcademy(id, updatedAcademy);
    // return ResponseEntity.ok(academy); // Return the updated user object in the
    // }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Academy> updateAcademy(
            @PathVariable String id,
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) throws IOException {

        // Build Academy object from form-data
        Academy updatedAcademy = new Academy();
        updatedAcademy.setName(name);
        updatedAcademy.setLocation(location);
        updatedAcademy.setDescription(description);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadAcademyImage(imageFile);
            updatedAcademy.setImage(imageUrl);
        }

        Academy academy = academyService.updateAcademy(id, updatedAcademy);
        return ResponseEntity.ok(academy);
    }
}
