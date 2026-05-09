package ressources;

/**
 * Métier : TECHNICIEN
 *
 * Travaille dans plusieurs usines selon le produit souhaité :
 *
 *   FONDERIE
 *     • PLAQUE_ACIER  : 10 MINERAI_FER → 1 PLAQUE_ACIER    (120 ticks, niveau min : DEBUTANT)
 *
 *   RAFFINERIE
 *     • KEROSENE  : 10 PETROLE → 1 KEROSENE          (180 ticks, niveau min : DEBUTANT)
 *     • PLASTIQUE : 5  PETROLE → 1 PLASTIQUE          (120 ticks, niveau min : DEBUTANT)
 *     • CABLAGE   : 1  PLASTIQUE + 2 SILICIUM → 1 CABLAGE (180 ticks, niveau min : CONFIRME)
 *
 * Règle de priorité en raffinerie (un seul cycle à la fois par Technicien) :
 *   CABLAGE > KEROSENE > PLASTIQUE
 *   (le premier produit dont les ressources sont disponibles est lancé)
 *
 * Niveau requis pour le câblage : CONFIRME minimum.
 */
public class Technicien implements Metier {

    // --- Fonderie ---
    private static final int MINERAI_PAR_PLAQUE   = 10;
    private static final int TICKS_PLAQUE         = 120;

    // --- Raffinerie ---
    private static final int PETROLE_PAR_KEROSENE  = 10;
    private static final int TICKS_KEROSENE        = 180;

    private static final int PETROLE_PAR_PLASTIQUE = 5;
    private static final int TICKS_PLASTIQUE       = 120;

    private static final int PLASTIQUE_PAR_CABLAGE = 1;
    private static final int SILICIUM_PAR_CABLAGE  = 2;
    private static final int TICKS_CABLAGE         = 180;

    // --- Accumulateur de cycle ---
    // Enum interne pour tracer ce qu'on est en train de produire
    private enum Produit { AUCUN, PLAQUE_ACIER, KEROSENE, PLASTIQUE, CABLAGE }

    private Produit produitEnCours = Produit.AUCUN;
    private double  ticksCycle     = 0;

    // -------------------------------------------------------------------------

    @Override
    public Ouvrier.TypeMetier getType() {
        return Ouvrier.TypeMetier.TECHNICIEN;
    }

    @Override
    public String getNomAffichage() {
        return "Technicien";
    }

    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        TypeBatiment type = batiment.getType();
        return type == TypeBatiment.FONDERIE
            || type == TypeBatiment.RAFFINERIE;
    }

    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {

        if (!batiment.isOperationnel()) return;

        TypeBatiment lieu = batiment.getType();
        double efficacite = ouvrier.getEfficacite();

        // --- Démarrage d'un nouveau cycle si nécessaire ---
        if (produitEnCours == Produit.AUCUN) {
            produitEnCours = choisirProchain(lieu, ouvrier, stock);
            if (produitEnCours == Produit.AUCUN) {
                // Rien à produire (manque de ressources ou niveau insuffisant)
                ouvrier.travailler(ticks);
                return;
            }
            // Consomme les ingrédients au démarrage
            if (!consommerIngredients(produitEnCours, stock)) {
                produitEnCours = Produit.AUCUN;
                ouvrier.travailler(ticks);
                return;
            }
            ticksCycle = 0;
        }

        // --- Avancement du cycle ---
        ticksCycle += ticks * efficacite;

        int duree = dureeProduction(produitEnCours);
        if (ticksCycle >= duree) {
            livrerProduit(produitEnCours, stock);
            produitEnCours = Produit.AUCUN;
            ticksCycle     = 0;
        }

        ouvrier.travailler(ticks);
    }

    // -------------------------------------------------------------------------
    //  Méthodes privées
    // -------------------------------------------------------------------------

    /**
     * Choisit le prochain produit à lancer selon le lieu et les stocks.
     * Retourne Produit.AUCUN si rien n'est possible.
     */
    private Produit choisirProchain(TypeBatiment lieu, Ouvrier ouvrier, Stock stock) {
        switch (lieu) {

            case FONDERIE:
                if (stock.contient(TypeRessource.MINERAI_FER, MINERAI_PAR_PLAQUE)) {
                    return Produit.PLAQUE_ACIER;
                }
                break;

            case RAFFINERIE:
                // Priorité : câblage (niveau CONFIRME requis)
                boolean peutCabler = ouvrier.getNiveau().ordinal()
                                     >= Ouvrier.NiveauExp.CONFIRME.ordinal();
                if (peutCabler
                    && stock.contient(TypeRessource.PLASTIQUE, PLASTIQUE_PAR_CABLAGE)
                    && stock.contient(TypeRessource.SILICIUM,  SILICIUM_PAR_CABLAGE)) {
                    return Produit.CABLAGE;
                }
                if (stock.contient(TypeRessource.PETROLE, PETROLE_PAR_KEROSENE)) {
                    return Produit.KEROSENE;
                }
                if (stock.contient(TypeRessource.PETROLE, PETROLE_PAR_PLASTIQUE)) {
                    return Produit.PLASTIQUE;
                }
                break;

            default:
                break;
        }
        return Produit.AUCUN;
    }

    /**
     * Consomme les ingrédients nécessaires au démarrage du cycle.
     * Retourne false si un retrait échoue (ne devrait pas arriver si
     * choisirProchain() a vérifié, mais on sécurise).
     */
    private boolean consommerIngredients(Produit produit, Stock stock) {
        try {
            switch (produit) {
                case PLAQUE_ACIER:
                    stock.retirer(TypeRessource.MINERAI_FER, MINERAI_PAR_PLAQUE);
                    break;
                case KEROSENE:
                    stock.retirer(TypeRessource.PETROLE, PETROLE_PAR_KEROSENE);
                    break;
                case PLASTIQUE:
                    stock.retirer(TypeRessource.PETROLE, PETROLE_PAR_PLASTIQUE);
                    break;
                case CABLAGE:
                    stock.retirer(TypeRessource.PLASTIQUE, PLASTIQUE_PAR_CABLAGE);
                    stock.retirer(TypeRessource.SILICIUM,  SILICIUM_PAR_CABLAGE);
                    break;
                default:
                    break;
            }
            return true;
        } catch (RessourceInsuffisanteException e) {
            System.out.println("[Technicien] Erreur consommation ingrédients : " + e.getMessage());
            return false;
        }
    }

    /**
     * Ajoute le produit fini au stock.
     */
    private void livrerProduit(Produit produit, Stock stock) {
        try {
            switch (produit) {
                case PLAQUE_ACIER: stock.ajouter(TypeRessource.PLAQUE_ACIER, 1); break;
                case KEROSENE:     stock.ajouter(TypeRessource.KEROSENE,     1); break;
                case PLASTIQUE:    stock.ajouter(TypeRessource.PLASTIQUE,    1); break;
                case CABLAGE:      stock.ajouter(TypeRessource.CABLAGE,      1); break;
                default:           break;
            }
        } catch (StockException e) {
            System.out.println("[Technicien] Stock plein pour " + produit + " — produit perdu.");
        }
    }

    /**
     * Retourne la durée de base (en ticks) pour le produit donné.
     */
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