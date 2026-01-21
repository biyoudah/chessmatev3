package fr.univlorraine.pierreludmannchessmate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeDeJeuTest {

    private TypeDeJeu typeDeJeu;

    @BeforeEach
    void setUp() {
        typeDeJeu = new TypeDeJeu(1L, "Placement");
    }

    @Test
    void constructeurParDefaut_InitialiseTypeDeJeu() {
        TypeDeJeu nouveauType = new TypeDeJeu();

        assertNotNull(nouveauType);
    }

    @Test
    void constructeurAvecParametres_InitialiseTypeDeJeuCorrectement() {
        assertEquals(1L, typeDeJeu.getId());
        assertEquals("Placement", typeDeJeu.getNomTypeDeJeu());
    }

    @Test
    void obtenirIdentifiant_RetourneIdDefini() {
        assertEquals(1L, typeDeJeu.getId());
    }

    @Test
    void definirIdentifiant_ModifieIdCorrectement() {
        typeDeJeu.setId(2L);

        assertEquals(2L, typeDeJeu.getId());
    }

    @Test
    void obtenirNomTypeDeJeu_RetourneNomDefini() {
        assertEquals("Placement", typeDeJeu.getNomTypeDeJeu());
    }

    @Test
    void definirNomTypeDeJeu_ModifieNomCorrectement() {
        typeDeJeu.setNomTypeDeJeu("Puzzle");

        assertEquals("Puzzle", typeDeJeu.getNomTypeDeJeu());
    }

    @Test
    void typeDeJeu_AvecTousParametres_EstValide() {
        assertEquals(1L, typeDeJeu.getId());
        assertEquals("Placement", typeDeJeu.getNomTypeDeJeu());
    }

    @Test
    void typeDeJeu_Placement_EstValide() {
        TypeDeJeu typePlacement = new TypeDeJeu(1L, "Placement");

        assertEquals(1L, typePlacement.getId());
        assertEquals("Placement", typePlacement.getNomTypeDeJeu());
    }

    @Test
    void typeDeJeu_Puzzle_EstValide() {
        TypeDeJeu typePuzzle = new TypeDeJeu(2L, "Puzzle");

        assertEquals(2L, typePuzzle.getId());
        assertEquals("Puzzle", typePuzzle.getNomTypeDeJeu());
    }

    @Test
    void typeDeJeu_Defi_EstValide() {
        TypeDeJeu typeDefi = new TypeDeJeu(3L, "Défi");

        assertEquals(3L, typeDefi.getId());
        assertEquals("Défi", typeDefi.getNomTypeDeJeu());
    }

    @Test
    void nomTypeDeJeu_Vide_EstValide() {
        TypeDeJeu typeVide = new TypeDeJeu(1L, "");

        assertEquals("", typeVide.getNomTypeDeJeu());
    }

    @Test
    void nomTypeDeJeu_Null_EstValide() {
        TypeDeJeu typeNull = new TypeDeJeu(1L, null);

        assertNull(typeNull.getNomTypeDeJeu());
    }

    @Test
    void identifiant_Null_EstValide() {
        TypeDeJeu typeIdNull = new TypeDeJeu(null, "Placement");

        assertNull(typeIdNull.getId());
    }

    @Test
    void identifiant_Zero_EstValide() {
        TypeDeJeu typeIdZero = new TypeDeJeu(0L, "Placement");

        assertEquals(0L, typeIdZero.getId());
    }

    @Test
    void multipleTypesDeJeu_SontIndependants() {
        TypeDeJeu type2 = new TypeDeJeu(2L, "Puzzle");

        assertEquals(1L, typeDeJeu.getId());
        assertEquals("Placement", typeDeJeu.getNomTypeDeJeu());
        assertEquals(2L, type2.getId());
        assertEquals("Puzzle", type2.getNomTypeDeJeu());
        assertNotEquals(typeDeJeu.getId(), type2.getId());
        assertNotEquals(typeDeJeu.getNomTypeDeJeu(), type2.getNomTypeDeJeu());
    }

    @Test
    void typeDeJeu_ModificationId_EstValide() {
        typeDeJeu.setId(5L);
        typeDeJeu.setNomTypeDeJeu("Étude");

        assertEquals(5L, typeDeJeu.getId());
        assertEquals("Étude", typeDeJeu.getNomTypeDeJeu());
    }

    @Test
    void typeDeJeu_AvecNomLong_EstValide() {
        String nomLong = "Type de Jeu avec un très long nom pour tester";
        TypeDeJeu typeLongNom = new TypeDeJeu(1L, nomLong);

        assertEquals(nomLong, typeLongNom.getNomTypeDeJeu());
    }
}
