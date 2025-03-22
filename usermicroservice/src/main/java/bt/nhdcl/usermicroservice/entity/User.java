package bt.nhdcl.usermicroservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users") // Defines the MongoDB collection
public class User {
    @Id
    private String userId; // MongoDB ID (stored as a String)

    private String email;
    private String password;
    private boolean enabled;
    private String image;
    private String name;
    private String academyId;
    private String departmentId; // References the Department document
    private String roleId; // Stores the role ID (one-to-one relationship)

    // Constructors
    public User() {
    }

    public User(String email, String password, String name, String academyId, String departmentId, String roleId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.academyId = academyId;
        this.departmentId = departmentId;
        this.roleId = roleId;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademyId() {
        return academyId;
    }

    public void setAcademyId(String academyId) {
        this.academyId = academyId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
