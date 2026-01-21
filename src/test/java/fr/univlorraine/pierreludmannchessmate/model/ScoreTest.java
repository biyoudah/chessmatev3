package fr.univlorraine.pierreludmannchessmate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    private Score score;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Alice", "alice@test.com", "Password123@");
        score = new Score(120, true);
    }

    @Test
    void constructeurParDefaut_InitialiseScore() {
        Score nouveauScore = new Score();

        assertNotNull(nouveauScore);
        assertEquals(0, nouveauScore.getPoints());
        assertEquals(0, nouveauScore.getScore());
    }

    @Test
    void constructeurAvecParametres_InitialiseTempsResolutionEtReussi() {
        Score scoreTest = new Score(300, true);

        assertEquals(300, scoreTest.getTempsResolution());
        assertTrue(scoreTest.isReussi());
    }

    @Test
    void definirUtilisateur_ModifieUtilisateurCorrectement() {
        score.setUtilisateur(utilisateur);

        assertEquals(utilisateur, score.getUtilisateur());
    }

    @Test
    void definirMode_ModifieModecorrectement() {
        score.setMode("PLACEMENT");

        assertEquals("PLACEMENT", score.getMode());
    }

    @Test
    void definirCleSchema_ModifieCleSchemaCorrectement() {
        score.setSchemaKey("schema_001");

        assertEquals("schema_001", score.getSchemaKey());
    }

    @Test
    void definirPoints_ModifiePointsCorrectement() {
        score.setPoints(150);

        assertEquals(150, score.getPoints());
    }

    @Test
    void definirScore_ModifieScoreCorrectement() {
        score.setScore(500);

        assertEquals(500, score.getScore());
    }

    @Test
    void definirBonusPremierSchema_ModifieBonusCorrectement() {
        score.setBonusPremierSchemaAttribue(50);

        assertEquals(50, score.getBonusPremierSchemaAttribue());
    }

    @Test
    void definirErreurs_ModifieErreursCorrectement() {
        score.setErreursPlacement(3);

        assertEquals(3, score.getErreursPlacement());
    }

    @Test
    void definirParfait_ModifieParfaitCorrectement() {
        score.setFirstTime(true);

        assertTrue(score.isFirstTime());
    }

    @Test
    void definirTempsResolution_ModifieTempsCorrectement() {
        score.setTempsResolution(450);

        assertEquals(450, score.getTempsResolution());
    }

    @Test
    void definirReussi_ModifieReussiCorrectement() {
        score.setReussi(true);

        assertTrue(score.isReussi());
    }

    @Test
    void obtenirIdentifiant_RetourneIdAutoGenere() {
        // L'ID est null avant la persistance
        assertNull(score.getId());
    }

    @Test
    void obtenirUtilisateur_RetourneUtilisateurDefini() {
        score.setUtilisateur(utilisateur);

        assertEquals(utilisateur, score.getUtilisateur());
    }

    @Test
    void obtenirMode_RetourneModeDefini() {
        score.setMode("PUZZLE");

        assertEquals("PUZZLE", score.getMode());
    }

    @Test
    void obtenirCleSchema_RetourneCleSchemaDefinie() {
        score.setSchemaKey("puzzle_123");

        assertEquals("puzzle_123", score.getSchemaKey());
    }

    @Test
    void obtenirPoints_RetournePointsDefinis() {
        score.setPoints(200);

        assertEquals(200, score.getPoints());
    }

    @Test
    void obtenirScore_RetourneScoreDefini() {
        score.setScore(800);

        assertEquals(800, score.getScore());
    }

    @Test
    void obtenirBonusPremierSchema_RetourneBonusDefini() {
        score.setBonusPremierSchemaAttribue(75);

        assertEquals(75, score.getBonusPremierSchemaAttribue());
    }

    @Test
    void obtenirErreurs_RetourneErreursDefinies() {
        score.setErreursPlacement(2);

        assertEquals(2, score.getErreursPlacement());
    }

    @Test
    void obtenirParfait_RetourneParfaitDefini() {
        score.setFirstTime(true);

        assertTrue(score.isFirstTime());
    }

    @Test
    void obtenirTempsResolution_RetourneTempsDefini() {
        score.setTempsResolution(600);

        assertEquals(600, score.getTempsResolution());
    }

    @Test
    void obtenirReussi_RetourneReussiDefini() {
        score.setReussi(false);

        assertFalse(score.isReussi());
    }

    @Test
    void score_AvecTousParametres_EstValide() {
        score.setUtilisateur(utilisateur);
        score.setMode("PLACEMENT");
        score.setSchemaKey("schema_42");
        score.setPoints(250);
        score.setScore(1000);
        score.setBonusPremierSchemaAttribue(100);
        score.setErreursPlacement(1);
        score.setFirstTime(true);
        score.setTempsResolution(180);
        score.setReussi(true);

        assertEquals(utilisateur, score.getUtilisateur());
        assertEquals("PLACEMENT", score.getMode());
        assertEquals("schema_42", score.getSchemaKey());
        assertEquals(250, score.getPoints());
        assertEquals(1000, score.getScore());
        assertEquals(100, score.getBonusPremierSchemaAttribue());
        assertEquals(1, score.getErreursPlacement());
        assertTrue(score.isFirstTime());
        assertEquals(180, score.getTempsResolution());
        assertTrue(score.isReussi());
    }

    @Test
    void tempsResolutionParDefaut_EstZero() {
        Score nouveauScore = new Score();

        assertEquals(0, nouveauScore.getTempsResolution());
    }

    @Test
    void reussiParDefaut_EstFalse() {
        Score nouveauScore = new Score();

        assertFalse(nouveauScore.isReussi());
    }

    @Test
    void pointsParDefaut_EstZero() {
        Score nouveauScore = new Score();

        assertEquals(0, nouveauScore.getPoints());
    }

    @Test
    void scoreParDefaut_EstZero() {
        Score nouveauScore = new Score();

        assertEquals(0, nouveauScore.getScore());
    }
}
