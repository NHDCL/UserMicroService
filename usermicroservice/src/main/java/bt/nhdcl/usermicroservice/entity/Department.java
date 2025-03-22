package bt.nhdcl.usermicroservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "department") // Defines the MongoDB collection
public class Department {

    @Id // MongoDB ID annotation (instead of @GeneratedValue)
    private String departmentId; // Change type to String (MongoDB uses ObjectId)

    private String name;
    private String description;

    // Default constructor
    public Department() {
    }

    // Parameterized constructor
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters
    public String getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
