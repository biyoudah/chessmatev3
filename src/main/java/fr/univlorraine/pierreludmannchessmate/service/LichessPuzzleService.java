package fr.univlorraine.pierreludmannchessmate.service;

import com.github.bhlangonijr.chesslib.Board;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class LichessPuzzleService {
    private static final Logger logger = LoggerFactory.getLogger(LichessPuzzleService.class);
    private static final String LICHESS_API_URL = "https://lichess.org/api/puzzle";
    private final RestTemplate restTemplate;

    /**
     * Service d'accès aux puzzles Lichess (daily ou par identifiant).
     *
     * @param restTemplate client HTTP injecté par Spring
     */
    public LichessPuzzleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère le puzzle quotidien Lichess puis ses détails complets.
     *
     * @return objet JSON prêt à être consommé par le frontend, ou {@code null} en cas d'erreur.
     */
    public JSONObject getPuzzleRandom() {
        try {
            String url = LICHESS_API_URL + "/daily";
            logger.info("Appel Lichess API /daily");

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    getHttpEntity(),
                    String.class
            );

            logger.info("Réponse reçue: " + response.getStatusCode());
            JSONObject jsonResponse = new JSONObject(response.getBody());

            String puzzleId = jsonResponse.getJSONObject("puzzle").getString("id");
            logger.info("Puzzle ID du jour: " + puzzleId);

            return getPuzzleById(puzzleId);

        } catch (Exception e) {
            logger.error("ERREUR Lichess API daily:", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Récupère un puzzle par identifiant via l'API Lichess et prépare un JSON (fen, moves...).
     */
    private JSONObject getPuzzleById(String puzzleId) {
        try {
            String url = LICHESS_API_URL + "/" + puzzleId;
            logger.info("Appel Lichess API /puzzle/{}", puzzleId);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    getHttpEntity(),
                    String.class
            );

            JSONObject jsonResponse = new JSONObject(response.getBody());
            System.out.println(jsonResponse);
            JSONObject puzzleObj = jsonResponse.getJSONObject("puzzle");
            JSONObject gameObj = jsonResponse.getJSONObject("game");

            String pgn = gameObj.getString("pgn");
            System.out.println("pgn: " + pgn);
            int initialPly = puzzleObj.getInt("initialPly");
            JSONArray solutionMoves = puzzleObj.getJSONArray("solution");

            // ✅ STRATÉGIE ROBUSTE
            String fen;
            if (puzzleObj.has("fen")) {
                fen = puzzleObj.getString("fen");
                logger.info("FEN fournie par Lichess");
            } else {
                fen = movesToFen(pgn, initialPly);
                logger.warn("⚠FEN absente → calculée depuis le PGN au ply {}", initialPly);
                System.out.println("fen: " + fen);

            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < solutionMoves.length(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(solutionMoves.getString(i));
            }
            String solutionStr = sb.toString();

                boolean traitAuBlanc = extraireTrait(fen, initialPly);

            JSONObject result = new JSONObject();
            result.put("PuzzleId", puzzleObj.getString("id"));
            result.put("puzzleId", puzzleObj.getString("id")); // doublon minuscule pour le frontend
            result.put("fen", fen);
            result.put("moves", solutionStr);
            result.put("initialPly", initialPly);
            result.put("pgn", pgn);
                result.put("traitAuBlanc", traitAuBlanc);

            logger.info("✅ Puzzle prêt | FEN = {}", fen);
            return result;

        } catch (Exception e) {
            logger.error("❌ Erreur puzzle", e);
            return null;
        }
    }

    /**
     * Détermine le trait à partir du FEN (ou, en secours, du parity initialPly).
     */
    private boolean extraireTrait(String fen, int initialPly) {
        try {
            String[] parts = fen.trim().split(" ");
            if (parts.length > 1) {
                return "w".equals(parts[1]);
            }
        } catch (Exception ignored) { }
        return initialPly % 2 == 0;
    }


    /**
     * Convertit une séquence PGN en FEN après un certain nombre de demi-coups.
     * @param pgn partie complète au format PGN
     * @param initialPly nombre de demi-coups à jouer
     * @return FEN résultante
     */
    public static String movesToFen(String pgn, int initialPly) throws Exception {
        Board board = new Board();
        initialPly +=1;
        System.out.println(initialPly);


        String movesSection = pgn.contains("\n\n")
                ? pgn.substring(pgn.indexOf("\n\n") + 2)
                : pgn;
        movesSection = movesSection
                .replaceAll("\\{[^}]*\\}", " ")   // commentaires { ... }
                .replaceAll("\\([^)]*\\)", " ")   // variantes ( ... )
                .replaceAll("\\s+", " ")
                .trim();

        String[] tokens = movesSection.split(" ");
        int played = 0;

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (token.matches("\\d+\\.") || token.matches("\\d+\\.\\.\\.")) continue; // numéros de coups
            if (token.equals("1-0") || token.equals("0-1") || token.equals("1/2-1/2") || token.equals("*")) break;

            if (played >= initialPly) break;

            board.doMove(token);
            played++;
        }

        logger.info("FEN calculée depuis PGN après {} demi-coups: {}", played, board.getFen());
        return board.getFen();
    }




    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Chessmate-Univ-Lorraine");
        headers.set("Accept", "application/json");
        return new HttpEntity<>(headers);
    }
}
