package bt.nhdcl.usermicroservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API clients
                .authorizeHttpRequests(auth -> auth
                        // Allow all operations on Users
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}").permitAll()
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

                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable()) // Disable HTTP Basic Auth
                .formLogin(form -> form.disable()); // Disable login form

        return http.build();
    }
}
