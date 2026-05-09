package batiments.test_batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.CentreLancement;
import fusee.Fusee;

public class CentreLancementTest {
    private CentreLancement centre;
    private Fusee fuseeDeTest;

    @Before
    public void setUp() {
        centre = new CentreLancement("Fusée très cool", 100, 100); // position x = 100, y = 100
        fuseeDeTest = new Fusee(); 
    }

    @Test
    public void testLancementSansFusee() {
        assertFalse("Le lancement devrait échouer sans fusée", centre.lancerFusee());
    }

    @Test
    public void testLancementReussi() {
        centre.setFusee(fuseeDeTest);
        // Si la fusée est configurée pour réussir par défaut
        boolean resultat = centre.lancerFusee();
        // On vérifie que le résultat du centre est celui de la fusée
        assertEquals(fuseeDeTest.lancer(), resultat);
    }
}
