package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Entité décrivant un type ou mode de jeu (ex: puzzle, placement, blitz).
 */
@Data
@Entity
public class TypeDeJeu {
    @Id
    private Long id;
    private String nomTypeDeJeu;

    /**
     * Constructeur sans argument requis par JPA.
     */
    public TypeDeJeu() {
    }

    /**
     * Constructeur utilitaire pour initialiser un type de jeu avec son identifiant.
     *
     * @param id Identifiant unique.
     * @param nomTypeDeJeu Libellé du mode de jeu.
     */
    public TypeDeJeu(Long id,String nomTypeDeJeu) {
        this.id = id;
        this.nomTypeDeJeu = nomTypeDeJeu;
    }
}
