package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import java.time.Instant;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entité représentant un utilisateur applicatif.
 *
 * Les champs couvrent l'identité (pseudo, email), l'authentification (password),
 * les métadonnées de création/mise à jour, le rôle (USER/ADMIN) et le temps de jeu total.
 */
@Data
@Entity
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pseudo;
    private String email;
    private String password;
    @CreationTimestamp
    private Instant dateCreation;
    @UpdateTimestamp
    private Instant dateMAJ;
    private String role; // Ex: "USER", "ADMIN"
    private long tempsTotalDeJeu; // En secondes

    /**
     * Constructeur sans argument requis par JPA.
     */
    public Utilisateur() {
    }

    /**
     * Constructeur utilitaire pour créer un utilisateur en mémoire.
     *
     * @param pseudo Nom d'utilisateur affiché.
     * @param email Adresse de contact.
     * @param password Mot de passe (hash attendu en production).
     */
    public Utilisateur(String pseudo, String email, String password) {
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
    }
}


