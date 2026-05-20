package carte;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import entites.Ouvrier;

public class TestCase {

    private Case caseVide;
    private Ouvrier ouvrier1;
    private Ouvrier ouvrier2;

    @Before
    public void setUp() {
        caseVide = new Case();
        ouvrier1 = new Ouvrier("Alice", 0, 0);
        ouvrier2 = new Ouvrier("Bob",   0, 0);
    }

    // ================================================================== //
    //  1. INITIALISATION                                                  //
    // ================================================================== //

    @Test
    public void init_caseVide_pasOccupee() {
        assertFalse("Une case fraîchement créée ne doit pas être occupée",
                caseVide.estOccupee());
    }

    @Test
    public void init_contenuVide() {
        assertTrue("Le contenu initial doit être vide", caseVide.getContenu().isEmpty());
    }

    // ================================================================== //
    //  2. AJOUTER                                                         //
    // ================================================================== //

    @Test
    public void ajouter_unItem_caseEstOccupee() {
        caseVide.ajouter(ouvrier1);
        assertTrue(caseVide.estOccupee());
    }

    @Test
    public void ajouter_unItem_contenuContientItem() {
        caseVide.ajouter(ouvrier1);
        assertTrue(caseVide.getContenu().contains(ouvrier1));
    }

    @Test
    public void ajouter_deuxItems_contenuContientLesDeux() {
        caseVide.ajouter(ouvrier1);
        caseVide.ajouter(ouvrier2);
        assertTrue(caseVide.getContenu().contains(ouvrier1));
        assertTrue(caseVide.getContenu().contains(ouvrier2));
        assertEquals(2, caseVide.getContenu().size());
    }

    @Test
    public void ajouter_null_ignoreSansCrash() {
        caseVide.ajouter(null); // ne doit pas lever d'exception
        assertFalse("Un null ne doit pas rendre la case occupée", caseVide.estOccupee());
    }

    // ================================================================== //
    //  3. SUPPRIMER                                                       //
    // ================================================================== //

    @Test
    public void supprimer_itemPresent_videCase() {
        caseVide.ajouter(ouvrier1);
        caseVide.supprimer(ouvrier1);
        assertFalse(caseVide.estOccupee());
    }

    @Test
    public void supprimer_unParmiDeux_lautreReste() {
        caseVide.ajouter(ouvrier1);
        caseVide.ajouter(ouvrier2);
        caseVide.supprimer(ouvrier1);
        assertFalse(caseVide.getContenu().contains(ouvrier1));
        assertTrue(caseVide.getContenu().contains(ouvrier2));
    }

    @Test
    public void supprimer_itemAbsent_pasDeException() {
        caseVide.supprimer(ouvrier1); // ne doit pas lever d'exception
        assertFalse(caseVide.estOccupee());
    }

    // ================================================================== //
    //  4. IMMUTABILITÉ DU CONTENU RETOURNÉ                                //
    // ================================================================== //

    @Test
    public void getContenu_retourneCopie_pasLaListeInterne() {
        caseVide.ajouter(ouvrier1);
        // Modifier la liste retournée ne doit pas affecter la case
        caseVide.getContenu().clear();
        assertTrue("La modification de la liste retournée ne doit pas vider la case",
                caseVide.estOccupee());
    }
}
