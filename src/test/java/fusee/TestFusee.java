package fusee;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import fusee.*;
import exceptions.LancementImpossibleException;

/**
 * Tests unitaires de la classe {@link Fusee}.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class TestFusee {

    private Fusee fusee;

    @Before
    public void setUp() {
        fusee = new Fusee();
    }

    //  1. INITIALISATION                                                //
    @Test
    public void init_etatsAZero() {
        assertEquals("Propulseur doit demarrer a 0%", 0.0f, fusee.getEtatPropulseur(), 0.001f);
        assertEquals("ChargeUtile doit demarrer a 0%", 0.0f, fusee.getEtatChargeUtile(), 0.001f);
        assertEquals("OrdiDeBord doit demarrer a 0%", 0.0f, fusee.getEtatCommande(), 0.001f);
    }

    @Test
    public void init_aucunIngenieurABord() {
        assertFalse("Aucun ingenieur ne doit etre a bord au depart",
                fusee.isIngenieurABord());
    }

    @Test
    public void init_modulesNonNull() {
        assertNotNull(fusee.getPropulseur());
        assertNotNull(fusee.getChargeUtile());
        assertNotNull(fusee.getOrdiDeBord());
    }

    @Test
    public void init_modulesNonTermines() {
        assertFalse(fusee.getPropulseur().estTermine());
        assertFalse(fusee.getChargeUtile().estTermine());
        assertFalse(fusee.getOrdiDeBord().estTermine());
        assertFalse("La fusee complete ne doit pas etre prete",
                fusee.tousModulesAssembles());
    }

    //  2. CALCUL DE LA PROBABILITÉ DE SUCCÈS                            //
    @Test
    public void probabilite_initiale_estZero() {
        assertEquals(0.0f, fusee.calculerProbabiliteSucces(), 0.001f);
    }

    @Test
    public void probabilite_tousModulesA100_estUn() {
        // On force les 3 modules a 100%
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        fusee.getOrdiDeBord().assembler(1.0f);
        fusee.synchroniserEtats();

        assertEquals("Avec 3 modules a 100%, la proba doit etre 1.0",
                1.0f, fusee.calculerProbabiliteSucces(), 0.001f);
    }

    @Test
    public void probabilite_dansBornes() {
        fusee.getPropulseur().assembler(0.5f);
        fusee.synchroniserEtats();
        float p = fusee.calculerProbabiliteSucces();
        assertTrue("La proba doit etre >= 0", p >= 0.0f);
        assertTrue("La proba doit etre <= 1", p <= 1.0f);
    }

    @Test
    public void probabilite_neModifiePasLaFusee() {
        // Postcondition contrat : la fusee n'est pas modifiee par calcul
        fusee.getPropulseur().assembler(0.5f);
        fusee.synchroniserEtats();
        float etatAvant = fusee.getEtatPropulseur();

        fusee.calculerProbabiliteSucces();
        fusee.calculerProbabiliteSucces();
        fusee.calculerProbabiliteSucces();

        assertEquals(etatAvant, fusee.getEtatPropulseur(), 0.001f);
    }

    //  3. LANCEMENT                                                     //
    @Test
    public void lancer_fuseeVide_echoue() {
        assertFalse("Une fusee a 0% ne doit pas decoller", fusee.lancer());
    }

    @Test
    public void lancer_modulesIncompletsMaisIngenieurPresent_echoue() {
        fusee.setIngenieurABord(true);
        fusee.getPropulseur().assembler(1.0f);
        fusee.synchroniserEtats();
        // Charge utile et Ordi a 0%
        assertFalse(fusee.lancer());
    }

    @Test
    public void lancer_tousModulesPretsMaisPasDIngenieur_echoue() {
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        fusee.getOrdiDeBord().assembler(1.0f);
        fusee.synchroniserEtats();
        // ingenieurABord reste false
        assertFalse("Sans ingenieur, le lancement doit echouer", fusee.lancer());
    }

    @Test
    public void lancer_toutesConditionsRemplies_succes() {
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        fusee.getOrdiDeBord().assembler(1.0f);
        fusee.synchroniserEtats();
        fusee.setIngenieurABord(true);

        assertTrue("Avec 3 modules a 100% + ingenieur, le decollage doit reussir",
                fusee.lancer());
    }

    //  4. LANCEMENT STRICT (avec exception)                             //
    @Test(expected = LancementImpossibleException.class)
    public void lancerStrict_fuseeVide_leveException() throws LancementImpossibleException {
        fusee.lancerStrict();
    }

    @Test(expected = LancementImpossibleException.class)
    public void lancerStrict_sansIngenieur_leveException() throws LancementImpossibleException {
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        fusee.getOrdiDeBord().assembler(1.0f);
        fusee.synchroniserEtats();
        // Pas d'ingenieur
        fusee.lancerStrict();
    }

    @Test
    public void lancerStrict_toutesConditionsOk_passe() throws LancementImpossibleException {
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        fusee.getOrdiDeBord().assembler(1.0f);
        fusee.synchroniserEtats();
        fusee.setIngenieurABord(true);

        // Ne doit pas lever d'exception
        fusee.lancerStrict();
    }

    //  5. SYNCHRONISATION DES ETATS                                     //
    @Test
    public void synchroniser_metAJourLesFloats() {
        fusee.getPropulseur().assembler(0.4f);
        fusee.getChargeUtile().assembler(0.6f);
        fusee.getOrdiDeBord().assembler(0.8f);

        // Avant synchro : les floats sont encore a 0
        assertEquals(0.0f, fusee.getEtatPropulseur(), 0.001f);

        fusee.synchroniserEtats();

        assertEquals(0.4f, fusee.getEtatPropulseur(), 0.001f);
        assertEquals(0.6f, fusee.getEtatChargeUtile(), 0.001f);
        assertEquals(0.8f, fusee.getEtatCommande(), 0.001f);
    }

    //  6. tousModulesAssembles                                          //
    @Test
    public void tousModulesAssembles_partiel_retourneFalse() {
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        // Ordi a 0%
        assertFalse(fusee.tousModulesAssembles());
    }

    @Test
    public void tousModulesAssembles_complet_retourneTrue() {
        fusee.getPropulseur().assembler(1.0f);
        fusee.getChargeUtile().assembler(1.0f);
        fusee.getOrdiDeBord().assembler(1.0f);
        assertTrue(fusee.tousModulesAssembles());
    }
}
