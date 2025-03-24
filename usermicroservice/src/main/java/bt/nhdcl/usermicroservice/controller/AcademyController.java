package bt.nhdcl.usermicroservice.controller;

import bt.nhdcl.usermicroservice.entity.Academy;
import bt.nhdcl.usermicroservice.service.AcademyService;
import bt.nhdcl.usermicroservice.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
