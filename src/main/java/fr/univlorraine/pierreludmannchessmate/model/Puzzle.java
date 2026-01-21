package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entité représentant un puzzle issu (par exemple) de Lichess ou d'une base interne.
 *
 * Les informations stockées incluent l'identifiant du puzzle, la position FEN,
 * la séquence de coups attendue et un indice de difficulté (rating).
 */
@Entity
@Table(name = "puzzle")
@Data // Si vous utilisez Lombok, sinon générez les Getters/Setters manuellement
public class Puzzle {

    @Id
    @Column(name = "PuzzleId")
    private String puzzleId;

    @Column(name = "FEN", length = 500)
    private String fen;

    @Column(name = "Moves", length = 1000)
    private String moves;

    @Column(name = "Rating")
    private Integer rating;
}