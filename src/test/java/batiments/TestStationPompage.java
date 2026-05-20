package batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import ressources.Ressource;
import ressources.Stock;
import ressources.TypeRessource;

public class TestStationPompage {

    private StationPompage station;
    private Stock stock;

    @Before
    public void setUp() throws Exception {
        // Débit de 5 unités d'eau par pompage
        station = new StationPompage("Station Bêta", 2, 8, 5);
        stock   = new Stock(false); // mode facile
    }

    // ================================================================== //
    //  1. TYPE DE BÂTIMENT                                                //
    // ================================================================== //

    @Test
    public void testTypeBatiment() {
        assertEquals("Le type doit être STATION_POMPAGE",
                TypeBatiment.STATION_POMPAGE, station.getType());
    }

    // ================================================================== //
    //  2. OPÉRATIONALITÉ                                                  //
    // ================================================================== //

    @Test
    public void testIsOperationnel_toujours() {
        assertTrue("La station doit toujours être opérationnelle", station.isOperationnel());
    }

    // ================================================================== //
    //  3. POMPAGE MANUEL (pomper)                                         //
    // ================================================================== //

    @Test
    public void testPomper_retourneEauAvecBonDebit() {
        Ressource r = station.pomper();
        assertNotNull("pomper() ne doit pas retourner null", r);
        assertEquals("Le type doit être EAU", TypeRessource.EAU, r.getType());
        assertEquals("La quantité doit correspondre au débit", 5, r.getQuantite());
    }

    @Test
    public void testGetDebitEau() {
        assertEquals("Le débit renvoyé doit être 5", 5, station.getDebitEau());
    }

    // ================================================================== //
    //  4. MISE À JOUR — pompage automatique (mettreAJour)                 //
    // ================================================================== //

    @Test
    public void testMettreAJour_pasDEauAvantIntervalle() throws Exception {
        // INTERVALLE_POMPAGE_SEC = 10 ; après 5 s, pas encore de pompage
        station.mettreAJour(stock, 5);
        assertEquals("Pas d'eau avant l'intervalle de 10 secondes",
                0, stock.getQuantite(TypeRessource.EAU));
    }

    @Test
    public void testMettreAJour_exactementIntervalle_ajouteEau() throws Exception {
        station.mettreAJour(stock, 10);
        assertEquals("Un pompage doit avoir eu lieu à 10 secondes",
                5, stock.getQuantite(TypeRessource.EAU));
    }

    @Test
    public void testMettreAJour_deuxIntervalles_eauDoublee() throws Exception {
        station.mettreAJour(stock, 10); // premier pompage
        station.mettreAJour(stock, 10); // second pompage
        assertEquals("Deux pumpings doivent produire 2× le débit",
                10, stock.getQuantite(TypeRessource.EAU));
    }

    @Test
    public void testMettreAJour_tempsAccumule_pumpingDecale() throws Exception {
        // 7s puis 5s → total 12s → devrait pomper une fois
        station.mettreAJour(stock, 7);
        assertEquals("Pas encore de pompage après 7s", 0, stock.getQuantite(TypeRessource.EAU));
        station.mettreAJour(stock, 5);
        assertEquals("Un pompage doit avoir eu lieu après 12s cumulées",
                5, stock.getQuantite(TypeRessource.EAU));
    }

    // ================================================================== //
    //  5. GESTION DU PERSONNEL (stub — la station n'en a pas)            //
    // ================================================================== //

    @Test
    public void testADeLaPlace_toujours() {
        assertTrue("La station doit toujours avoir de la place", station.aDeLaPlace());
    }

    // ================================================================== //
    //  6. POSITION & NOM                                                  //
    // ================================================================== //

    @Test
    public void testGetNom() {
        assertEquals("Station Bêta", station.getNom());
    }

    @Test
    public void testGetPosition() {
        assertEquals(2, station.getX());
        assertEquals(8, station.getY());
    }

    @Test
    public void testDeplacer() {
        station.deplacer(0, 0);
        assertEquals(0, station.getX());
        assertEquals(0, station.getY());
    }
}
