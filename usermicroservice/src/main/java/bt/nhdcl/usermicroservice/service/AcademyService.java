package bt.nhdcl.usermicroservice.service;

import bt.nhdcl.usermicroservice.entity.Academy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AcademyService {
    Academy addAcademy(Academy academy);

    List<Academy> getAllAcademies();

    Optional<Academy> getAcademyById(String id);

    void deleteAcademyById(String id);

    String uploadAcademyImage(String id, MultipartFile image) throws IOException;
}
