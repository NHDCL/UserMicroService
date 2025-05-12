package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // Other existing methods...

    User save(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(String id);

    Optional<User> getUserByEmail(String email);

    void deleteUserById(String id);

    boolean isEmailDuplicate(String email);

    // User updateUser(String id, User updatedUser);

    void updateUserEnabledStatus(String id, boolean enabled);

    String uploadUserImage(String id, MultipartFile image) throws IOException;

    boolean generateOtp(String email);

    boolean validateOtp(String email, String otp);

    boolean resetPassword(String email, String newPassword);

    boolean changePassword(String email, String oldPassword, String newPassword);

    boolean resendOtp(String email);

    User updateUser(String id, User updatedUser);

    boolean isEmployeeIdDuplicate(String employeeId);

    void permanentlyDeleteUser(String userId);

}
