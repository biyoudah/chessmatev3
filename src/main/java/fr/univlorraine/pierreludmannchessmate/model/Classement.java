package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entité représentant un classement nommé.
 *
 * Chaque classement porte un nom unique et conserve la date de dernière mise à jour.
 */
@Data
@Entity
public class Classement {
    @Id
    private String nom;
    @UpdateTimestamp
    private Instant dateMAJ;

    /**
     * Constructeur sans argument requis par JPA.
     */
    public Classement() {
    }

    /**
     * Constructeur utilitaire pour instancier un classement nommé.
     *
     * @param nom Libellé unique du classement.
     */
    public Classement(String nom) {
        this.nom = nom;
        this.dateMAJ = Instant.now();
    }
}
