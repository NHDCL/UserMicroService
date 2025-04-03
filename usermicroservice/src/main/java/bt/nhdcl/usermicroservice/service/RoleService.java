package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role addRole(Role role);

    List<Role> getAllRoles();

    Optional<Role> getRoleById(String id);

    Optional<Role> getRoleByName(String name); // Add this line

    void deleteRoleById(String id);
}
