package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.Academy;
import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.repository.AcademyRepository;
import bt.nhdcl.usermicroservice.exception.AcademyNotFoundException;
import bt.nhdcl.usermicroservice.exception.FileSizeException;
import bt.nhdcl.usermicroservice.exception.UserNotFoundException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AcademyServiceImpl implements AcademyService {
    private final AcademyRepository academyRepository;
    private final Cloudinary cloudinary;

    @Autowired
    public AcademyServiceImpl(AcademyRepository academyRepository, Cloudinary cloudinary) {
        this.academyRepository = academyRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public Academy addAcademy(Academy academy) {
        return academyRepository.save(academy);
    }

    @Override
    public List<Academy> getAllAcademies() {
        return academyRepository.findAll();
    }

    @Override
    public Optional<Academy> getAcademyById(String id) {
        return academyRepository.findById(id);
    }

    @Override
    public void deleteAcademyById(String id) {
        academyRepository.deleteById(id);
    }

    @Override
    public String uploadAcademyImage(String id, MultipartFile image) throws IOException {
        Academy academy = academyRepository.findById(id)
                .orElseThrow(() -> new AcademyNotFoundException("Academy not found with id: " + id));

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Uploaded image file is empty");
        }

        if (image.getSize() > 1024 * 1024) { // 1MB limit
            throw new FileSizeException("File size must be < 1MB");
        }

        try {
            // Upload image to Cloudinary in academies_images folder
            Map<String, Object> uploadResult = cloudinary.uploader().upload(image.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "academies_images", // Specify the folder for academy images
                            "resource_type", "image"));
            String imageUrl = (String) uploadResult.get("secure_url");

            // Store the image URL in the academy object
            academy.setImage(imageUrl);
            academyRepository.save(academy);

            return imageUrl; // Return uploaded image URL
        } catch (IOException e) {
            throw new IOException("Error uploading image to Cloudinary", e);
        }
    }

    @Override
    public Academy updateAcademy(String id, Academy updatedAcademy) {
        return academyRepository.findById(id).map(existingAcademy -> {
            existingAcademy.setName(updatedAcademy.getName());
            existingAcademy.setLocation(updatedAcademy.getLocation());
            existingAcademy.setImage(updatedAcademy.getImage());
            existingAcademy.setDescription(updatedAcademy.getDescription());

            return academyRepository.save(existingAcademy);
        }).orElseThrow(() -> new AcademyNotFoundException("Academy not found with id: " + id));
    }
}