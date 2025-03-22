package bt.nhdcl.usermicroservice.repository;

import bt.nhdcl.usermicroservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email); // Custom method to find a user by email
}
