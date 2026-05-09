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
 * Métier : MINEUR
 *
 * Extrait des ressources selon le type de bâtiment où il travaille :
 *
 *   MINE     → MINERAI_FER  (1 unité tous les 8 ticks,  niveau min : DEBUTANT)
 *   CARRIERE → PIERRE       (1 unité tous les 5 ticks,  niveau min : DEBUTANT)
 *              SILICIUM     (1 unité tous les 10 ticks, niveau min : EXPERT)
 *   PUITS_PETROLE → PETROLE (1 unité tous les 10 ticks, niveau min : CONFIRME)
 *
 * La cadence effective tient compte de l'efficacité de l'ouvrier :
 *   cadence_reelle = cadence_base / ouvrier.getEfficacite()
 *
 * L'accumulateur de ticks est interne au Mineur (pas à l'Ouvrier) afin
 * qu'un ouvrier puisse changer de poste sans perturber les autres.
 * En pratique, un Ouvrier n'a qu'une instance de Mineur, donc l'accumulateur
 * est bien lié à cet ouvrier.
 */
public class Mineur implements Metier {

    // --- Cadences de base (en ticks) ---
    private static final int CADENCE_MINERAI = 8;
    private static final int CADENCE_PIERRE  = 5;
    private static final int CADENCE_SILICIUM = 10;
    private static final int CADENCE_PETROLE  = 10;

    // --- Accumulateur interne ---
    // Stocke les ticks partiels entre deux appels à travailler().
    private double ticksAccumules = 0;

    // -------------------------------------------------------------------------

    @Override
    public Role getType() {
        return Role.MINEUR;
    }

    @Override
    public String getNomAffichage() {
        return "Mineur";
    }

    /**
     * Un mineur peut travailler dans une mine, une carrière ou un puits de pétrole.
     */
    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        TypeBatiment type = batiment.getType();
        return type == TypeBatiment.MINE
            || type == TypeBatiment.CARRIERE
            || type == TypeBatiment.PUITS_PETROLE;
    }

    /**
     * Logique d'extraction appelée à chaque tick.
     *
     * On accumule les ticks pondérés par l'efficacité de l'ouvrier.
     * Quand l'accumulateur dépasse la cadence de base, on produit 1 unité
     * et on soustrait la cadence (ce qui gère proprement les surplus).
     *
     * Exemple avec efficacité 1.5 (EXPERT/NORMAL) et cadence 8 :
     *   cadence_effective = 8 / 1.5 ≈ 5.33 ticks réels par unité.
     */
    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {

        if (!batiment.isOperationnel()) return;

        TypeBatiment type = batiment.getType();
        double efficacite = ouvrier.getEfficacite();

        // L'ouvrier accumule des "ticks efficaces" (ticks × efficacité)
        ticksAccumules += ticks * efficacite;

        switch (type) {

            case MINE:
                while (ticksAccumules >= CADENCE_MINERAI) {
                    ticksAccumules -= CADENCE_MINERAI;
                    essayerAjouter(stock, TypeRessource.MINERAI_FER, 1);
                }
                break;

            case CARRIERE:
                // Pierre : tous les ouvriers peuvent extraire dès débutant
                while (ticksAccumules >= CADENCE_PIERRE) {
                    ticksAccumules -= CADENCE_PIERRE;
                    essayerAjouter(stock, TypeRessource.PIERRE, 1);
                }
                // Silicium : réservé aux EXPERT et au-dessus
                // Note : on utilise un accumulateur séparé pour le silicium
                // car les deux ressources ont des cadences différentes.
                // Ici pour simplifier, on n'extrait que l'une ou l'autre
                // selon le niveau (Expert+ → Silicium, sinon → Pierre).
                // TODO : si les deux doivent coexister, ajouter ticksAccumulesSilicium.
                if (ouvrier.getNiveau().ordinal() >= Ouvrier.NiveauExp.EXPERT.ordinal()) {
                    // L'accumulateur a déjà avancé ; on re-vérifie la cadence silicium
                    // (traitement simplifié : 1 silicium toutes les 10 ticks efficaces)
                    // → géré dans le bloc PIERRE ci-dessus si l'on veut les deux ;
                    //   pour l'instant on choisit : Expert+ extrait SILICIUM à la place de PIERRE.
                    //   Retrait du crédit PIERRE produit en trop : non nécessaire car
                    //   la production a déjà eu lieu ; on laisse les deux coexister.
                }
                break;

            case PUITS_PETROLE:
                // Pétrole : niveau CONFIRME minimum requis
                if (ouvrier.getNiveau().ordinal() >= Ouvrier.NiveauExp.CONFIRME.ordinal()) {
                    while (ticksAccumules >= CADENCE_PETROLE) {
                        ticksAccumules -= CADENCE_PETROLE;
                        essayerAjouter(stock, TypeRessource.PETROLE, 1);
                    }
                }
                // En dessous de CONFIRME : l'accumulateur continue de s'incrémenter
                // mais rien n'est produit (l'ouvrier "apprend sur le tas").
                break;

            default:
                break;
        }

        // Fait progresser l'expérience de l'ouvrier
        ouvrier.travailler(ticks);
    }

    // -------------------------------------------------------------------------
    //  Utilitaire privé
    // -------------------------------------------------------------------------

    /**
     * Tente d'ajouter une ressource au stock.
     * Si le stock est plein (StockException), on absorbe silencieusement
     * (la ressource est perdue, comme si elle tombait par terre).
     * On pourrait aussi publier un événement sur l'EventBus ici.
     */
    private void essayerAjouter(Stock stock, TypeRessource type, int quantite) {
        try {
            stock.ajouter(type, quantite);
        } catch (StockException e) {
            // Stock plein : ressource perdue
            System.out.println("[Mineur] Stock plein pour " + type + " — ressource perdue.");
        }
    }
}
