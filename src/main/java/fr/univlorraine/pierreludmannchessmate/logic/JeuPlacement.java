package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.Case;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Mode de jeu "placement" (exercice des 8 dames ou variantes).
 *
 * Le joueur place des pièces sur l'échiquier en respectant des contraintes de
 * non-menace et une configuration cible. La classe calcule les erreurs,
 * le score brut et le statut de réussite.
 */
public class JeuPlacement extends AbstractChessGame {
    @Getter @Setter
    private String typeSelectionne = "Dame";

    @Getter @Setter
    private Map<String, Integer> configurationRequise;

    @Getter @Setter
    private String modeDeJeu = "8-dames";

    @Getter @Setter
    private boolean scoreEnregistre = false;

    @Getter private int erreurs = 0;
    @Getter private int placementsValides = 0;
    @Getter private int scoreBrut = 0;
    private boolean aRetire = false; // Pour le calcul du "Perfect"

    public JeuPlacement() {
        super();
        this.configurationRequise = new HashMap<>();
        this.configurationRequise.put("Dame", 8);
    }

    @Override
    public void reinitialiser() {
        super.reinitialiser();
        this.erreurs = 0;
        this.placementsValides = 0;
        this.scoreBrut = 0;
        this.aRetire = false;
        this.scoreEnregistre = false;
    }

    /**
     * Tente de placer une pièce choisie par le joueur sur la case (x, y).
     *
     * @param x colonne (0..7)
     * @param y ligne (0..7)
     * @param typePiece Libellé de la pièce à placer (Roi, Dame, Tour, etc.).
     * @param estBlanc {@code true} si la pièce est blanche.
     * @return Code résultat : "OK" si placé, "OCCUPEE:..." si la case est prise, "INVALID:..." si menace détectée.
     */
    public String placerPieceJoueur(int x, int y, String typePiece, boolean estBlanc) {
        Case c = echiquier.getCase(x, y);
        if (!c.isEstVide()) {
            this.erreurs++;
            return "OCCUPEE:Une pièce occupe déjà cette case.";
        }

        String conflit = getDetailConflit(x, y, typePiece);
        if (conflit != null) {
            this.erreurs++;
            return "INVALID:" + conflit;
        }

        Piece p = factoryPiece(typePiece, estBlanc);
        placerPieceInterne(x, y, p);
        this.placementsValides++;
        this.scoreBrut += poidsPiece(typePiece);
        return "OK";
    }

    /**
     * Vérifie si poser un type de pièce en (targetX, targetY) menace une pièce existante.
     */
    private boolean nouvellePieceMenace(int targetX, int targetY, String typeNouvellePiece) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    int dx = Math.abs(i - targetX);
                    int dy = Math.abs(j - targetY);
                    if (attaque(typeNouvellePiece, dx, dy)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Calcule la matrice des cases menacées ou rendues invalides pour le placement courant.
     *
     * @return matrice 8x8 où {@code true} indique une case à éviter.
     */
    public boolean[][] getMatriceMenaces() {
        boolean[][] menaces = new boolean[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // On affiche les cases rouges selon la pièce que le joueur a en main
                menaces[i][j] = estCaseMenacee(i, j) || nouvellePieceMenace(i, j, this.typeSelectionne);
            }
        }
        return menaces;
    }

    @Override
    public boolean retirerPiece(int x, int y) {
        Case c = echiquier.getCase(x, y);

        // 1. Vérifier si une pièce existe pour récupérer son type avant suppression
        if (c != null && !c.isEstVide() && c.getPiece() != null) {
            String typeDeLaPieceRetiree = c.getPiece().getClass().getSimpleName();

            // 2. Appeler la logique parente pour supprimer physiquement la pièce
            boolean reussite = super.retirerPiece(x, y);

            if (reussite) {
                this.aRetire = true;
                // 3. Soustraire le poids correct basé sur le type identifié
                this.scoreBrut -= poidsPiece(typeDeLaPieceRetiree);
                this.placementsValides--; // Optionnel : décrémenter aussi le compteur de placements
                return true;
            }
        }
        return false;
    }

