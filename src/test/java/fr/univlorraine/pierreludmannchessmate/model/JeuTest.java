package fr.univlorraine.pierreludmannchessmate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JeuTest {

    private Jeu jeu;
    private Difficulte difficulteNormal;

    @BeforeEach
    void setUp() {
        difficulteNormal = Difficulte.normal;
        jeu = new Jeu("Mat en 3 coups", "fen_position_123", difficulteNormal);
    }

    @Test
    void constructeurParDefaut_InitialiseJeu() {
        Jeu nouveauJeu = new Jeu();

        assertNotNull(nouveauJeu);
    }

    @Test
    void constructeurAvecParametres_InitialiseJeuCorrectement() {
        assertEquals("Mat en 3 coups", jeu.getNom());
        assertEquals("fen_position_123", jeu.getDonneesProbleme());
        assertEquals(difficulteNormal, jeu.getDifficulte());
    }

    @Test
    void obtenirNom_RetourneNomDefini() {
        assertEquals("Mat en 3 coups", jeu.getNom());
    }

    @Test
    void definirNom_ModifieNomCorrectement() {
        jeu.setNom("Finale de pions");

        assertEquals("Finale de pions", jeu.getNom());
    }

    @Test
    void obtenirDonneesProbleme_RetourneDonneesDefinies() {
        assertEquals("fen_position_123", jeu.getDonneesProbleme());
    }

    @Test
    void definirDonneesProbleme_ModifieDonneesCorrectement() {
        jeu.setDonneesProbleme("fen_position_456");

        assertEquals("fen_position_456", jeu.getDonneesProbleme());
    }

    @Test
    void obtenirDifficulte_RetourneDifficulteDefinie() {
        assertEquals(difficulteNormal, jeu.getDifficulte());
    }

    @Test
    void definirDifficulte_ModifieDifficulteCorrectement() {
        Difficulte difficulteElevee = Difficulte.expert;
        jeu.setDifficulte(difficulteElevee);

        assertEquals(difficulteElevee, jeu.getDifficulte());
    }

    @Test
    void obtenirIdentifiant_RetourneIdAutoGenere() {
        // L'ID est null avant la persistance en base de données
        assertNull(jeu.getId());
    }

    @Test
    void definirIdentifiant_ModifieIdCorrectement() {
        jeu.setId(1L);

        assertEquals(1L, jeu.getId());
    }

    @Test
    void jeu_AvecTousParametres_EstValide() {
        assertEquals("Mat en 3 coups", jeu.getNom());
        assertEquals("fen_position_123", jeu.getDonneesProbleme());
        assertEquals(difficulteNormal, jeu.getDifficulte());
        assertNull(jeu.getId());
    }

    @Test
    void jeu_AvecDifficulteFacile_EstValide() {
        Jeu jeuFacile = new Jeu("Défi facile", "fen_facile", Difficulte.facile);

        assertEquals("Défi facile", jeuFacile.getNom());
        assertEquals(Difficulte.facile, jeuFacile.getDifficulte());
    }

    @Test
    void jeu_AvecDifficulteNormal_EstValide() {
        Jeu jeuNormal = new Jeu("Défi normal", "fen_normal", Difficulte.normal);

        assertEquals("Défi normal", jeuNormal.getNom());
        assertEquals(Difficulte.normal, jeuNormal.getDifficulte());
    }

    @Test
    void jeu_AvecDifficulteDifficile_EstValide() {
        Jeu jeuDifficile = new Jeu("Défi difficile", "fen_difficile", Difficulte.difficile);

        assertEquals("Défi difficile", jeuDifficile.getNom());
        assertEquals(Difficulte.difficile, jeuDifficile.getDifficulte());
    }

    @Test
    void jeu_AvecDifficulteExpert_EstValide() {
        Jeu jeuExpert = new Jeu("Défi expert", "fen_expert", Difficulte.expert);

        assertEquals("Défi expert", jeuExpert.getNom());
        assertEquals(Difficulte.expert, jeuExpert.getDifficulte());
    }

    @Test
    void donneesProbleme_Vide_EstValide() {
        Jeu jeuVide = new Jeu("Jeu", "", difficulteNormal);

        assertEquals("", jeuVide.getDonneesProbleme());
    }

    @Test
    void donneesProbleme_Null_EstValide() {
        Jeu jeuNull = new Jeu("Jeu", null, difficulteNormal);

        assertNull(jeuNull.getDonneesProbleme());
    }

    @Test
    void nom_Vide_EstValide() {
        Jeu jeuSansNom = new Jeu("", "fen_position", difficulteNormal);

        assertEquals("", jeuSansNom.getNom());
    }

    @Test
    void nom_Null_EstValide() {
        Jeu jeuNomNull = new Jeu(null, "fen_position", difficulteNormal);

        assertNull(jeuNomNull.getNom());
    }

    @Test
    void difficulte_Null_EstValide() {
        Jeu jeuDifficulteNull = new Jeu("Jeu", "fen_position", null);

        assertNull(jeuDifficulteNull.getDifficulte());
    }

    @Test
    void multipleJeux_SontIndependants() {
        Jeu jeu2 = new Jeu("Autre défi", "fen_autre", Difficulte.facile);

        assertEquals("Mat en 3 coups", jeu.getNom());
        assertEquals("Autre défi", jeu2.getNom());
        assertNotEquals(jeu.getNom(), jeu2.getNom());
    }
}
