package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
import fr.univlorraine.pierreludmannchessmate.model.Puzzle;
import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.PuzzleRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import fr.univlorraine.pierreludmannchessmate.service.LichessPuzzleService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Contrôleur du mode « Puzzle ».
 * Gère l'affichage, les mouvements AJAX et la logique de session (Lichess vs Local).
 */
@Controller
@RequestMapping("/puzzle")
@SessionAttributes("jeuPuzzle")
public class PuzzleController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;
    private final PuzzleRepository puzzleRepository;
    private final LichessPuzzleService lichessPuzzleService;

    public PuzzleController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository, PuzzleRepository puzzleRepository, LichessPuzzleService lichessPuzzleService) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
        this.puzzleRepository = puzzleRepository;
        this.lichessPuzzleService = lichessPuzzleService;
    }

    @ModelAttribute("jeuPuzzle")
    public JeuPuzzle initPuzzle() {
        return new JeuPuzzle();
    }

    /**
     * Affiche la page du puzzle (GET).
     */
    @GetMapping
    public String afficherPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                 Model model,
                                 Authentication auth,
                                 HttpSession session) {

        // 1. Gestion des messages Flash (pour les popups JS)
        String[] sessionAttrs = {"flashMessage", "flashDetail", "flashType"};
        for (String attr : sessionAttrs) {
            Object val = session.getAttribute(attr);
            if (val != null) {
                model.addAttribute(attr.replace("flash", "").toLowerCase(), val);
                session.removeAttribute(attr);
            }
        }

        // 2. Utilisateur connecté ?
        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            String pseudo = recupererUtilisateurCourant(auth)
                    .map(Utilisateur::getPseudo)
                    .orElse("Joueur");
            model.addAttribute("pseudo", pseudo);
        } else {
            model.addAttribute("pseudo", "Invité");
        }

        // 3. Indice visuel
        Object hint = session.getAttribute("hintCoords");
        if (hint != null) {
            model.addAttribute("hintCoords", hint);
        }

        // 4. Si le plateau est vide, on charge un puzzle par défaut (Local)
        boolean plateauVide = true;
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                if(game.getBoard()[i][j] != null) { plateauVide = false; break; }
            }
        }

        if (plateauVide) {
            chargerPuzzleSelonDifficulte(game, session);
            game.setScoreEnregistre(false);
            session.removeAttribute("lichessPuzzle");
        }

        // 5. Orientation du plateau (Vue Blanc ou Noir)
        boolean orienterPourLesBlancs = game.isVueJoueurEstBlanc();

        List<Integer> rows = orienterPourLesBlancs
                ? IntStream.rangeClosed(0, 7).map(i -> 7 - i).boxed().toList()
                : IntStream.rangeClosed(0, 7).boxed().toList();

        List<Integer> cols = orienterPourLesBlancs
                ? IntStream.rangeClosed(0, 7).boxed().toList()
                : IntStream.rangeClosed(0, 7).map(i -> 7 - i).boxed().toList();

        model.addAttribute("rows", rows);
        model.addAttribute("cols", cols);
        model.addAttribute("board", game.getBoard());
        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        model.addAttribute("lastMoveStart", game.getLastMoveStart());
        model.addAttribute("lastMoveEnd", game.getLastMoveEnd());

        // 6. Classements
        model.addAttribute("classementGlobal", scoreRepository.getClassementGlobal());
        model.addAttribute("classementTactique", scoreRepository.getClassementParMode("PUZZLE"));

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "jeuPuzzle"; // Vérifie que ton fichier HTML s'appelle bien jeuPuzzle.html ou puzzle.html
    }

    // --- ACTIONS AJAX (POST) ---

    @PostMapping("/hint")
    @ResponseBody
    public ResponseEntity<Void> getHint(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        String coords = game.getCoupAide();
        if (coords != null) {
            session.setAttribute("hintCoords", coords);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/move")
    @ResponseBody
    public ResponseEntity<Void> handleMove(@RequestParam int departX, @RequestParam int departY,
                                           @RequestParam int arriveeX, @RequestParam int arriveeY,
                                           @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                           HttpSession session,
                                           Authentication auth) {

        session.removeAttribute("hintCoords");
        // Attention à l'ordre X/Y selon ta logique JeuPuzzle (souvent Y=Ligne, X=Colonne)
        String resultat = game.jouerCoupJoueur(departY, departX, arriveeY, arriveeX);
        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        if ("GAGNE".equals(resultat)) {
            traiterVictoirePuzzle(game, session, auth);
            session.setAttribute("flashMessage", "Puzzle Résolu !");
            session.setAttribute("flashType", "victory");
            if (userOpt.isEmpty()) {
                session.setAttribute("flashDetail", "Inscrivez-vous pour enregistrer vos points !");
            } else {
                session.setAttribute("flashDetail", "Bien joué !");
            }
        } else if ("RATE".equals(resultat) || "ECHEC".equals(resultat)) {
            session.setAttribute("flashMessage", "Mauvais coup !");
            session.setAttribute("flashType", "error");
            session.setAttribute("flashDetail", "Ce n'est pas la solution.");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/computer-move")
    @ResponseBody
    public ResponseEntity<Void> computerMove(@ModelAttribute("jeuPuzzle") JeuPuzzle game) {
        game.reponseOrdinateur();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/changeMode")
    @ResponseBody
    public ResponseEntity<Void> changeMode(@RequestParam("difficulte") String difficulte,
                                           @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                           HttpSession session) {
        session.removeAttribute("hintCoords");

        // IMPORTANT : On force la sortie du mode Lichess
        session.removeAttribute("lichessPuzzle");
        game.setLichessPuzzle(false);

        game.setDifficulte(difficulte);
        boolean succes = chargerPuzzleSelonDifficulte(game, session);

        if (!succes) {
            game.viderPlateau();
        } else {
            game.setScoreEnregistre(false);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    @ResponseBody
    public ResponseEntity<Void> resetPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        session.removeAttribute("hintCoords");

        // Si mode Lichess : on recommence le puzzle actuel
        if (session.getAttribute("lichessPuzzle") != null || game.isLichessPuzzle()) {
            game.recommencerPuzzleActuel();
        } else {
            // Si mode Local : on en charge un nouveau
            boolean succes = chargerPuzzleSelonDifficulte(game, session);
            if (succes) {
                game.setScoreEnregistre(false);
            } else {
                game.viderPlateau();
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Void> clearBoard(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        session.removeAttribute("hintCoords");
        game.viderPlateau();
        return ResponseEntity.ok().build();
    }

    // --- CHARGEMENT DONNÉES EXTERNES ---

    @GetMapping("/random-lichess")
    @ResponseBody
    public ResponseEntity<?> getRandomPuzzleFromLichess(Authentication auth) {
        JSONObject puzzle = lichessPuzzleService.getPuzzleRandom();

        if (puzzle == null) {
            return ResponseEntity.status(500).body("{\"error\": \"Impossible de récupérer un puzzle\"}");
        }

        boolean dejaFait = false;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);
            if (userOpt.isPresent()) {
                String pid = puzzle.optString("PuzzleId", puzzle.optString("puzzleId", ""));
                if (!pid.isBlank()) {
                    String schemaKey = "PUZZLE_" + pid;
                    dejaFait = scoreRepository.existsByUtilisateurAndSchemaKey(userOpt.get(), schemaKey);
                }
            }
        }

        if (dejaFait) {
            puzzle.put("alreadySolved", true);
            puzzle.put("message", "Puzzle du jour déjà résolu.");
        }

        // Sécurité FEN
        if (!puzzle.has("fen") || puzzle.get("fen") == null) {
            puzzle.put("fen", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        }

        return ResponseEntity.ok(puzzle.toString());
    }

    @PostMapping("/load-from-data")
    @ResponseBody
    public ResponseEntity<Void> loadPuzzleFromData(
            @RequestParam String puzzleId,
            @RequestParam String fen,
            @RequestParam String moves,
            @RequestParam(defaultValue = "false") boolean isLichessPuzzle,
            @ModelAttribute JeuPuzzle game,
            HttpSession session) {

        session.removeAttribute("hintCoords");
        JSONObject puzzleData = new JSONObject();
        puzzleData.put("puzzleId", puzzleId);
        puzzleData.put("fen", fen);
        puzzleData.put("moves", moves);
        puzzleData.put("isLichessPuzzle", isLichessPuzzle);

        game.setLichessPuzzle(isLichessPuzzle);
        game.dechiffre_pb(puzzleData);
        game.setScoreEnregistre(false);

        if (isLichessPuzzle) {
            session.setAttribute("lichessPuzzle", true);
        } else {
            session.removeAttribute("lichessPuzzle");
        }

        return ResponseEntity.ok().build();
    }

    // --- ADMINISTRATION ---

    @GetMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel(Model model) {
        return "adminPuzzle";
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addPuzzleCsv(@RequestParam String puzzleId, @RequestParam String fen,
                               @RequestParam String moves, @RequestParam Integer rating,
                               HttpSession session) {

        try {
            Puzzle newPuzzle = new Puzzle();
            newPuzzle.setPuzzleId(puzzleId);
            newPuzzle.setFen(fen);
            newPuzzle.setMoves(moves);
            newPuzzle.setRating(rating);

            puzzleRepository.save(newPuzzle);

            session.setAttribute("flashMessage", "Puzzle ajouté avec succès !");
            session.setAttribute("flashType", "victory");
            session.setAttribute("flashDetail", "ID: " + puzzleId);

        } catch (Exception e) {
            session.setAttribute("flashMessage", "Erreur lors de l'ajout");
            session.setAttribute("flashType", "error");
            session.setAttribute("flashDetail", e.getMessage());
        }

        return "redirect:/puzzle";
    }

    // --- PRIVATE HELPERS ---

    private void traiterVictoirePuzzle(JeuPuzzle game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);
        if (userOpt.isEmpty()) {
            game.setScoreEnregistre(true);
            return;
        }

        Utilisateur user = userOpt.get();
        int nouveauxPoints = switch (game.getDifficulte()) {
            case "1" -> 10;
            case "2" -> 25;
            case "3" -> 50;
            default  -> 10;
        };

        String puzzleId = game.getPuzzleId();
        if (puzzleId == null || puzzleId.isBlank() || "????".equals(puzzleId)) {
            puzzleId = "AUTO-" + System.currentTimeMillis();
            game.setPuzzleId(puzzleId);
        }

        String schemaKey = "PUZZLE_" + puzzleId;
        boolean dejaReussi = scoreRepository.existsByUtilisateurAndSchemaKey(user, schemaKey);

        if (!dejaReussi) {
            Score s = new Score();
            s.setUtilisateur(user);
            s.setMode("PUZZLE");
            s.setSchemaKey(schemaKey);
            s.setPoints(nouveauxPoints);
            s.setScore(nouveauxPoints);
            s.setReussi(true);
            s.setPerfect(true);
            s.setFirstTime(true);
            scoreRepository.save(s);
        }

        game.setScoreEnregistre(true);
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(auth.getName());
    }

    private boolean chargerPuzzleSelonDifficulte(JeuPuzzle game, HttpSession session) {
        int minMoves = 1;
        int maxMoves = 2;

        switch (game.getDifficulte()) {
            case "2" -> { minMoves = 3; maxMoves = 4; }
            case "3" -> { minMoves = 5; maxMoves = 20; }
        }

        Optional<Puzzle> puzzleData = puzzleRepository.findRandomByMoveCount(minMoves, maxMoves);

        if (puzzleData.isPresent()) {
            Puzzle p = puzzleData.get();

            JSONObject json = new JSONObject();
            json.put("PuzzleId", p.getPuzzleId());
            json.put("fen", p.getFen());
            json.put("moves", p.getMoves());

            // On s'assure que ce n'est pas un puzzle lichess
            json.put("isLichessPuzzle", false);

            game.setPuzzleId(p.getPuzzleId());
            game.setLichessPuzzle(false);
            game.dechiffre_pb(json);

            // Nettoyage session
            session.removeAttribute("lichessPuzzle");

            return true;
        }

        return false;
    }
}