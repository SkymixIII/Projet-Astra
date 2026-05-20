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

public class TestMacon {

    private Macon macon;
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
        macon   = new Macon();
        ouvrier = new Ouvrier("Pierre", 0, 0);
        ouvrier.setMetier(macon);
        stock   = new Stock(false);
    }

    // ================================================================== //
    //  1. IDENTITÉ DU MÉTIER                                              //
    // ================================================================== //

    @Test
    public void getType_retourneMASON() {
        assertEquals(Role.MACON, macon.getType());
    }

    @Test
    public void peutTravaillerDans_menuiserie_retourneTrue() {
        assertTrue(macon.peutTravaillerDans(batimentStub(TypeBatiment.MENUISERIE, true)));
    }

    @Test
    public void peutTravaillerDans_fonderie_retourneFalse() {
        assertFalse(macon.peutTravaillerDans(batimentStub(TypeBatiment.FONDERIE, true)));
    }

    // ================================================================== //
    //  2. CYCLE DE PRODUCTION                                             //
    // ================================================================== //

    @Test
    public void travailler_sansRessources_rienProduit() throws Exception {
        Batiment menuiserie = batimentStub(TypeBatiment.MENUISERIE, true);
        // Pas de bois → cycle impossible
        macon.travailler(ouvrier, menuiserie, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.POUTRE));
    }

    @Test
    public void travailler_cycleComplet_produitUnePoutre() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 10); // recette = 10 BOIS → 1 POUTRE
        Batiment menuiserie = batimentStub(TypeBatiment.MENUISERIE, true);
        // TICKS_PRODUCTION = 60, efficacité DEBUTANT/NORMAL = 0.7
        // ticks nécessaires ≈ 60 / 0.7 ≈ 86 ticks réels
        macon.travailler(ouvrier, menuiserie, stock, 90);
        assertEquals("Un cycle complet doit produire 1 poutre",
                1, stock.getQuantite(TypeRessource.POUTRE));
    }

    @Test
    public void travailler_boisConsommeAuDemarrage() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 10);
        Batiment menuiserie = batimentStub(TypeBatiment.MENUISERIE, true);
        // On démarre le cycle mais pas encore terminé
        macon.travailler(ouvrier, menuiserie, stock, 5);
        assertEquals("Les 10 bois doivent être consommés dès le démarrage du cycle",
                0, stock.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void travailler_cycleIncomplet_pasDePoutre() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 10);
        Batiment menuiserie = batimentStub(TypeBatiment.MENUISERIE, true);
        // Seulement 10 ticks → 10 × 0.7 = 7 efficaces < 60
        macon.travailler(ouvrier, menuiserie, stock, 10);
        assertEquals(0, stock.getQuantite(TypeRessource.POUTRE));
    }

    @Test
    public void travailler_deuxCycles_deuxPoutres() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 20);
        Batiment menuiserie = batimentStub(TypeBatiment.MENUISERIE, true);
        // 90 ticks suffit pour 1 cycle; on fait 2×90 pour 2 cycles
        macon.travailler(ouvrier, menuiserie, stock, 90); // cycle 1
        macon.travailler(ouvrier, menuiserie, stock, 90); // cycle 2
        assertEquals(2, stock.getQuantite(TypeRessource.POUTRE));
    }

    @Test
    public void travailler_batimentInoperant_rienProduit() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 10);
        Batiment inoperant = batimentStub(TypeBatiment.MENUISERIE, false);
        macon.travailler(ouvrier, inoperant, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.POUTRE));
    }

    @Test
    public void travailler_mauvaisBatiment_rienProduit() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 10);
        Batiment foret = batimentStub(TypeBatiment.FORET, true);
        macon.travailler(ouvrier, foret, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.POUTRE));
    }

    @Test
    public void travailler_faisProgresserExperience() throws Exception {
        stock.ajouter(TypeRessource.BOIS, 10);
        Batiment menuiserie = batimentStub(TypeBatiment.MENUISERIE, true);
        macon.travailler(ouvrier, menuiserie, stock, 600);
        assertEquals("L'ouvrier doit progresser après 600 ticks",
                Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
    }
}
