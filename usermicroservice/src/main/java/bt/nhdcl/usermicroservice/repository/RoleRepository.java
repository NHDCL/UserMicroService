package bt.nhdcl.usermicroservice.repository;

import bt.nhdcl.usermicroservice.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    Optional<Role> findByName(String name); // Custom query to find role by name
}
