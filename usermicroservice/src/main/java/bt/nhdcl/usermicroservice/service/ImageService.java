package bt.nhdcl.usermicroservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

    private final Cloudinary cloudinary;

    // Inject Cloudinary credentials from application.properties
    public ImageService(@Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    // Method to upload an image to Cloudinary
    public String uploadImage(MultipartFile image) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap("folder", "user_images"));
        return (String) uploadResult.get("url"); // Return the URL of the uploaded image
    }
}
