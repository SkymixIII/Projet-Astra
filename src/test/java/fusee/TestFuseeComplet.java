package fusee;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import fusee.Fusee;
import fusee.ModuleFusee;
import fusee.Propulseur;
import fusee.ChargeUtile;
import fusee.OrdiDeBord;
import ressources.TypeRessource;
import exceptions.LancementImpossibleException;

/**
 * Tests complets pour Fusee et ModuleFusee.
 * Couvre : probabilité de succès, lancement, modules, synchronisation, condition victoire.
 */
public class TestFuseeComplet {
    private Fusee fusee;

    @Before
    public void setUp() {
        fusee = new Fusee();
    }

    // ================================================================
    //  Tests d'initialisation
    // ================================================================

    @Test
    public void testInitialisation() {
        assertEquals("Propulseur initialement à 0%", 0.0f, fusee.getEtatPropulseur(), 0.01f);
        assertEquals("ChargeUtile initialement à 0%", 0.0f, fusee.getEtatChargeUtile(), 0.01f);
        assertEquals("OrdiDeBord initialement à 0%", 0.0f, fusee.getEtatCommande(), 0.01f);
        assertFalse("Pas d'ingénieur à bord au départ", fusee.isIngenieurABord());
    }

    @Test
    public void testModulesNonNull() {
        assertNotNull("Propulseur ne doit pas être null", fusee.getPropulseur());
        assertNotNull("ChargeUtile ne doit pas être null", fusee.getChargeUtile());
        assertNotNull("OrdiDeBord ne doit pas être null", fusee.getOrdiDeBord());
    }

    // ================================================================
    //  Tests des modules individuels
    // ================================================================

    @Test
    public void testModuleAssembly() {
        Propulseur p = fusee.getPropulseur();
        
        assertEquals("Module initialement à 0%", 0.0f, p.getPourcentageAssemblage(), 0.01f);
        assertFalse("Module pas terminé", p.estTermine());
        
        p.assembler(0.5f);
        assertEquals("Après assembler 0.5, à 50%", 0.5f, p.getPourcentageAssemblage(), 0.01f);
        assertFalse("Toujours pas terminé", p.estTermine());
        
        p.assembler(0.5f);
        assertEquals("Après assembler 0.5 de plus, à 100%", 1.0f, p.getPourcentageAssemblage(), 0.01f);
        assertTrue("Maintenant c'est terminé", p.estTermine());
    }

    @Test
    public void testModuleCaponnementMax() {
        Propulseur p = fusee.getPropulseur();
        
        p.assembler(1.5f);  // Plus que 100%
        assertEquals("Doit être plafonné à 1.0", 1.0f, p.getPourcentageAssemblage(), 0.01f);
        assertTrue("Doit être considéré comme terminé", p.estTermine());
    }

