package code;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestLieuDeRessource {
    private LieuDeRessource mineFer;

    @Before
    public void setUp() {
        // Initialisation avant chaque test : Mine avec 100 unités
        mineFer = new LieuDeRessource("Mine de Fer", TypeRessource.FER, 100, 5, 5);
    }

    @Test
    public void testExploiterStandard() {
        int extrait = mineFer.exploiter(30);
        assertEquals("L'extraction doit renvoyer la quantité demandée", 30, extrait);
        assertEquals("Le stock du gisement doit diminuer", 70, mineFer.getQuantiteRestante());
    }

    @Test
    public void testExploiterEpuisement() {
        int extrait = mineFer.exploiter(150); 
        assertEquals("On ne peut pas extraire plus que le maximum disponible", 100, extrait);
        assertEquals("Le gisement doit être vide", 0, mineFer.getQuantiteRestante());
        assertFalse("Le gisement ne doit plus être opérationnel", mineFer.isOperationnel());
    }

    @Test
    public void testExploiterNegatif() {
        int extrait = mineFer.exploiter(-10);
        assertEquals("Une extraction négative doit renvoyer 0", 0, extrait);
        assertEquals("Le stock ne doit pas bouger", 100, mineFer.getQuantiteRestante());
    }
}