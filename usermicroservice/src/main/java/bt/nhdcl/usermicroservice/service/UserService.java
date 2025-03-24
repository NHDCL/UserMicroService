package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User save(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(String id); // MongoDB uses String for IDs

    void deleteUserById(String id);

    boolean isEmailDuplicate(String email);

    User updateUser(String id, User updatedUser);

    void updateUserEnabledStatus(String id, boolean enabled);

    String uploadUserImage(String id, MultipartFile image) throws IOException; // Returns image URL
}
