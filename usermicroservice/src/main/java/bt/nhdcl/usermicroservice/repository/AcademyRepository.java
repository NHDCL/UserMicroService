package bt.nhdcl.usermicroservice.repository;

import bt.nhdcl.usermicroservice.entity.Academy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AcademyRepository extends MongoRepository<Academy, String> {

    Optional<Academy> findByName(String name); // Custom query to find academy by name
}
