package bt.nhdcl.usermicroservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles") // Defines the MongoDB collection
public class Role {

    @Id // MongoDB ID annotation
    private String roleId; // MongoDB stores ObjectId as String

    private String name; // Role name (can be 'Admin', 'User', etc.)
    private String description; // Description of the role

    // Default constructor
    public Role() {
    }

    // Parameterized constructor
    public Role(String roleId, String name, String description) {
        this.roleId = roleId;
        this.name = name;
        this.description = description;
    }

    // Getters
    public String getRoleId() {
        return roleId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
