package fr.univlorraine.pierreludmannchessmate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DefiTest {

    private static class DefiImpl extends Defi {
        public DefiImpl(String nom, String description, int difficulte, Joueur createur) {
            super(nom, description, difficulte, createur);
        }

        @Override
        public boolean validerSolution(Echiquier echiquier) {
            return echiquier != null;
        }

        @Override
        public int calculerScore(int tempsResolution) {
            return 100 - (tempsResolution / 10);
        }
    }

    private Joueur createur;
    private DefiImpl defi;

    @BeforeEach
    void setUp() {
        createur = new Joueur("Alice", true);
        defi = new DefiImpl("Mat en 3 coups", "Trouvez le mat blanc en 3 coups", 3, createur);
    }

    @Test
    void constructeur_InitialiseDefiCorrectement() {
        assertEquals("Mat en 3 coups", defi.getNom());
        assertEquals("Trouvez le mat blanc en 3 coups", defi.getDescription());
        assertEquals(3, defi.getDifficulte());
        assertEquals(createur, defi.getCreateur());
        assertNotNull(defi.getDateCreation());
    }

    @Test
    void obtenirNom_RetourneNomDefini() {
        assertEquals("Mat en 3 coups", defi.getNom());
    }

    @Test
    void obtenirDescription_RetourneDescriptionDefinie() {
        assertEquals("Trouvez le mat blanc en 3 coups", defi.getDescription());
    }

    @Test
    void obtenirDifficulte_RetourneDifficulteDefinie() {
        assertEquals(3, defi.getDifficulte());
    }

    @Test
    void obtenirCreateur_RetourneCreateurDefini() {
        assertEquals(createur, defi.getCreateur());
    }

    @Test
    void obtenirDateCreation_RetourneDateCreation() {
        Date dateCreation = defi.getDateCreation();
        assertNotNull(dateCreation);
        assertTrue(dateCreation.before(new Date()));
    }

    @Test
    void obtenirIdentifiant_RetourneIdUnique() {
        DefiImpl defi2 = new DefiImpl("Autre défi", "Description", 2, createur);
        assertNotEquals(defi.getId(), defi2.getId());
    }

    @Test
    void calculerScore_RetourneScorevecTempsResolution() {
        int score = defi.calculerScore(100);
        assertEquals(90, score);  // 100 - (100/10) = 90
    }

    @Test
    void calculerScore_ScoreDecroitAvecTemps() {
        int score1 = defi.calculerScore(50);
        int score2 = defi.calculerScore(100);
        assertTrue(score1 > score2);
    }

    @Test
    void difficulte_Valide_EntreSureEtCinq() {
        DefiImpl defiDifficile = new DefiImpl("Difficile", "Très difficile", 5, createur);
        assertEquals(5, defiDifficile.getDifficulte());

        DefiImpl defiSimple = new DefiImpl("Simple", "Facile", 1, createur);
        assertEquals(1, defiSimple.getDifficulte());
    }

    @Test
    void dateCreation_EstProcheDeLaCreation() {
        Date avant = new Date();
        DefiImpl nouveauDefi = new DefiImpl("Nouveau", "Desc", 2, createur);
        Date apres = new Date();

        assertTrue(nouveauDefi.getDateCreation().after(avant) ||
                nouveauDefi.getDateCreation().equals(avant));
        assertTrue(nouveauDefi.getDateCreation().before(apres) ||
                nouveauDefi.getDateCreation().equals(apres));
    }

    @Test
    void defi_AvecTousParametres_EstValide() {
        assertEquals("Mat en 3 coups", defi.getNom());
        assertEquals("Trouvez le mat blanc en 3 coups", defi.getDescription());
        assertEquals(3, defi.getDifficulte());
        assertEquals(createur, defi.getCreateur());
        assertNotNull(defi.getDateCreation());
    }

    @Test
    void identifiantAuto_IncrementePourChaqueInstance() {
        DefiImpl defi1 = new DefiImpl("Défi 1", "Desc 1", 1, createur);
        long id1 = defi1.getId();

        DefiImpl defi2 = new DefiImpl("Défi 2", "Desc 2", 2, createur);
        long id2 = defi2.getId();

        assertTrue(id2 > id1);
    }
}
