package bt.nhdcl.usermicroservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "academies") // Defines the MongoDB collection
public class Academy {

    @Id // MongoDB ID annotation
    private String academyId; // Changed type to String (MongoDB stores ObjectId as String)

    private String name;
    private String location;
    private String image; // URL or Base64 string
    private String description;

    // Default constructor
    public Academy() {
    }

    // Parameterized constructor
    public Academy(String name, String location, String image, String description) {
        this.name = name;
        this.location = location;
        this.image = image;
        this.description = description;
    }

    // Getters
    public String getAcademyId() {
        return academyId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setAcademyId(String academyId) {
        this.academyId = academyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
