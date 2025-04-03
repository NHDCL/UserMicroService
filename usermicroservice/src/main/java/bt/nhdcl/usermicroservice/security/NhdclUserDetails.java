package bt.nhdcl.usermicroservice.security;

import bt.nhdcl.usermicroservice.entity.User;
import bt.nhdcl.usermicroservice.entity.Role; // Import the Role class
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class NhdclUserDetails implements UserDetails {

    private User user;

    public NhdclUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Access the Role object and its roleId
        Role role = user.getRole(); // Since user has a Role object, not a roleId

        // Check if the role is not null, then fetch the roleId or roleName
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleId())); // Assuming roleId is a String
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_UNKNOWN")); // Default role if no role is set
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public String getUserId() {
        return user.getUserId();
    }

    public String getName() {
        return user.getName();
    }

    public String getAcademyId() {
        return user.getAcademyId();
    }

    public String getDepartmentId() {
        return user.getDepartmentId();
    }

    public String getImage() {
        return user.getImage();
    }
}
