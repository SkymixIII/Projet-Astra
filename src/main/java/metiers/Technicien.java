package metiers;

import batiments.Batiment;
import batiments.TypeBatiment;
import entites.Ouvrier;
import entites.Role;
import ressources.Stock;
import ressources.TypeRessource;
import exceptions.RessourceInsuffisanteException;
import exceptions.StockException;

/**
 * Métier : TECHNICIEN
 *
 * FONDERIE    : 10 FER → 1 PLAQUE_ACIER          (120 ticks)
 * RAFFINERIE  :
 *   CABLAGE   : 1 PLASTIQUE + 2 SILICIUM → 1 CABLAGE (180 ticks, niveau CONFIRME min)
 *   KEROSENE  : 10 PETROLE → 1 KEROSENE           (180 ticks)
 *   PLASTIQUE : 5  PETROLE → 1 PLASTIQUE           (120 ticks)
 *
 * Note : la ressource de fer est FER (et non MINERAI_FER) car c'est ce que
 * LieuDeRessource et le stock initial injectent. MINERAI_FER reste dans
 * TypeRessource pour la V2 (minerai de surface vs mine profonde).
 */
public class Technicien implements Metier {

    // --- Fonderie ---
    private static final int FER_PAR_PLAQUE       = 10;
    private static final int TICKS_PLAQUE         = 120;

    // --- Raffinerie ---
    private static final int PETROLE_PAR_KEROSENE  = 10;
    private static final int TICKS_KEROSENE        = 180;

    private static final int PETROLE_PAR_PLASTIQUE = 5;
    private static final int TICKS_PLASTIQUE       = 120;

    private static final int PLASTIQUE_PAR_CABLAGE = 1;
    private static final int SILICIUM_PAR_CABLAGE  = 2;
    private static final int TICKS_CABLAGE         = 180;

    private enum Produit { AUCUN, PLAQUE_ACIER, KEROSENE, PLASTIQUE, CABLAGE }

    private Produit produitEnCours = Produit.AUCUN;
    private double  ticksCycle     = 0;

    @Override public Role   getType()          { return Role.TECHNICIEN; }
    @Override public String getNomAffichage()  { return "Technicien"; }

    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        TypeBatiment type = batiment.getType();
        return type == TypeBatiment.FONDERIE || type == TypeBatiment.RAFFINERIE;
    }

    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {
        if (!batiment.isOperationnel()) return;

        TypeBatiment lieu      = batiment.getType();
        double       efficacite = ouvrier.getEfficacite();

        // Démarrage d'un nouveau cycle
        if (produitEnCours == Produit.AUCUN) {
            produitEnCours = choisirProchain(lieu, ouvrier, stock);
            if (produitEnCours == Produit.AUCUN) {
                ouvrier.travailler(ticks);
                return;
            }
            if (!consommerIngredients(produitEnCours, stock)) {
                produitEnCours = Produit.AUCUN;
                ouvrier.travailler(ticks);
                return;
            }
            ticksCycle = 0;
        }

        // Avancement
        ticksCycle += ticks * efficacite;

        if (ticksCycle >= dureeProduction(produitEnCours)) {
            livrerProduit(produitEnCours, stock);
            produitEnCours = Produit.AUCUN;
            ticksCycle     = 0;
        }

        ouvrier.travailler(ticks);
    }

    private Produit choisirProchain(TypeBatiment lieu, Ouvrier ouvrier, Stock stock) {
        switch (lieu) {
            case FONDERIE:
                // Utilise FER (cohérence avec LieuDeRessource "Fer" et stock initial)
                if (stock.contient(TypeRessource.FER, FER_PAR_PLAQUE)) return Produit.PLAQUE_ACIER;
                break;
            case RAFFINERIE:
                boolean peutCabler = ouvrier.getNiveau().ordinal()
                                     >= Ouvrier.NiveauExp.CONFIRME.ordinal();
                if (peutCabler
                        && stock.contient(TypeRessource.PLASTIQUE, PLASTIQUE_PAR_CABLAGE)
                        && stock.contient(TypeRessource.SILICIUM,  SILICIUM_PAR_CABLAGE))
                    return Produit.CABLAGE;
                if (stock.contient(TypeRessource.PETROLE, PETROLE_PAR_KEROSENE))
                    return Produit.KEROSENE;
                if (stock.contient(TypeRessource.PETROLE, PETROLE_PAR_PLASTIQUE))
                    return Produit.PLASTIQUE;
                break;
            default: break;
        }
        return Produit.AUCUN;
    }

    private boolean consommerIngredients(Produit produit, Stock stock) {
        try {
            switch (produit) {
                case PLAQUE_ACIER: stock.retirer(TypeRessource.FER,      FER_PAR_PLAQUE);       break;
                case KEROSENE:     stock.retirer(TypeRessource.PETROLE,  PETROLE_PAR_KEROSENE); break;
                case PLASTIQUE:    stock.retirer(TypeRessource.PETROLE,  PETROLE_PAR_PLASTIQUE);break;
                case CABLAGE:
                    stock.retirer(TypeRessource.PLASTIQUE, PLASTIQUE_PAR_CABLAGE);
                    stock.retirer(TypeRessource.SILICIUM,  SILICIUM_PAR_CABLAGE);
                    break;
                default: break;
            }
            return true;
        } catch (RessourceInsuffisanteException e) {
            System.out.println("[Technicien] Erreur consommation : " + e.getMessage());
            return false;
        }
    }

    private void livrerProduit(Produit produit, Stock stock) {
        try {
            switch (produit) {
                case PLAQUE_ACIER: stock.ajouter(TypeRessource.PLAQUE_ACIER, 1); break;
                case KEROSENE:     stock.ajouter(TypeRessource.KEROSENE,     1); break;
                case PLASTIQUE:    stock.ajouter(TypeRessource.PLASTIQUE,    1); break;
                case CABLAGE:      stock.ajouter(TypeRessource.CABLAGE,      1); break;
                default: break;
            }
        } catch (StockException e) {
            System.out.println("[Technicien] Stock plein pour " + produit + " — produit perdu.");
        }
    }

    private int dureeProduction(Produit produit) {
        switch (produit) {
            case PLAQUE_ACIER: return TICKS_PLAQUE;
            case KEROSENE:     return TICKS_KEROSENE;
            case PLASTIQUE:    return TICKS_PLASTIQUE;
            case CABLAGE:      return TICKS_CABLAGE;
            default:           return Integer.MAX_VALUE;
        }
    }
}
