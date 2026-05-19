package metiers;

import batiments.Batiment;
import batiments.LieuDeRessource;
import batiments.TypeBatiment;
import entites.Ouvrier;
import entites.Role;
import ressources.Stock;
import ressources.TypeRessource;
import exceptions.StockException;

/**
 * Métier : MINEUR
 *
 * Extrait des ressources selon le TypeRessource du LieuDeRessource :
 *   FER      → MINE         (1 FER  / 8 ticks)
 *   PIERRE   → CARRIERE     (1 PIERRE / 5 ticks)
 *   PETROLE  → PUITS_PETROLE(1 PETROLE / 10 ticks, niveau CONFIRME min)
 *   SILICIUM → CARRIERE     (1 SILICIUM / 10 ticks, niveau EXPERT min)
 *
 * Correction V1 : peutTravaillerDans accepte MINE, CARRIERE et PUITS_PETROLE
 * (n'était auparavant limité qu'à LIEU_DE_RESSOURCE générique).
 * Correction V1 : produit TypeRessource.FER (au lieu de MINERAI_FER) pour
 * cohérence avec le Technicien et le stock initial.
 */
public class Mineur implements Metier {

    private static final int CADENCE_FER      = 8;
    private static final int CADENCE_PIERRE   = 5;
    private static final int CADENCE_SILICIUM = 10;
    private static final int CADENCE_PETROLE  = 10;

    private double ticksAccumules = 0;

    @Override public Role   getType()         { return Role.MINEUR; }
    @Override public String getNomAffichage() { return "Mineur"; }

    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        TypeBatiment type = batiment.getType();
        return type == TypeBatiment.MINE
            || type == TypeBatiment.CARRIERE
            || type == TypeBatiment.PUITS_PETROLE
            || type == TypeBatiment.LIEU_DE_RESSOURCE; // compatibilité avec la valeur générique
    }

    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {
        if (!batiment.isOperationnel()) return;

        TypeBatiment type      = batiment.getType();
        double       efficacite = ouvrier.getEfficacite();

        ticksAccumules += ticks * efficacite;

        // On détermine la ressource à extraire depuis le LieuDeRessource si possible
        TypeRessource ressource = resoudreRessource(batiment);

        switch (ressource != null ? typeVersCase(ressource) : type) {

            case MINE: // FER
                while (ticksAccumules >= CADENCE_FER) {
                    ticksAccumules -= CADENCE_FER;
                    extraire(stock, TypeRessource.FER, batiment);
                }
                break;

            case CARRIERE: // PIERRE ou SILICIUM selon le niveau
                if (ouvrier.getNiveau().ordinal() >= Ouvrier.NiveauExp.EXPERT.ordinal()
                        && estGisementDe(batiment, TypeRessource.SILICIUM)) {
                    while (ticksAccumules >= CADENCE_SILICIUM) {
                        ticksAccumules -= CADENCE_SILICIUM;
                        extraire(stock, TypeRessource.SILICIUM, batiment);
                    }
                } else if (estGisementDe(batiment, TypeRessource.PIERRE)
                        || !estGisementDe(batiment, TypeRessource.SILICIUM)) {
                    while (ticksAccumules >= CADENCE_PIERRE) {
                        ticksAccumules -= CADENCE_PIERRE;
                        extraire(stock, TypeRessource.PIERRE, batiment);
                    }
                }
                break;

            case PUITS_PETROLE: // PETROLE, niveau CONFIRME requis
                if (ouvrier.getNiveau().ordinal() >= Ouvrier.NiveauExp.CONFIRME.ordinal()) {
                    while (ticksAccumules >= CADENCE_PETROLE) {
                        ticksAccumules -= CADENCE_PETROLE;
                        extraire(stock, TypeRessource.PETROLE, batiment);
                    }
                }
                break;

            default:
                // LIEU_DE_RESSOURCE générique : on extrait directement la ressource du gisement
                if (ressource != null) {
                    int cadence = cadenceParRessource(ressource);
                    while (ticksAccumules >= cadence) {
                        ticksAccumules -= cadence;
                        extraire(stock, ressource, batiment);
                    }
                }
                break;
        }

        ouvrier.travailler(ticks);
    }

    // -------------------------------------------------------------------------
    // Utilitaires privés
    // -------------------------------------------------------------------------

    /** Extrait une unité depuis le gisement vers le stock global. */
    private void extraire(Stock stock, TypeRessource type, Batiment batiment) {
        if (batiment instanceof LieuDeRessource) {
            int quantite = ((LieuDeRessource) batiment).exploiter(1);
            if (quantite == 0) return; // gisement épuisé
        }
        try {
            stock.ajouter(type, 1);
        } catch (StockException e) {
            System.out.println("[Mineur] Stock plein pour " + type + " — ressource perdue.");
        }
    }

    /** Retourne la ressource du LieuDeRessource si le bâtiment est un gisement. */
    private TypeRessource resoudreRessource(Batiment batiment) {
        if (batiment instanceof LieuDeRessource)
            return ((LieuDeRessource) batiment).getRessource();
        return null;
    }

    /** Vérifie si le gisement produit bien la ressource attendue. */
    private boolean estGisementDe(Batiment batiment, TypeRessource type) {
        TypeRessource r = resoudreRessource(batiment);
        return r == type;
    }

    /**
     * Convertit une TypeRessource en TypeBatiment pour le switch principal.
     * Permet de traiter un LIEU_DE_RESSOURCE générique via sa ressource.
     */
    private TypeBatiment typeVersCase(TypeRessource ressource) {
        switch (ressource) {
            case FER:      return TypeBatiment.MINE;
            case PIERRE:   return TypeBatiment.CARRIERE;
            case SILICIUM: return TypeBatiment.CARRIERE;
            case PETROLE:  return TypeBatiment.PUITS_PETROLE;
            default:       return TypeBatiment.LIEU_DE_RESSOURCE;
        }
    }

    private int cadenceParRessource(TypeRessource ressource) {
        switch (ressource) {
            case FER:      return CADENCE_FER;
            case PIERRE:   return CADENCE_PIERRE;
            case SILICIUM: return CADENCE_SILICIUM;
            case PETROLE:  return CADENCE_PETROLE;
            default:       return 10;
        }
    }
}
