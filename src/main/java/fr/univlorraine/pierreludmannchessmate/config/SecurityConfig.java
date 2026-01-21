package fr.univlorraine.pierreludmannchessmate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security pour l'application.
 *
 * Définit les règles d'autorisation, la chaîne de filtres de sécurité,
 * l'encodage des mots de passe et le gestionnaire personnalisé de redirection
 * post-connexion.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customSuccessHandler;

    /**
     * Configure la chaîne de filtres de sécurité (autorisation, login, CSRF...).
     *
     * @param http constructeur de configuration HTTP Security
     * @return chaîne de filtres configurée
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/puzzle/**", "/placement/**"))

                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico", "/static/**").permitAll()

                        .requestMatchers("/", "/home", "/classement", "/infos/**", "/show", "/placement/**").permitAll()

                        .requestMatchers("/login", "/register", "/error").permitAll()

                        .requestMatchers("/puzzle/admin/**").hasRole("ADMIN")
                        .requestMatchers("/puzzle/**").permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin((form) -> form
                        .loginPage("/login")
                        // 2. ON REMPLACE .defaultSuccessUrl PAR NOTRE HANDLER
                        // C'est cette ligne qui fait la magie de la redirection dynamique
                        .successHandler(customSuccessHandler)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Fournit l'encodeur de mots de passe (BCrypt).
     *
     * @return encodeur BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}