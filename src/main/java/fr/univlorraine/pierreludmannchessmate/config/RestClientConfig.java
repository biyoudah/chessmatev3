package fr.univlorraine.pierreludmannchessmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration Spring pour les clients HTTP.
 *
 * Expose un bean {@link RestTemplate} utilisé par les services pour
 * communiquer avec des APIs externes (Lichess, Chess.com, etc.).
 */
@Configuration
public class RestClientConfig {

    /**
     * Instancie et fournit un RestTemplate pour les appels HTTP.
     *
     * @return bean RestTemplate prêt à être injecté
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
