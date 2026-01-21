package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Accès CRUD et requêtes spécifiques pour les utilisateurs.
 */
public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {

    /**
     * Recherche par email (identifiant de connexion).
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Recherche par pseudonyme affiché.
     */
    Optional<Utilisateur> findByPseudo(String pseudo);

    /**
     * Incrémente le temps total de jeu accumulé pour un utilisateur donné.
     *
     * @param id identifiant utilisateur
     * @param secondes durée à ajouter (en secondes)
     */
    @Transactional
    @Modifying
    @Query("UPDATE Utilisateur u SET u.tempsTotalDeJeu = u.tempsTotalDeJeu + :secondes WHERE u.id = :id")
    void ajouterTempsDeJeu(@Param("id") Long id, @Param("secondes") long secondes);
}

