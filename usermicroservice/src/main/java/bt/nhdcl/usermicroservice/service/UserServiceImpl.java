package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.repository.UserRepository;
import bt.nhdcl.usermicroservice.exception.UserNotFoundException;
import bt.nhdcl.usermicroservice.exception.FileSizeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            MongoTemplate mongoTemplate, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteUserById(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User updateUser(String id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            existingUser.setAcademyId(updatedUser.getAcademyId());
            existingUser.setDepartmentId(updatedUser.getDepartmentId());
            existingUser.setRoleId(updatedUser.getRoleId());
            existingUser.setEnabled(updatedUser.isEnabled());
            existingUser.setImage(updatedUser.getImage());

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public void updateUserEnabledStatus(String id, boolean enabled) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("enabled", enabled);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public String uploadUserImage(String id, MultipartFile image) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Uploaded image file is empty");
        }

        if (image.getSize() > 1024 * 1024) { // 1MB limit
            throw new FileSizeException("File size must be < 1MB");
        }

        // Upload image to Cloudinary in user_images folder
        String imageUrl = cloudinaryService.uploadImage(image, "user_images");

        // Store the image URL in the user object
        user.setImage(imageUrl);
        userRepository.save(user);

        return imageUrl; // Return uploaded image URL
    }
}