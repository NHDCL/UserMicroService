package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    Department addDepartment(Department department);

    List<Department> getAllDepartments();

    Optional<Department> getDepartmentById(String id);

    void deleteDepartmentById(String id);
}