    @Test
    public void testRecettesExistent() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        assertNotNull("Propulseur doit avoir une recette", p.getRecette());
        assertNotNull("ChargeUtile doit avoir une recette", c.getRecette());
        assertNotNull("OrdiDeBord doit avoir une recette", o.getRecette());
    }

    // ================================================================
    //  Tests de probabilité de succès
    // ================================================================

    @Test
    public void testProbabiliteInitiale() {
        float prob = fusee.calculerProbabiliteSucces();
        assertEquals("Probabilité initiale = 0%", 0.0f, prob, 0.01f);
    }

    @Test
    public void testProbabiliteDansBornes() {
        float prob = fusee.calculerProbabiliteSucces();
        assertTrue("Probabilité >= 0", prob >= 0.0f);
        assertTrue("Probabilité <= 1", prob <= 1.0f);
    }

    @Test
    public void testProbabiliteMoyenne() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(0.3f);
        c.assembler(0.6f);
        o.assembler(0.9f);
        fusee.synchroniserEtats();
        
        float probExpectee = (0.3f + 0.6f + 0.9f) / 3.0f;
        assertEquals("Probabilité = moyenne des 3 modules", probExpectee, 
            fusee.calculerProbabiliteSucces(), 0.01f);
    }

    @Test
    public void testProbabiliteComplete() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(1.0f);
        c.assembler(1.0f);
        o.assembler(1.0f);
        fusee.synchroniserEtats();
        
        assertEquals("Probabilité = 100% si tous les modules sont terminés", 1.0f, 
            fusee.calculerProbabiliteSucces(), 0.01f);
    }

    @Test
    public void testProbabiliteNeModifiePasLaFusee() {
        Propulseur p = fusee.getPropulseur();
        p.assembler(0.5f);
        fusee.synchroniserEtats();
        
        float etatAvant = fusee.getEtatPropulseur();
        
        // Calculer la probabilité plusieurs fois
        fusee.calculerProbabiliteSucces();
        fusee.calculerProbabiliteSucces();
        fusee.calculerProbabiliteSucces();
        
        assertEquals("L'état du propulseur ne doit pas changer", etatAvant, 
            fusee.getEtatPropulseur(), 0.01f);
    }

    // ================================================================
    //  Tests de lancement
    // ================================================================

    @Test
    public void testLancementFuseeVide() {
        assertFalse("Fusée à 0% ne doit pas décoller", fusee.lancer());
    }

    @Test
    public void testLancementSansIngenieur() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(1.0f);
        c.assembler(1.0f);
        o.assembler(1.0f);
        fusee.synchroniserEtats();
        // Pas d'ingénieur à bord
        
        assertFalse("Sans ingénieur, le lancement doit échouer", fusee.lancer());
    }

    @Test
    public void testLancementReussi() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(1.0f);
        c.assembler(1.0f);
        o.assembler(1.0f);
        fusee.synchroniserEtats();
        fusee.setIngenieurABord(true);
        
        assertTrue("Avec tous les modules à 100% + ingénieur, doit décoller", fusee.lancer());
    }

    @Test
    public void testLancementStrict() throws LancementImpossibleException {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(1.0f);
        c.assembler(1.0f);
        o.assembler(1.0f);
        fusee.synchroniserEtats();
        fusee.setIngenieurABord(true);
        
        // Ne doit pas lever d'exception
        fusee.lancerStrict();
    }

    @Test(expected = LancementImpossibleException.class)
    public void testLancementStrictFuseeVide() throws LancementImpossibleException {
        fusee.lancerStrict();
    }

    @Test(expected = LancementImpossibleException.class)
    public void testLancementStrictSansIngenieur() throws LancementImpossibleException {
        Propulseur p = fusee.getPropulseur();
        p.assembler(1.0f);
        fusee.lancerStrict();
    }

    // ================================================================
    //  Tests de synchronisation
    // ================================================================

    @Test
    public void testSynchroniserEtats() {
        Propulseur p = fusee.getPropulseur();
        ChargeUtile c = fusee.getChargeUtile();
        OrdiDeBord o = fusee.getOrdiDeBord();
        
        p.assembler(0.4f);
        c.assembler(0.6f);
        o.assembler(0.8f);
        
        // Avant synchro, les floats sont à 0
        assertEquals("Propulseur float non synchronisé", 0.0f, fusee.getEtatPropulseur(), 0.01f);
        
        fusee.synchroniserEtats();
        
        assertEquals("Propulseur float synchronisé", 0.4f, fusee.getEtatPropulseur(), 0.01f);
        assertEquals("ChargeUtile float synchronisé", 0.6f, fusee.getEtatChargeUtile(), 0.01f);
        assertEquals("OrdiDeBord float synchronisé", 0.8f, fusee.getEtatCommande(), 0.01f);
    }

    // ================================================================
    //  Tests de condition de victoire
    // ================================================================

    @Test
    public void testTousModulesAssembles() {
        assertFalse("Initialement, tous les modules ne sont pas assemblés", 
            fusee.tousModulesAssembles());
        
        fusee.getPropulseur().assembler(1.0f);
        assertFalse("1 module sur 3 ne suffit pas", fusee.tousModulesAssembles());
        
        fusee.getChargeUtile().assembler(1.0f);
        assertFalse("2 modules sur 3 ne suffisent pas", fusee.tousModulesAssembles());
        
        fusee.getOrdiDeBord().assembler(1.0f);
        assertTrue("3 modules à 100% suffisent", fusee.tousModulesAssembles());
    }

    @Test
    public void testSetIngenieur() {
        assertFalse("Pas d'ingénieur initialement", fusee.isIngenieurABord());
        
        fusee.setIngenieurABord(true);
        assertTrue("Ingénieur embarqué", fusee.isIngenieurABord());
        
        fusee.setIngenieurABord(false);
        assertFalse("Ingénieur débarqué", fusee.isIngenieurABord());
    }

    // ================================================================
    //  Tests d'affichage
    // ================================================================

    @Test
    public void testToString() {
        String desc = fusee.toString();
        assertNotNull("toString() ne doit pas retourner null", desc);
        assertTrue("toString() doit contenir 'Fusee'", desc.toLowerCase().contains("fusee"));
    }
}
