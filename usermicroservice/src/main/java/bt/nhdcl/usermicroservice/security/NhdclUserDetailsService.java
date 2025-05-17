package bt.nhdcl.usermicroservice.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import bt.nhdcl.usermicroservice.repository.UserRepository; // Import your UserRepository
import bt.nhdcl.usermicroservice.entity.User; // Import your User entity
import java.util.List;

@Service
public class NhdclUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Use UserRepository for MongoDB

    public NhdclUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch user by email from MongoDB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Return Spring Security's User object with email, password, and authorities
        return new NhdclUserDetails(user);
    }
}
