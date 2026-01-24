package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la page ChessMate Desktop Lite.
 */
@Controller
public class PlusController {

    private final UtilisateurRepository utilisateurRepository;

    public PlusController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Affiche la page "Plus" (Desktop Lite).
     */
    @GetMapping("/plus")
    public String plus(Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        return "plus"; // Doit correspondre au nom de ton fichier plus.html
    }

    /**
     * Méthode identique à ton InfoController pour garder la cohérence du pseudo
     */
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isConnected);

        if (isConnected) {
            String emailUtilisateur = authentication.getName();
            String pseudo = utilisateurRepository.findByEmail(emailUtilisateur)
                    .map(Utilisateur::getPseudo)
                    .orElse("Joueur");
            model.addAttribute("pseudo", pseudo);
        } else {
            model.addAttribute("pseudo", "Invité");
        }
    }
}