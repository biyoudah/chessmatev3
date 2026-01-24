package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class PlusController {

    private final UtilisateurRepository utilisateurRepository;

    public PlusController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/plus")
    public String plus(Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        return "plus";
    }

    /**
     * Gère le téléchargement du fichier RAR
     */
    @GetMapping("/download/lite")
    @ResponseBody
    public ResponseEntity<Resource> downloadLite() throws IOException {
        Resource resource = new ClassPathResource("static/downloads/ChessMateDesktopLite.rar");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-rar-compressed"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ChessMateDesktopLite.rar\"")
                .body(resource);
    }

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