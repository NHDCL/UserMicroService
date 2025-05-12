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

import com.mongodb.client.result.UpdateResult;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import bt.nhdcl.usermicroservice.security.OtpDetails;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final CloudinaryService cloudinaryService;
    private final JavaMailSender mailSender;
    private Map<String, OtpDetails> otpStorage = new HashMap<>();

    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            MongoTemplate mongoTemplate, CloudinaryService cloudinaryService, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.cloudinaryService = cloudinaryService;
        this.mailSender = mailSender;
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findByEnabledTrue();
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteUserById(String id) {
        // Check if the user exists
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        // Find the user and set the 'enabled' flag to false (soft delete)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Perform soft delete by disabling the user
        user.setEnabled(false);

        // Save the user with updated enabled status
        userRepository.save(user);
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User updateUser(String id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setImage(updatedUser.getImage());

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public void updateUserEnabledStatus(String id, boolean enabled) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("enabled", enabled);

        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);

        if (result.getMatchedCount() == 0) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }

    @Override
    public String uploadUserImage(String id, MultipartFile image) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Uploaded image file is empty");
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeException("File size must be < 1MB");
        }

        String imageUrl = cloudinaryService.uploadImage(image, "user_images");
        user.setImage(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    public boolean isEmployeeIdDuplicate(String employeeId) {
        return userRepository.existsByEmployeeId(employeeId);
    }

    @Override
    public void permanentlyDeleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public boolean generateOtp(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false; // Return false if user not found
        }

        try {
            // Generate OTP
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Set expiration time (e.g., 5 minutes from now)
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

            // Store OTP with expiration
            otpStorage.put(email, new OtpDetails(otp, expiryTime));

            // Send OTP via email
            sendOtpEmail(email, otp);
            return true; // Return true if OTP was successfully generated and sent
        } catch (Exception e) {
            return false; // Return false if there was an error
        }
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        OtpDetails otpDetails = otpStorage.get(email); // Correct type

        if (otpDetails == null) {
            return false; // No OTP found for the email
        }

        // Check if OTP is expired
        if (System.currentTimeMillis() > otpDetails.getExpiryTime()) {
            otpStorage.remove(email); // Remove expired OTP
            return false;
        }

        // Check if OTP matches
        return otpDetails.getOtp().equals(otp);
    }

    @Override
    public boolean resendOtp(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false; // Return false if user not found
        }

        try {
            // Generate OTP
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Set expiration time (e.g., 5 minutes from now)
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

            // Store OTP with expiration
            otpStorage.put(email, new OtpDetails(otp, expiryTime));

            // Send OTP via email
            sendOtpEmail(email, otp);
            return true; // Return true if OTP was successfully generated and sent
        } catch (Exception e) {
            return false; // Return false if there was an error
        }
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        try {
            // Find the user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Encode the new password and set it to the user
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);

            // Save the user with the updated password
            userRepository.save(user);

            // Return true if the password reset was successful
            return true;
        } catch (Exception e) {
            // Handle any errors (e.g., user not found or database issues)
            return false; // Return false if something goes wrong
        }
    }

    private void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Your One-Time Password (OTP) for Secure Access");

            String emailContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; padding: 20px; " +
                    "border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;'>" +
                    "<h2 style='color: #897462;'>Hello,</h2>" +
                    "<p style='font-size: 16px;'>You requested a one-time password (OTP) to verify your identity.</p>" +
                    "<p style='font-size: 18px; font-weight: bold; color: #f9f9f9; text-align: center; " +
                    "border: 1px solid #305845; padding: 10px; border-radius: 5px; background: #4a7f68;'>" + otp
                    + "</p>" +
                    "<p style='font-size: 14px; color: #555;'>Please use this OTP within the next 5 minutes. " +
                    "Do not share this code with anyone.</p>" +
                    "<p style='font-size: 14px;'>If you did not request this OTP, please ignore this email or contact our support team.</p>"
                    +
                    "<hr style='border: none; border-top: 1px solid #ddd;'/>" +
                    "<p style='font-size: 12px; color: #777;'>Thank you,<br/><strong>NHDCL</strong></p>" +
                    "</div>";

            helper.setText(emailContent, true);
            mailSender.send(message);
            System.out.println("OTP email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Error sending OTP email: " + e.getMessage());
        }
    }

    @Override
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if old password matches
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("Old password is incorrect.");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        throw new IllegalArgumentException("User not found.");
    }
}
