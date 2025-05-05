package bt.nhdcl.usermicroservice.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class NhdclSecurityConfig {

    @Autowired
    private NhdclUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService) // Set the custom UserDetailsService
                .passwordEncoder(passwordEncoder()); // Set the PasswordEncoder
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API clients
                .authorizeHttpRequests(auth -> auth
                        // Allow login endpoint for all
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/keep-alive").permitAll()

                        // Allow all operations on Users for Admin only
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/image").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/checkDuplicateEmail").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}/enabled").permitAll()

                        // Allow all operations on Academies
                        .requestMatchers(HttpMethod.POST, "/api/academies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/academies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/academies/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/academies/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/academies/{id}").permitAll()

                        // Allow all operations on Roles
                        .requestMatchers(HttpMethod.POST, "/api/roles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/roles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/roles/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/roles/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/roles/{id}").permitAll()

                        // Allow all operations on Departments
                        .requestMatchers(HttpMethod.POST, "/api/departments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departments/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/departments/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/{id}").permitAll()

                        // Allow all operation on Emails
                        .requestMatchers(HttpMethod.POST, "/api/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/verify-otp").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/resend-otp").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/change-password").permitAll()

                        // Protect any other request that needs to be authenticated
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable()) // Disable HTTP Basic Auth
                .formLogin(form -> form.disable()); // Disable login form

        return http.build();
    }
}
