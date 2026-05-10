package ressources;

import org.junit.Before;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.*;

import ressources.*;
import exceptions.*;

public class StockTest {

    // ------------------------------------------------------------------ //
    //  Fixtures                                                           //
    // ------------------------------------------------------------------ //

    private Stock stockFacile;
    private Stock stockDifficile;

    @Before
    public void setUp() {
        stockFacile    = new Stock(false);
        stockDifficile = new Stock(true);
    }


    // ================================================================== //
    //  1. INITIALISATION                                                  //
    // ================================================================== //

    @Test
    public void init_toutesRessourcesAZero() {
        for (TypeRessource type : TypeRessource.values()) {
            assertEquals("La ressource " + type + " devrait valoir 0",
                0, stockFacile.getQuantite(type));
        }
    }

    @Test
    public void init_totalActuelEstZero() {
        assertEquals(0, stockFacile.totalActuel());
        assertEquals(0, stockDifficile.totalActuel());
    }


    // ================================================================== //
    //  2. AJOUTER                                                         //
    // ================================================================== //

    @Test
    public void ajouter_quantiteAugmente() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 10);
        assertEquals(10, stockFacile.getQuantite(TypeRessource.FER));
    }

    @Test
    public void ajouter_deuxAppelsCumulatifs() throws Exception {
        stockFacile.ajouter(TypeRessource.BOIS, 50);
        stockFacile.ajouter(TypeRessource.BOIS, 30);
        assertEquals(80, stockFacile.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void ajouter_nAffectePasAutresRessources() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 100);
        assertEquals(0, stockFacile.getQuantite(TypeRessource.ACIER));
        assertEquals(0, stockFacile.getQuantite(TypeRessource.EAU));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ajouter_quantiteNegative_leveException() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ajouter_quantiteZero_leveException() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 0);
    }


    // ================================================================== //
    //  3. RETIRER                                                         //
    // ================================================================== //

    @Test
    public void retirer_quantiteDiminue() throws Exception {
        stockFacile.ajouter(TypeRessource.ACIER, 20);
        stockFacile.retirer(TypeRessource.ACIER, 8);
        assertEquals(12, stockFacile.getQuantite(TypeRessource.ACIER));
    }

    @Test
    public void retirer_totalite_metAZero() throws Exception {
        stockFacile.ajouter(TypeRessource.PIERRE, 15);
        stockFacile.retirer(TypeRessource.PIERRE, 15);
        assertEquals(0, stockFacile.getQuantite(TypeRessource.PIERRE));
    }

    @Test(expected = RessourceInsuffisanteException.class)
    public void retirer_stockInsuffisant_leveException() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 5);
        stockFacile.retirer(TypeRessource.FER, 10);
    }

    @Test(expected = RessourceInsuffisanteException.class)
    public void retirer_stockVide_leveException() throws Exception {
        stockFacile.retirer(TypeRessource.KEROSENE, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retirer_quantiteNegative_leveException() throws Exception {
        stockFacile.retirer(TypeRessource.FER, -3);
    }

    @Test
    public void retirer_pasDeModificationSiException() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 5);
        try {
            stockFacile.retirer(TypeRessource.FER, 10);
        } catch (RessourceInsuffisanteException ignored) {}
        assertEquals(5, stockFacile.getQuantite(TypeRessource.FER));
    }


    // ================================================================== //
    //  4. CONTIENT / CONTIENT TOUT                                        //
    // ================================================================== //

    @Test
    public void contient_quantiteSuffisante_retourneTrue() throws Exception {
        stockFacile.ajouter(TypeRessource.SILICIUM, 10);
        assertTrue(stockFacile.contient(TypeRessource.SILICIUM, 10));
        assertTrue(stockFacile.contient(TypeRessource.SILICIUM, 5));
    }

    @Test
    public void contient_quantiteInsuffisante_retourneFalse() throws Exception {
        stockFacile.ajouter(TypeRessource.SILICIUM, 3);
        assertFalse(stockFacile.contient(TypeRessource.SILICIUM, 5));
    }

    @Test
    public void contient_stockVide_retourneFalse() {
        assertFalse(stockFacile.contient(TypeRessource.FER, 1));
    }

    @Test
    public void contientTout_recetteDisponible_retourneTrue() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 3);
        stockFacile.ajouter(TypeRessource.ENERGIE, 2);

        Map<TypeRessource, Integer> recette = new EnumMap<>(TypeRessource.class);
        recette.put(TypeRessource.FER, 3);
        recette.put(TypeRessource.ENERGIE, 2);

        assertTrue(stockFacile.contientTout(recette));
    }

    @Test
    public void contientTout_uneRessourceManque_retourneFalse() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 3);

        Map<TypeRessource, Integer> recette = new EnumMap<>(TypeRessource.class);
        recette.put(TypeRessource.FER, 3);
        recette.put(TypeRessource.ENERGIE, 2);

        assertFalse(stockFacile.contientTout(recette));
    }


    // ================================================================== //
    //  5. CONSOMMER RECETTE                                               //
    // ================================================================== //

    @Test
    public void consommerRecette_reussie_retiresCorrectement() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 3);
        stockFacile.ajouter(TypeRessource.ENERGIE, 2);

        Map<TypeRessource, Integer> recette = new EnumMap<>(TypeRessource.class);
        recette.put(TypeRessource.FER, 3);
        recette.put(TypeRessource.ENERGIE, 2);

        stockFacile.consommerRecette(recette);

        assertEquals(0, stockFacile.getQuantite(TypeRessource.FER));
        assertEquals(0, stockFacile.getQuantite(TypeRessource.ENERGIE));
    }

    @Test(expected = RessourceInsuffisanteException.class)
    public void consommerRecette_ressourcesInsuffisantes_leveException() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 1);

        Map<TypeRessource, Integer> recette = new EnumMap<>(TypeRessource.class);
        recette.put(TypeRessource.FER, 3);
        recette.put(TypeRessource.ENERGIE, 2);

        stockFacile.consommerRecette(recette);
    }

    @Test
    public void consommerRecette_atomique_aucunRetraitSiEchec() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 3);

        Map<TypeRessource, Integer> recette = new EnumMap<>(TypeRessource.class);
        recette.put(TypeRessource.FER, 3);
        recette.put(TypeRessource.ENERGIE, 2);

        try {
            stockFacile.consommerRecette(recette);
        } catch (RessourceInsuffisanteException ignored) {}
        assertEquals("Le FER ne doit pas avoir été retiré si la recette a échoué",
            3, stockFacile.getQuantite(TypeRessource.FER));
    }


    // ================================================================== //
    //  6. TOTAL ACTUEL AVEC POIDS                                         //
    // ================================================================== //

    @Test
    public void totalActuel_tientCompteDuPoids() throws Exception {
        stockDifficile.ajouter(TypeRessource.FER, 10);
        stockDifficile.ajouter(TypeRessource.ACIER, 5);

        int attendu = 10 * TypeRessource.FER.getPoids()
                    +  5 * TypeRessource.ACIER.getPoids();
        assertEquals(attendu, stockDifficile.totalActuel());
    }

    @Test
    public void totalActuel_energiePoids0_nAugmentePas() throws Exception {
        stockDifficile.ajouter(TypeRessource.ENERGIE, 1000);
        assertEquals("L'énergie (poids 0) ne doit pas occuper de volume",
            0, stockDifficile.totalActuel());
    }

    @Test
    public void totalActuel_coherent_fer() throws Exception {
        stockDifficile.ajouter(TypeRessource.FER, 10);
        assertEquals(10 * TypeRessource.FER.getPoids(), stockDifficile.totalActuel());
    }

    @Test
    public void totalActuel_coherent_acier() throws Exception {
        stockDifficile.ajouter(TypeRessource.ACIER, 5);
        assertEquals(5 * TypeRessource.ACIER.getPoids(), stockDifficile.totalActuel());
    }

    @Test
    public void totalActuel_coherent_kerosene() throws Exception {
        stockDifficile.ajouter(TypeRessource.KEROSENE, 3);
        assertEquals(3 * TypeRessource.KEROSENE.getPoids(), stockDifficile.totalActuel());
    }


    // ================================================================== //
    //  7. MODE DIFFICILE — capacité globale                               //
    // ================================================================== //

    @Test(expected = StockException.class)
    public void modeDifficile_depasserCapaciteGlobale_leveException() throws Exception {
        stockDifficile.ajouter(TypeRessource.FER, 99_990);
        stockDifficile.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 2);
    }

    @Test
    public void modeDifficile_exactementCapaciteMax_autorise() throws Exception {
        stockDifficile.ajouter(TypeRessource.FER, 100_000);
    }

    @Test
    public void modeDifficile_apresRetrait_peutAjouterANouveau() throws Exception {
        stockDifficile.ajouter(TypeRessource.FER, 100_000);
        stockDifficile.retirer(TypeRessource.FER, 100);
        stockDifficile.ajouter(TypeRessource.FER, 100);
    }


    // ================================================================== //
    //  8. MODE FACILE — capacité par type                                 //
    // ================================================================== //

    @Test(expected = StockException.class)
    public void modeFacile_depasserLimiteParType_leveException() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 999);
        stockFacile.ajouter(TypeRessource.FER, 2);
    }

    @Test
    public void modeFacile_autreTypeNonBloque() throws Exception {
        stockFacile.ajouter(TypeRessource.FER, 1_000);
        stockFacile.ajouter(TypeRessource.ACIER, 250);
    }
}
