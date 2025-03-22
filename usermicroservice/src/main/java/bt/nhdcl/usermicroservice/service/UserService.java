package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(String id);

    void deleteUserById(String id);
}