    /**
     * Donne le poids (valeur de score) d'un type de pièce.
     */
    private int poidsPiece(String type) {
        return switch (type) {
            case "Dame" -> 5;
            case "Tour" -> 4;
            case "Fou", "Cavalier" -> 3;
            case "Roi" -> 2;
            default -> 1;
        };
    }

    /**
     * Score brut accumulé (avant bonus/malus).
     */
    public int getScoreCourant() {
        return scoreBrut;
    }

    /**
     * Calcule le score final sans bonus externes, basé sur les erreurs et le statut perfect.
     */
    public int calculerScoreFinalSansBonus() {
        double facteur = Math.max(0.2, 1.0 - 0.1 * erreurs);
        if (estTentativeParfaite()) facteur += 0.2;
        return (int) Math.round(scoreBrut * facteur);
    }

    /**
     * Indique si la tentative est parfaite (aucune erreur, aucune pièce retirée).
     */
    public boolean estTentativeParfaite() {
        return erreurs == 0 && !aRetire;
    }

    @Override
    public boolean estPuzzleResolu() {
        Map<String, Integer> compte = getCompteActuelCalculated();
        for (Map.Entry<String, Integer> entry : configurationRequise.entrySet()) {
            if (!compte.getOrDefault(entry.getKey(), 0).equals(entry.getValue())) return false;
        }
        return true;
    }

    /**
     * Indique si une case est attaquée par au moins une pièce déjà placée.
     */
    private boolean estCaseMenacee(int targetX, int targetY) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    String typeAttaquant = c.getPiece().getClass().getSimpleName();
                    int dx = Math.abs(i - targetX);
                    int dy = Math.abs(j - targetY);
                    if (attaque(typeAttaquant, dx, dy)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si un type de pièce attaque selon les offsets (dx, dy).
     */
    private boolean attaque(String type, int dx, int dy) {
        return switch (type) {
            case "Dame" -> (dx == 0 || dy == 0) || (dx == dy);
            case "Tour" -> (dx == 0 || dy == 0);
            case "Fou" -> (dx == dy);
            case "Roi" -> (dx <= 1 && dy <= 1);
            case "Cavalier" -> (dx * dy == 2);
            default -> false;
        };
    }

    /**
     * Valide une configuration cible fournie par l'utilisateur.
     *
     * @param config Map type->quantité attendue.
     * @return "OK" si la configuration est acceptable, sinon un message d'erreur.
     */
    public String validerConfiguration(Map<String, Integer> config) {
        int total = 0;
        for (int val : config.values()) total += val;
        if (total == 0) return "Choisissez au moins une pièce.";
        if (total > 64) return "Trop de pièces !";
        return "OK";
    }

    /**
     * Identifie la pièce qui bloque le placement.
     * Retourne une String descriptive ou null si pas de menace.
     */
    public String getDetailConflit(int targetX, int targetY, String typeNouvellePiece) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    String typeExistante = c.getPiece().getClass().getSimpleName();
                    int dx = Math.abs(i - targetX);
                    int dy = Math.abs(j - targetY);
                    String coord = (char)('A' + i) + "" + (j + 1);

                    if (attaque(typeExistante, dx, dy)) {
                        return "Le " + typeExistante + " en " + coord + " menace cette case !";
                    }
                    if (attaque(typeNouvellePiece, dx, dy)) {
                        return "Placer un " + typeNouvellePiece + " ici capturerait le " + typeExistante + " en " + coord + " !";
                    }
                }
            }
        }
        return null;
    }

    public String getSignatureSolution() {
        List<String> positions = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    // Format : Coordonnée (A1, B2...)
                    String coord = (char)('A' + i) + "" + (j + 1);
                    positions.add(coord);
                }
            }
        }
        // On trie pour que l'ordre dans lequel le joueur a placé les pièces n'importe pas
        Collections.sort(positions);
        return String.join("-", positions);
    }
}