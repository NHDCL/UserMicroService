package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role addRole(Role role);

    List<Role> getAllRoles();

    Optional<Role> getRoleById(String id);

    void deleteRoleById(String id);
}
