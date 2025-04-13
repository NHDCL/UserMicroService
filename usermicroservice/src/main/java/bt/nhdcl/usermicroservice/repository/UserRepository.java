package bt.nhdcl.usermicroservice.repository;

import bt.nhdcl.usermicroservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email); // Find user by email

    Optional<User> findById(String id); // Find user by ID (MongoDB uses String ID)

    void deleteById(String id); // Delete user by ID\\

    List<User> findByEnabledTrue();

}
