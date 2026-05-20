package metiers;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.Batiment;
import batiments.LieuDeRessource;
import batiments.TypeBatiment;
import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import entites.Role;
import exceptions.StockException;
import ressources.Stock;
import ressources.TypeRessource;

public class TestBucheron {

    private Bucheron bucheron;
    private Ouvrier ouvrier;
    private Stock stock;

    /** Bâtiment stub simple. */
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
        bucheron = new Bucheron();
        ouvrier  = new Ouvrier("Gaston", 0, 0);
        ouvrier.setMetier(bucheron);
        stock = new Stock(false);
    }

    // ================================================================== //
    //  1. IDENTITÉ DU MÉTIER                                              //
    // ================================================================== //

    @Test
    public void getType_retourneBUCHERON() {
        assertEquals(Role.BUCHERON, bucheron.getType());
    }

    @Test
    public void getNomAffichage_nonVide() {
        assertNotNull(bucheron.getNomAffichage());
        assertFalse(bucheron.getNomAffichage().isEmpty());
    }

    // ================================================================== //
    //  2. peutTravaillerDans                                              //
    // ================================================================== //

    @Test
    public void peutTravaillerDans_foret_retourneTrue() {
        assertTrue(bucheron.peutTravaillerDans(batimentStub(TypeBatiment.FORET, true)));
    }

    @Test
    public void peutTravaillerDans_mine_retourneFalse() {
        assertFalse(bucheron.peutTravaillerDans(batimentStub(TypeBatiment.MINE, true)));
    }

    @Test
    public void peutTravaillerDans_usine_retourneFalse() {
        assertFalse(bucheron.peutTravaillerDans(batimentStub(TypeBatiment.USINE, true)));
    }

    // ================================================================== //
    //  3. PRODUCTION DE BOIS                                              //
    // ================================================================== //

    @Test
    public void travailler_cadence5ticks_produitBois() throws Exception {
        Batiment foret = batimentStub(TypeBatiment.FORET, true);
        // CADENCE = 5 ticks ; efficacité DEBUTANT/NORMAL = 0.7 → 5/0.7 ≈ 7.14 ticks réels
        // On envoie 8 ticks : 8 × 0.7 = 5.6 ticks efficaces → 1 bois produit
        bucheron.travailler(ouvrier, foret, stock, 8);
        assertEquals("Un bois doit être produit après ~8 ticks réels",
                1, stock.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void travailler_pasAssezDeTicks_rienProduit() throws Exception {
        Batiment foret = batimentStub(TypeBatiment.FORET, true);
        // 3 ticks réels × 0.7 = 2.1 efficaces < 5 → rien
        bucheron.travailler(ouvrier, foret, stock, 3);
        assertEquals(0, stock.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void travailler_batimentInoperant_rienProduit() throws Exception {
        Batiment foretBrule = batimentStub(TypeBatiment.FORET, false);
        bucheron.travailler(ouvrier, foretBrule, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void travailler_mauvaisBatiment_rienProduit() throws Exception {
        Batiment mine = batimentStub(TypeBatiment.MINE, true);
        bucheron.travailler(ouvrier, mine, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void travailler_ticksAccumules_plusieursBois() throws Exception {
        Batiment foret = batimentStub(TypeBatiment.FORET, true);
        // 100 ticks × 0.7 = 70 efficaces → 70/5 = 14 bois
        bucheron.travailler(ouvrier, foret, stock, 100);
        assertEquals("100 ticks à efficacité 0.7 doivent produire 14 bois",
                14, stock.getQuantite(TypeRessource.BOIS));
    }

    @Test
    public void travailler_faisProgresserExperience() throws Exception {
        Batiment foret = batimentStub(TypeBatiment.FORET, true);
        bucheron.travailler(ouvrier, foret, stock, 600);
        assertEquals("L'ouvrier doit progresser au-delà de DEBUTANT après 600 ticks",
                Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
    }
}
