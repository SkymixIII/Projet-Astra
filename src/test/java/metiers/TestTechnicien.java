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

public class TestTechnicien {

    private Technicien technicien;
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
        technicien = new Technicien();
        ouvrier    = new Ouvrier("Lena", 0, 0);
        ouvrier.setMetier(technicien);
        stock      = new Stock(false);
    }

    // ================================================================== //
    //  1. IDENTITÉ DU MÉTIER                                              //
    // ================================================================== //

    @Test
    public void getType_retourneTECHNICIEN() {
        assertEquals(Role.TECHNICIEN, technicien.getType());
    }

    @Test
    public void peutTravaillerDans_fonderie_retourneTrue() {
        assertTrue(technicien.peutTravaillerDans(batimentStub(TypeBatiment.FONDERIE, true)));
    }

    @Test
    public void peutTravaillerDans_raffinerie_retourneTrue() {
        assertTrue(technicien.peutTravaillerDans(batimentStub(TypeBatiment.RAFFINERIE, true)));
    }

    @Test
    public void peutTravaillerDans_foret_retourneFalse() {
        assertFalse(technicien.peutTravaillerDans(batimentStub(TypeBatiment.FORET, true)));
    }

    // ================================================================== //
    //  2. FONDERIE — PLAQUE_ACIER (10 MINERAI_FER → 1 PLAQUE, 120 ticks) //
    // ================================================================== //

    @Test
    public void fonderie_sansRessources_rienProduit() throws Exception {
        Batiment fonderie = batimentStub(TypeBatiment.FONDERIE, true);
        technicien.travailler(ouvrier, fonderie, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.PLAQUE_ACIER));
    }

    @Test
    public void fonderie_cycleComplet_produitPlaqueAcier() throws Exception {
        stock.ajouter(TypeRessource.MINERAI_FER, 10);
        Batiment fonderie = batimentStub(TypeBatiment.FONDERIE, true);
        // 120 ticks / 0.7 efficacité ≈ 172 ticks réels
        technicien.travailler(ouvrier, fonderie, stock, 175);
        assertEquals("Un cycle fonderie complet doit livrer 1 PLAQUE_ACIER",
                1, stock.getQuantite(TypeRessource.PLAQUE_ACIER));
    }

    @Test
    public void fonderie_mineralConsommeAuDemarrage() throws Exception {
        stock.ajouter(TypeRessource.MINERAI_FER, 10);
        Batiment fonderie = batimentStub(TypeBatiment.FONDERIE, true);
        technicien.travailler(ouvrier, fonderie, stock, 5);
        assertEquals("10 MINERAI_FER doivent être consommés dès le démarrage",
                0, stock.getQuantite(TypeRessource.MINERAI_FER));
    }

    @Test
    public void fonderie_cycleIncomplet_pasDePlaque() throws Exception {
        stock.ajouter(TypeRessource.MINERAI_FER, 10);
        Batiment fonderie = batimentStub(TypeBatiment.FONDERIE, true);
        technicien.travailler(ouvrier, fonderie, stock, 10);
        assertEquals(0, stock.getQuantite(TypeRessource.PLAQUE_ACIER));
    }

    // ================================================================== //
    //  3. RAFFINERIE — KEROSENE (10 PETROLE → 1 KEROSENE, 180 ticks)     //
    // ================================================================== //

    @Test
    public void raffinerie_kerosene_cycleComplet() throws Exception {
        stock.ajouter(TypeRessource.PETROLE, 10);
        Batiment raffinerie = batimentStub(TypeBatiment.RAFFINERIE, true);
        // 180 / 0.7 ≈ 257 ticks réels
        technicien.travailler(ouvrier, raffinerie, stock, 260);
        assertEquals("Un cycle raffinerie doit produire 1 KEROSENE",
                1, stock.getQuantite(TypeRessource.KEROSENE));
    }

    @Test
    public void raffinerie_kerosene_petrolConsomme() throws Exception {
        stock.ajouter(TypeRessource.PETROLE, 10);
        Batiment raffinerie = batimentStub(TypeBatiment.RAFFINERIE, true);
        technicien.travailler(ouvrier, raffinerie, stock, 5);
        assertEquals("10 PETROLE doivent être consommés dès le démarrage",
                0, stock.getQuantite(TypeRessource.PETROLE));
    }

    // ================================================================== //
    //  4. RAFFINERIE — PLASTIQUE (5 PETROLE → 1 PLASTIQUE, 120 ticks)    //
    // ================================================================== //

    @Test
    public void raffinerie_plastique_seulement5petrole() throws Exception {
        stock.ajouter(TypeRessource.PETROLE, 5); // moins que pour kérosène
        Batiment raffinerie = batimentStub(TypeBatiment.RAFFINERIE, true);
        // 120 / 0.7 ≈ 172 ticks
        technicien.travailler(ouvrier, raffinerie, stock, 175);
        assertEquals("Avec 5 PETROLE, 1 PLASTIQUE doit être produit",
                1, stock.getQuantite(TypeRessource.PLASTIQUE));
    }

    // ================================================================== //
    //  5. PRIORITÉ EN RAFFINERIE                                          //
    // ================================================================== //

    @Test
    public void raffinerie_priorite_keroseneAvantPlastique() throws Exception {
        // Assez pour kérosène (10) ET plastique (5) → kérosène prioritaire
        stock.ajouter(TypeRessource.PETROLE, 15);
        Batiment raffinerie = batimentStub(TypeBatiment.RAFFINERIE, true);
        technicien.travailler(ouvrier, raffinerie, stock, 5); // démarre un cycle
        // Après démarrage, 10 pétrolont été consommés (kérosène, pas 5 plastique)
        assertEquals("Le kérosène est prioritaire : 10 pétrolont être consommés",
                5, stock.getQuantite(TypeRessource.PETROLE));
    }

    // ================================================================== //
    //  6. BATIMENT INOPERANT                                              //
    // ================================================================== //

    @Test
    public void travailler_batimentInoperant_rienProduit() throws Exception {
        stock.ajouter(TypeRessource.MINERAI_FER, 10);
        Batiment inoperant = batimentStub(TypeBatiment.FONDERIE, false);
        technicien.travailler(ouvrier, inoperant, stock, 1000);
        assertEquals(0, stock.getQuantite(TypeRessource.PLAQUE_ACIER));
    }

    // ================================================================== //
    //  7. EXPÉRIENCE                                                      //
    // ================================================================== //

    @Test
    public void travailler_faisProgresserExperience() throws Exception {
        stock.ajouter(TypeRessource.MINERAI_FER, 10);
        Batiment fonderie = batimentStub(TypeBatiment.FONDERIE, true);
        technicien.travailler(ouvrier, fonderie, stock, 600);
        assertEquals("L'ouvrier doit progresser après 600 ticks",
                Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
    }
}
