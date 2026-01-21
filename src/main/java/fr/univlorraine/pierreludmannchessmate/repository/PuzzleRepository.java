package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
import fr.univlorraine.pierreludmannchessmate.model.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'accès aux puzzles stockés en base.
 */
@Repository
public interface PuzzleRepository extends JpaRepository<Puzzle, String> {

    /**
     * Sélectionne un puzzle aléatoire dont le nombre de coups est compris dans l'intervalle donné.
     *
     * @param minMoves borne basse du nombre de coups attendus
     * @param maxMoves borne haute du nombre de coups attendus
     * @return un puzzle aléatoire correspondant, ou vide si aucun ne matche
     */
    @Query(value = "SELECT * FROM puzzle WHERE " +
            "(LENGTH(moves) - LENGTH(REPLACE(moves, ' ', '')) + 1) BETWEEN :minMoves AND :maxMoves " +
            "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Puzzle> findRandomByMoveCount(@Param("minMoves") int minMoves, @Param("maxMoves") int maxMoves);

    /**
     * Recherche un puzzle par son identifiant métier.
     */
    Optional<Puzzle> findByPuzzleId(String id);
}