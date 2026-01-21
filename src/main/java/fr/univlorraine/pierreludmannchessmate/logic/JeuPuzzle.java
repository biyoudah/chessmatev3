package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.Dame;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

public class JeuPuzzle extends AbstractChessGame {

    private String[] solutionMoves;
    private int indexCoupActuel;

    // NOUVEAU : Stockage du FEN initial pour le bouton Reset
    @Getter @Setter
    private String fenDepart;

    @Getter
    private String lastMoveStart = null;
    @Getter
    private String lastMoveEnd = null;

    @Getter
    private boolean partieCommencee = false;
    @Getter
    private boolean ordiDoitJouer = false;
    @Getter
    private boolean vueJoueurEstBlanc;

    @Getter @Setter
    private String difficulte = "any";
    @Getter @Setter
    private String puzzleId = "????";
    @Getter @Setter
    private boolean scoreEnregistre = false;
    @Getter @Setter
    private boolean isLichessPuzzle = false;

    public JeuPuzzle() {
        super();
        this.indexCoupActuel = 0;
    }

    public void dechiffre_pb(JSONObject le_pb) {
        viderPlateau();

        if (le_pb.has("isLichessPuzzle")) {
            this.isLichessPuzzle = le_pb.getBoolean("isLichessPuzzle");
        }

        if (le_pb.has("fen")) {
            String fen = le_pb.getString("fen");
            this.fenDepart = fen; // Sauvegarde du FEN initial
            super.chargerFen(fen);
        }

        if (le_pb.has("moves")) {
            this.solutionMoves = le_pb.getString("moves").split(" ");
        }

        if (le_pb.has("PuzzleId")) {
            this.puzzleId = le_pb.getString("PuzzleId");
        } else if (le_pb.has("puzzleId")) {
            this.puzzleId = le_pb.getString("puzzleId");
        } else {
            this.puzzleId = "????";
        }

        if (le_pb.has("traitAuBlanc")) {
            this.setTraitAuBlanc(le_pb.getBoolean("traitAuBlanc"));
        }

        // Configuration initiale du puzzle
        setupInitialMoves();

        this.vueJoueurEstBlanc = this.isTraitAuBlanc();
        this.partieCommencee = true;
    }

    // Méthode helper pour configurer le premier coup auto si nécessaire
    private void setupInitialMoves() {
        if (solutionMoves != null && solutionMoves.length > 0) {
            // Si c'est Lichess, l'API donne parfois le coup de l'adversaire qui mène à la position
            // Mais dans votre logique, Lichess semble déjà donner la position AVANT le coup.
            // Adaptation selon votre logique existante :
            if (this.isLichessPuzzle) {
                this.indexCoupActuel = 0;
            } else {
                // Pour les puzzles locaux, souvent le premier move de la séquence est le dernier coup de l'adversaire
                jouerCoupInterne(solutionMoves[0]);
                this.indexCoupActuel = 1;
            }
        }
    }

    /**
     * Recommence le puzzle actuel au début (utile pour Lichess).
     */
    public void recommencerPuzzleActuel() {
        // On ne vide pas tout (on garde l'ID et les moves), on recharge juste le plateau
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                retirerPiece(x, y);
            }
        }

        // Recharge le FEN initial
        if (this.fenDepart != null) {
            super.chargerFen(this.fenDepart);
        }

        // Reset des états de jeu
        this.partieCommencee = true;
        this.ordiDoitJouer = false;
        this.lastMoveStart = null;
        this.lastMoveEnd = null;
        this.scoreEnregistre = false;

        // Remet l'index au début selon le type de puzzle
        setupInitialMoves();
    }

    public String jouerCoupJoueur(int xDep, int yDep, int xArr, int yArr) {
        String caseDep = coordsToUci(xDep, yDep);
        String caseArr = coordsToUci(xArr, yArr);
        String coupJoue = caseDep + caseArr;

        if (solutionMoves == null || indexCoupActuel >= solutionMoves.length) return "FINI";

        String coupAttendu = solutionMoves[indexCoupActuel];

        // Gestion promotion (ex: a7a8q)
        if (coupAttendu.length() > 4 && coupJoue.length() == 4) {
            if (!coupAttendu.startsWith(coupJoue)) return "RATE";
            coupJoue += "q";
        }

        if (!coupJoue.equals(coupAttendu)) {
            return "RATE";
        }

        jouerCoupInterne(coupJoue);
        indexCoupActuel++;

        if (estPuzzleResolu()) {
            this.ordiDoitJouer = false;
            return "GAGNE";
        }

        this.ordiDoitJouer = true;
        return "CONTINUE";
    }

    public void reponseOrdinateur() {
        if (this.ordiDoitJouer && solutionMoves != null && indexCoupActuel < solutionMoves.length) {
            jouerCoupInterne(solutionMoves[indexCoupActuel]);
            indexCoupActuel++;
            this.ordiDoitJouer = false;
        }
    }

    @Override
    public boolean estPuzzleResolu() {
        if (solutionMoves == null) return false;
        return indexCoupActuel >= solutionMoves.length;
    }

    public boolean isPuzzleLoaded() {
        return solutionMoves != null && solutionMoves.length > 0;
    }

    public void viderPlateau() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                retirerPiece(x, y);
            }
        }
        this.solutionMoves = null;
        this.indexCoupActuel = 0;
        this.partieCommencee = false;
        this.ordiDoitJouer = false;
        this.setTraitAuBlanc(true);
        this.puzzleId = "????";
        this.isLichessPuzzle = false;
        this.lastMoveStart = null;
        this.lastMoveEnd = null;
        this.fenDepart = null;
    }

    private void jouerCoupInterne(String uciMove) {
        int xDep = uciToX(uciMove.charAt(0));
        int yDep = uciToY(uciMove.charAt(1));
        int xArr = uciToX(uciMove.charAt(2));
        int yArr = uciToY(uciMove.charAt(3));

        Piece p = getPieceObject(xDep, yDep);
        if (p == null) return;

        retirerPiece(xDep, yDep);
        retirerPiece(xArr, yArr);

        if (uciMove.length() > 4) {
            p = new Dame(p.estBlanc());
        }

        placerPieceInterne(xArr, yArr, p);
        this.setTraitAuBlanc(!p.estBlanc());

        this.lastMoveStart = "" + uciMove.charAt(0) + uciMove.charAt(1);
        this.lastMoveEnd = "" + uciMove.charAt(2) + uciMove.charAt(3);
    }

    public String getCoupAide() {
        if (solutionMoves == null || indexCoupActuel >= solutionMoves.length) {
            return null;
        }
        String move = solutionMoves[indexCoupActuel];
        return uciToY(move.charAt(1)) + "," + uciToX(move.charAt(0));
    }

    public int getNbCoups() {
        if (solutionMoves == null || solutionMoves.length == 0) return 0;
        return isLichessPuzzle ? solutionMoves.length / 2 + 1 : solutionMoves.length / 2;
    }

    private String coordsToUci(int x, int y) {
        return "" + (char) ('a' + x) + (char) ('1' + y);
    }

    private int uciToX(char c) { return c - 'a'; }
    private int uciToY(char c) { return c - '1'; }
}