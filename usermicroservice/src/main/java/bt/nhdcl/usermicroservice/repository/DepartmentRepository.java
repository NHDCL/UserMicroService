package bt.nhdcl.usermicroservice.repository;

import bt.nhdcl.usermicroservice.entity.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
}
