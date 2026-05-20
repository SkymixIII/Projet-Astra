package metiers;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.Batiment;
import batiments.TypeBatiment;
import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import entites.Role;
import ressources.Stock;
import ressources.TypeRessource;

public class TestIngenieur {

    private Ingenieur ingenieur;
    private Ouvrier ouvrier;
    private Stock stock;

    private static Batiment batimentStub(TypeBatiment type, boolean operationnel) {
        return new Batiment() {
            @Override public TypeBatiment getType()                    { return type; }
            @Override public boolean isOperationnel()                  { return operationnel; }
            @Override public void affecterPersonnel(Ouvrier o)         {}
            @Override public void retirerPersonnel(Ouvrier o)          {}
            @Override public boolean aDeLaPlace()                      { return true; }
            @Override public void mettreAJour(Stock s, int t)          {}
            @Override public int getX()                                { return 0; }
            @Override public int getY()                                { return 0; }
            @Override public void deplacer(int x, int y)               {}
            @Override public void deplacer(Direction d)                {}
            @Override public double distance(Item autre)               { return 0; }
        };
    }

    @Before
    public void setUp() throws Exception {
        ingenieur = new Ingenieur();
        ouvrier   = new Ouvrier("Curie", 0, 0);
        ouvrier.setMetier(ingenieur);
        stock     = new Stock(false);
    }

    // ================================================================== //
    //  1. IDENTITÉ DU MÉTIER                                              //
    // ================================================================== //

    @Test
    public void getType_retourneINGENIEUR() {
        assertEquals(Role.INGENIEUR, ingenieur.getType());
    }

    @Test
    public void peutTravaillerDans_usineElectronique_retourneTrue() {
        assertTrue(ingenieur.peutTravaillerDans(
                batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true)));
    }

    @Test
    public void peutTravaillerDans_fonderie_retourneFalse() {
        assertFalse(ingenieur.peutTravaillerDans(
                batimentStub(TypeBatiment.FONDERIE, true)));
    }

    @Test
    public void peutTravaillerDans_foret_retourneFalse() {
        assertFalse(ingenieur.peutTravaillerDans(
                batimentStub(TypeBatiment.FORET, true)));
    }

    // ================================================================== //
    //  2. PRODUCTION DE PROCESSEUR_VOL (2 CARTE_MERE + 1 ALLIAGE, 600t) //
    // ================================================================== //

    @Test
    public void travailler_sansRessources_rienProduit() throws Exception {
        Batiment usine = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true);
        ingenieur.travailler(ouvrier, usine, stock, 10_000);
        assertEquals(0, stock.getQuantite(TypeRessource.PROCESSEUR_VOL));
    }

    @Test
    public void travailler_seulementCartes_rienProduit() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        Batiment usine = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true);
        ingenieur.travailler(ouvrier, usine, stock, 10_000);
        assertEquals("Sans ALLIAGE_THERMIQUE, pas de PROCESSEUR_VOL",
                0, stock.getQuantite(TypeRessource.PROCESSEUR_VOL));
    }

    @Test
    public void travailler_ressourcesCompletes_consommeIngredients() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 1);
        Batiment usine = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true);
        // Juste le démarrage du cycle
        ingenieur.travailler(ouvrier, usine, stock, 5);
        assertEquals("2 CARTE_MERE doivent être consommées au démarrage",
                0, stock.getQuantite(TypeRessource.CARTE_MERE));
        assertEquals("1 ALLIAGE doit être consommé au démarrage",
                0, stock.getQuantite(TypeRessource.ALLIAGE_THERMIQUE));
    }

    @Test
    public void travailler_cycleComplet_produitProcesseur() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 1);
        Batiment usine = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true);
        // 600 ticks / 0.7 efficacité ≈ 857 ticks réels
        ingenieur.travailler(ouvrier, usine, stock, 870);
        assertEquals("Un cycle complet doit livrer 1 PROCESSEUR_VOL",
                1, stock.getQuantite(TypeRessource.PROCESSEUR_VOL));
    }

    @Test
    public void travailler_cycleIncomplet_pasDeProcesseur() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 1);
        Batiment usine = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true);
        // Seulement 100 ticks → 100 × 0.7 = 70 efficaces < 600
        ingenieur.travailler(ouvrier, usine, stock, 100);
        assertEquals(0, stock.getQuantite(TypeRessource.PROCESSEUR_VOL));
    }

    @Test
    public void travailler_batimentInoperant_rienProduit() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 1);
        Batiment inoperant = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, false);
        ingenieur.travailler(ouvrier, inoperant, stock, 10_000);
        assertEquals(0, stock.getQuantite(TypeRessource.PROCESSEUR_VOL));
    }

    @Test
    public void travailler_mauvaisBatiment_rienProduit() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 1);
        Batiment usineElec = batimentStub(TypeBatiment.FONDERIE, true);
        ingenieur.travailler(ouvrier, usineElec, stock, 10_000);
        assertEquals(0, stock.getQuantite(TypeRessource.PROCESSEUR_VOL));
    }

    // ================================================================== //
    //  3. EXPÉRIENCE                                                      //
    // ================================================================== //

    @Test
    public void travailler_faisProgresserExperience() throws Exception {
        stock.ajouter(TypeRessource.CARTE_MERE, 2);
        stock.ajouter(TypeRessource.ALLIAGE_THERMIQUE, 1);
        Batiment usine = batimentStub(TypeBatiment.USINE_ELECTRONIQUE, true);
        ingenieur.travailler(ouvrier, usine, stock, 600);
        assertEquals("L'ouvrier doit progresser après 600 ticks",
                Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
    }
}
