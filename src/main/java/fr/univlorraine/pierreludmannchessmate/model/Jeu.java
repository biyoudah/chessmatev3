package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Entité représentant un modèle générique de jeu ou puzzle.
 *
 * Elle stocke le nom, les données du problème et son niveau de difficulté.
 */
@Data
@Entity
public class Jeu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String donneesProbleme;
    private Difficulte difficulte;

    /**
     * Constructeur sans argument requis par JPA.
     */
    public Jeu() {
    }

    /**
     * Constructeur pratique pour créer un jeu avec ses métadonnées.
     *
     * @param nom Nom du jeu ou puzzle.
     * @param donnees Contenu décrivant le problème (FEN, séquence de coups, etc.).
     * @param diff Niveau de difficulté associé.
     */
    public Jeu(String nom,String donnees,Difficulte diff) {
        this.nom = nom;
        this.donneesProbleme = donnees;
        this.difficulte = diff;
    }
}
