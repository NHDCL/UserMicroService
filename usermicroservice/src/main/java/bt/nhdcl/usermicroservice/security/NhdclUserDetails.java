package bt.nhdcl.usermicroservice.security;

import bt.nhdcl.usermicroservice.entity.User;
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
        // Assuming roles are stored in the user entity as roleId
        // You would ideally fetch the role based on roleId from a database or another
        // service
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Here, you can map the roleId to actual roles (for simplicity, we assume it's
        // a role name)
        authorities.add(new SimpleGrantedAuthority(user.getRoleId())); // Using roleId as authority

        return authorities;
    }

    @Override
    public String getPassword() {
        // Return the password stored in the User entity
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Return the email as the username
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Here, we assume the account does not expire
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Here, we assume the account is not locked
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Here, we assume the credentials do not expire
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Return the enabled status from the User entity
        return user.isEnabled();
    }

    public String getUserId() {
        // You can expose the userId if needed (e.g., for user-specific operations)
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
