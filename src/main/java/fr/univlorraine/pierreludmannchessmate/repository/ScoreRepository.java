package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository pour la persistance des scores et le calcul des classements.
 */
public interface ScoreRepository extends CrudRepository<Score, Long> {

    /**
     * Vérifie l'existence d'un score réussi pour un utilisateur et une clé de schéma donnée.
     */
    boolean existsByUtilisateurAndSchemaKey(Utilisateur utilisateur, String schemaKey);

    /**
     * Retourne les clés de schéma déjà réussies par un utilisateur.
     */
    @Query("select s.schemaKey from Score s where s.utilisateur = :user and s.reussi = true")
    Set<String> findCompletedSchemaKeysByUser(@Param("user") Utilisateur user);

    /**
     * Nombre total de réussites pour un utilisateur.
     */
    long countByUtilisateurAndReussiTrue(Utilisateur user);

    /**
     * Nombre de scores marqués perfect pour un utilisateur.
     */
    long countByUtilisateurAndPerfectTrue(Utilisateur user);

    /**
     * Nombre de réussites par mode pour un utilisateur.
     */
    long countByUtilisateurAndModeAndReussiTrue(Utilisateur user, String mode);

    /**
     * Classement filtré par clé de schéma.
     */
    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints " +
            "from Score s where s.schemaKey = :schemaKey " +
            "group by s.utilisateur.pseudo " +
            "order by totalPoints desc, min(s.createdAt) asc")
    List<ClassementRow> getClassementParSchemaKey(@Param("schemaKey") String schemaKey);

    /**
     * Classement global tous modes.
     */
    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints from Score s group by s.utilisateur.pseudo order by totalPoints desc, min(s.createdAt) asc")
    List<ClassementRow> getClassementGlobal();

    /**
     * Classement filtré par mode de jeu.
     */
    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints from Score s where s.mode = :mode group by s.utilisateur.pseudo order by totalPoints desc, min(s.createdAt) asc")
    List<ClassementRow> getClassementParMode(@Param("mode") String mode);

    /**
     * Vérifie l'existence d'un score réussi pour une clé de schéma (utile pour éviter les doublons).
     */
    boolean existsBySchemaKeyAndReussiTrue(String schemaKey);

    /**
     * Projection pour les rangs de classement.
     */
    interface ClassementRow {
        String getPseudo();
        Long getTotalPoints();
    }


    /**
     * Calcule le classement des joueurs pour un type de défi spécifique en utilisant un préfixe.
     *
     * @param prefix Le début de la clé de schéma à filtrer (ex: "8-dames[", "16-rois[").
     * @return Une liste d'objets {@link ClassementRow} contenant les pseudos et le cumul des points.
     */
    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints " +
            "from Score s where s.schemaKey LIKE concat(:prefix, '%') " +
            "group by s.utilisateur.pseudo " +
            "order by totalPoints desc, min(s.createdAt) asc")
    List<ClassementRow> getClassementParSchemaKeyPrefix(@Param("prefix") String prefix);
}