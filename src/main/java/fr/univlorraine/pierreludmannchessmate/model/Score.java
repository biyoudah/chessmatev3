package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * Entité de persistance représentant un score enregistré pour un utilisateur.
 *
 * Elle stocke le mode de jeu, les points détaillés, les erreurs, le temps de
 * résolution ainsi que des indicateurs (perfect, first time, bonus, etc.).
 */
@Data
@Entity
@Table(name = "score")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private String mode;

    @Column(name = "schema_key", length = 512)
    private String schemaKey;

    // Assurez-vous que le nom correspond à votre colonne DB (points)
    @Column(name = "points")
    private int points;

    // Colonne "score" obligatoire
    @Column(name = "score")
    private int score;

    // NOUVEAU : Correction de l'erreur SQL 1364
    @Column(name = "bonus_premier_schema_attribue")
    private int bonusPremierSchemaAttribue;

    private int erreurs;

    @Column(name = "erreurs_placement")
    private int erreursPlacement;

    private boolean perfect;

    @Column(name = "first_time")
    private boolean firstTime;

    @Column(name = "temps_resolution")
    private int tempsResolution;

    private boolean reussi;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    /**
     * Constructeur sans argument requis par JPA.
     */
    public Score() {}

    /**
     * Constructeur pratique pour instancier rapidement un score en mémoire.
     *
     * @param tempsResolution Durée de résolution du puzzle ou défi (en secondes).
     * @param reussi Indique si la tentative est réussie.
     */
    public Score(int tempsResolution, boolean reussi) {
        this.tempsResolution = tempsResolution;
        this.reussi = reussi;
    }
}