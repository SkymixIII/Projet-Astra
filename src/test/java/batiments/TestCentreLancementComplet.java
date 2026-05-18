package batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.CentreLancement;
import fusee.Fusee;
import fusee.ModuleFusee;
import fusee.Propulseur;
import fusee.ChargeUtile;
import fusee.OrdiDeBord;
import entites.Ouvrier;
import ressources.Stock;
import ressources.TypeRessource;
import exceptions.RessourceInsuffisanteException;

/**
 * Tests complets pour CentreLancement.
 * Couvre : lancement, affectation personnel, assemblage modules.
 */
public class TestCentreLancementComplet {
    private CentreLancement centre;
    private Fusee fusee;
    private Stock stock;
    private Ouvrier o1, o2, o3;

    @Before
    public void setUp() throws Exception {
        centre = new CentreLancement("Centre Alpha", 100, 100);
        fusee = new Fusee();
        stock = new Stock(false);
        
        // Pré-remplir le stock avec les ressources nécessaires
        stock.ajouter(TypeRessource.ACIER, 100);
        stock.ajouter(TypeRessource.KEROSENE, 100);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 100);
        stock.ajouter(TypeRessource.PLASTIQUE, 100);
        stock.ajouter(TypeRessource.CARTE_MERE, 100);
        stock.ajouter(TypeRessource.PROCESSEUR_VOL, 100);
        
        o1 = new Ouvrier("Technicien1", 100, 100);
        o2 = new Ouvrier("Technicien2", 100, 100);
        o3 = new Ouvrier("Scientist", 100, 100);
    }

    // ================================================================
    //  Tests de lancement (basiques)
    // ================================================================

    @Test
    public void testLancementSansFusee() {
        assertFalse("Le lancement devrait échouer sans fusée", centre.lancerFusee());
    }

    @Test
    public void testLancementAvecFuseeNonPrete() {
        centre.setFusee(fusee);
        // Fusée à 0% ne doit pas décoller
        assertFalse("La fusée non assemblée ne doit pas décoller", centre.lancerFusee());
    }

    @Test
    public void testLancementAvecFuseeComplete() throws RessourceInsuffisanteException {
        centre.setFusee(fusee);
        
        // Assembler tous les modules
        assemblerPropulseur(fusee.getPropulseur());
        assemblerChargeUtile(fusee.getChargeUtile());
        assemblerOrdiDeBord(fusee.getOrdiDeBord());
        
        // /!\ AJOUT : Synchroniser l'état de la fusée après l'assemblage
        fusee.synchroniserEtats();
        
        // Fusée complète, embarquer ingénieur
        fusee.setIngenieurABord(true);
        
        assertTrue("La fusée complète avec ingénieur doit décoller", centre.lancerFusee());
    }

    // ================================================================
    //  Tests d'affectation de personnel
    // ================================================================

    @Test
    public void testAffecterPersonnel() {
        assertTrue("Le centre doit avoir de la place initialement", centre.aDeLaPlace());
        centre.affecterPersonnel(o1);
        assertTrue("Après 1 ouvrier, il doit rester de la place", centre.aDeLaPlace());
    }

    //@Test -- TODO V2 : CentreLancement n'a pas de capacité max pour l'instant
    //public void testAffecterMultiplePersonnel() {
        //for (int i = 0; i < 5; i++) {
         //   Ouvrier o = new Ouvrier("Worker" + i, 100, 100);
           // centre.affecterPersonnel(o);
            //if (i < 4) {
              //  assertTrue("Capacité non atteinte à " + (i+1) + " ouvriers", centre.aDeLaPlace());
            //}
        //}
        // À la 5ème, on atteint la capacité max
        //assertFalse("À capacité max, aDeLaPlace doit retourner false", centre.aDeLaPlace());
    //}

    //@Test
    //public void testRetirerPersonnel() {
        //centre.affecterPersonnel(o1);
        //centre.affecterPersonnel(o2);
        //assertFalse("À 2 ouvriers, la place est limitée", centre.aDeLaPlace());
        
        //centre.retirerPersonnel(o1);
        //assertTrue("Après retrait, il doit y avoir de la place", centre.aDeLaPlace());
   // }

    // ================================================================
    //  Tests d'assemblage de modules
    // ================================================================

    //URGENT //@Test
