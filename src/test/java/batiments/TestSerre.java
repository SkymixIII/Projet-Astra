package batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import ressources.Ressource;
import ressources.Stock;
import ressources.TypeRessource;

public class TestSerre {

    private Serre serre;
    private Stock stock;

    @Before
    public void setUp() throws Exception {
        // Serre avec rendement de 10 unités de nourriture
        serre = new Serre("Serre Alpha", 3, 7, 10);
        stock = new Stock(false); // mode facile (pas de contrainte globale)
    }

    // ================================================================== //
    //  1. TYPE DE BÂTIMENT                                                //
    // ================================================================== //

    @Test
    public void testTypeBatiment() {
        assertEquals("Le type doit être SERRE", TypeBatiment.SERRE, serre.getType());
    }

    // ================================================================== //
    //  2. OPÉRATIONALITÉ                                                  //
    // ================================================================== //

    @Test
    public void testIsOperationnel_toujours() {
        assertTrue("La serre doit toujours être opérationnelle", serre.isOperationnel());
    }

    // ================================================================== //
    //  3. RÉCOLTE MANUELLE (recolter)                                     //
    // ================================================================== //

    @Test
    public void testRecolter_retourneNourritureAvecBonRendement() {
        Ressource r = serre.recolter();
        assertNotNull("recolter() ne doit pas retourner null", r);
        assertEquals("Le type doit être NOURRITURE", TypeRessource.NOURRITURE, r.getType());
        assertEquals("La quantité doit correspondre au rendement", 10, r.getQuantite());
    }

    // ================================================================== //
    //  4. MISE À JOUR — production automatique (mettreAJour)              //
    // ================================================================== //

    @Test
    public void testMettreAJour_progresseCycle() {
        // TEMPS_POUSSE_SEC = 60. Après 30s, progression = 50%
        double progresBefore = serre.getProgresPousse();
        serre.mettreAJour(stock, 30);
        assertTrue("Le progrès doit avoir augmenté",
                serre.getProgresPousse() > progresBefore);
    }

    @Test
    public void testMettreAJour_cycleComplet_ajouteAuStock() throws Exception {
        // 60 secondes = un cycle complet → nourriture ajoutée au stock
        serre.mettreAJour(stock, 60);
        assertEquals("La nourriture doit être dans le stock après 60s",
                10, stock.getQuantite(TypeRessource.NOURRITURE));
    }

    @Test
    public void testMettreAJour_apresRecolte_progresReset() {
        // Après un cycle, le progrès repart à 0
        serre.mettreAJour(stock, 60);
        assertEquals("Le progrès doit être remis à 0 après récolte",
                0.0, serre.getProgresPousse(), 0.01);
    }

    @Test
    public void testMettreAJour_deuxCycles_stokDoublé() throws Exception {
        serre.mettreAJour(stock, 60); // cycle 1
        serre.mettreAJour(stock, 60); // cycle 2
        assertEquals("Deux cycles doivent produire 2× le rendement",
                20, stock.getQuantite(TypeRessource.NOURRITURE));
    }

    @Test
    public void testMettreAJour_cyclePasEncore_stockInchange() throws Exception {
        // Moins d'un cycle : pas encore de nourriture
        serre.mettreAJour(stock, 30);
        assertEquals("Pas de nourriture avant la fin d'un cycle",
                0, stock.getQuantite(TypeRessource.NOURRITURE));
    }

    // ================================================================== //
    //  5. GESTION DU PERSONNEL (stub — la serre ne gère pas de personnel) //
    // ================================================================== //

    @Test
    public void testADeLaPlace_toujours() {
        assertTrue("La serre doit toujours avoir de la place", serre.aDeLaPlace());
    }

    // ================================================================== //
    //  6. POSITION & NOM                                                  //
    // ================================================================== //

    @Test
    public void testGetNom() {
        assertEquals("Serre Alpha", serre.getNom());
    }

    @Test
    public void testGetPosition() {
        assertEquals(3, serre.getX());
        assertEquals(7, serre.getY());
    }

    @Test
    public void testDeplacer() {
        serre.deplacer(1, 2);
        assertEquals(1, serre.getX());
        assertEquals(2, serre.getY());
    }
}
