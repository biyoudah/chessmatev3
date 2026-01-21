package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository.ClassementRow;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrôleur de consultation des classements.
 * <p>
 * Fournit une page récapitulant les meilleurs scores globalement ou par mode
 * de jeu, en s'appuyant sur des projections renvoyées par {@link ScoreRepository}.
 */
@Controller
public class ClassementController {

    private final ScoreRepository scoreRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Injection du dépôt des scores.
     * @param scoreRepository repository pour l'accès aux classements
     */
    public ClassementController(ScoreRepository scoreRepository, UtilisateurRepository utilisateurRepository) {
        this.scoreRepository = scoreRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Affiche le classement
     *
     * @param mode filtre optionnel (ex: PUZZLE, PLACEMENT)
     * @param schemaKey préfixe optionnel pour filtrer par défi spécifique (ex: 8-dames[)
     * @param authentication informations sur l'utilisateur
     * @param model model de la vue
     * @return le nom du template HTML "classement"
     */
    @GetMapping("/classement")
    public String afficherClassement(
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String schemaKey,
            Authentication authentication,
            Model model) {

        injecterInfosUtilisateur(model, authentication);

        List<ClassementRow> classement;

        if (schemaKey != null && !schemaKey.isEmpty() && !schemaKey.equals("TOUT")) {
            classement = scoreRepository.getClassementParSchemaKeyPrefix(schemaKey);
        }
        else if (mode != null && !mode.isEmpty() && !mode.equals("TOUS")) {
            classement = scoreRepository.getClassementParMode(mode);
        }
        else {
            classement = scoreRepository.getClassementGlobal();
        }

        model.addAttribute("classement", classement);
        model.addAttribute("modeSelectionne", mode);
        model.addAttribute("schemaKeySelectionne", schemaKey);

        return "classement";
    }

    /**
     * Injecte dans le modèle le statut de connexion et le pseudo de l'utilisateur courant.
     *
     * @param model modèle de la vue Thymeleaf
     * @param authentication informations d'authentification Spring Security
     */
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isConnected);

        if (isConnected) {
            String pseudo = utilisateurRepository.findByEmail(authentication.getName())
                    .map(Utilisateur::getPseudo)
                    .orElse("Joueur");

            model.addAttribute("pseudo", pseudo);
        } else {
            model.addAttribute("pseudo", "Invité");
        }
    }
}