//public void testLancementFuseeCompleteOK() {
  //  fusee.getPropulseur().assembler(1.0f);
    //fusee.getChargeUtile().assembler(1.0f);
    //fusee.getOrdiDeBord().assembler(1.0f);
    //fusee.synchroniserEtats(); // ← OBLIGATOIRE
    //fusee.setIngenieurABord(true);
    //centre.setFusee(fusee);
    //assertTrue(centre.lancerFusee());
//}

    @Test
    public void testFuseePropertiesAfterCreation() {
        assertNotNull("La fusée ne doit pas être null après création", fusee);
        assertNotNull("Propulseur ne doit pas être null", fusee.getPropulseur());
        assertNotNull("ChargeUtile ne doit pas être null", fusee.getChargeUtile());
        assertNotNull("OrdiDeBord ne doit pas être null", fusee.getOrdiDeBord());
    }

    @Test
    public void testModulesInitializeToZero() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        assertEquals("Propulseur initialement à 0%", 0.0f, p.getPourcentageAssemblage(), 0.01f);
        assertEquals("ChargeUtile initialement à 0%", 0.0f, c.getPourcentageAssemblage(), 0.01f);
        assertEquals("OrdiDeBord initialement à 0%", 0.0f, o.getPourcentageAssemblage(), 0.01f);
    }

    @Test
    public void testAssemblerModuleProgression() {
        Propulseur p = fusee.getPropulseur();
        
        p.assembler(0.3f);
        assertEquals("Après assembler 0.3, à 30%", 0.3f, p.getPourcentageAssemblage(), 0.01f);
        assertFalse("Pas encore terminé", p.estTermine());
        
        p.assembler(0.7f);
        assertEquals("Après assembler 0.7 de plus, à 100%", 1.0f, p.getPourcentageAssemblage(), 0.01f);
        assertTrue("Maintenant c'est terminé", p.estTermine());
    }

    @Test
    public void testModulePlafonnementMax() {
        Propulseur p = fusee.getPropulseur();
        
        p.assembler(1.5f);  // Plus que 100%
        assertEquals("Doit être plafonné à 1.0", 1.0f, p.getPourcentageAssemblage(), 0.01f);
        assertTrue("Doit être considéré comme terminé", p.estTermine());
    }

    @Test
    public void testModulesCombined() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(0.5f);
        c.assembler(0.6f);
        o.assembler(0.7f);
        
        assertFalse("Pas tous terminés", fusee.tousModulesAssembles());
        
        p.assembler(0.5f);
        c.assembler(0.4f);
        o.assembler(0.3f);
        
        assertTrue("Tous terminés", fusee.tousModulesAssembles());
    }

    // ================================================================
    //  Tests de propriétés du centre
    // ================================================================

    @Test
    public void testCoordonnees() {
        assertEquals("X doit être 100", 100, centre.getX());
        assertEquals("Y doit être 100", 100, centre.getY());
    }

    @Test
    public void testOperationnel() {
        assertTrue("Le centre doit être opérationnel par défaut", centre.isOperationnel());
    }

    @Test
    public void testType() {
        assertEquals("Le type doit être ZONE_ASSEMBLAGE", TypeBatiment.ZONE_ASSEMBLAGE, centre.getType());
    }

    // ================================================================
    //  Helpers privés
    // ================================================================

    private void assemblerPropulseur(Propulseur p) throws RessourceInsuffisanteException {
    p.assembler(1.0f);
}

private void assemblerChargeUtile(ChargeUtile c) throws RessourceInsuffisanteException {
    c.assembler(1.0f);
}

private void assemblerOrdiDeBord(OrdiDeBord o) throws RessourceInsuffisanteException {
    o.assembler(1.0f);
}
}
