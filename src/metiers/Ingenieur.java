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
 * Métier : INGENIEUR
 *
 * Travaille dans l'USINE_ELECTRONIQUE.
 * Produit des PROCESSEUR_VOL :
 *   Recette  : 2 CARTE_MERE + 1 ALLIAGE_THERMIQUE → 1 PROCESSEUR_VOL
 *   Durée    : 600 ticks (÷ efficacité de l'ouvrier)
 *   Niveau requis : DEBUTANT minimum
 *
 * Note : l'Ingénieur est aussi membre de l'équipage de la fusée
 * (1 Ingénieur requis pour le lancement), mais cette responsabilité
 * est gérée par la classe Fusee, pas par ce métier.
 *
 * Fonctionnement par cycle (identique au Maçon et au Technicien) :
 *   1. Aucun cycle en cours + ressources disponibles → consomme les ingrédients,
 *      démarre un cycle.
 *   2. Accumule les ticks × efficacité.
 *   3. Cycle terminé → livre 1 PROCESSEUR_VOL au stock.
 */
public class Ingenieur implements Metier {

    // --- Recette ---
    private static final int CARTES_PAR_PROCESSEUR   = 2;
    private static final int ALLIAGES_PAR_PROCESSEUR = 1;
    private static final int TICKS_PRODUCTION        = 600;

    // --- Accumulateur de cycle (-1 = aucun cycle en cours) ---
    private double ticksCycle = -1;

    // -------------------------------------------------------------------------

    @Override
    public Role getType() {
        return Role.INGENIEUR;
    }

    @Override
    public String getNomAffichage() {
        return "Ingénieur";
    }

    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        return batiment.getType() == TypeBatiment.USINE_ELECTRONIQUE;
    }

    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {

        if (!batiment.isOperationnel()) return;
        if (batiment.getType() != TypeBatiment.USINE_ELECTRONIQUE) return;

        double efficacite = ouvrier.getEfficacite();

        // --- Démarrage d'un nouveau cycle si nécessaire ---
        if (ticksCycle < 0) {
            boolean aCartes   = stock.contient(TypeRessource.CARTE_MERE,       CARTES_PAR_PROCESSEUR);
            boolean aAlliages = stock.contient(TypeRessource.ALLIAGE_THERMIQUE, ALLIAGES_PAR_PROCESSEUR);

            if (aCartes && aAlliages) {
                try {
                    stock.retirer(TypeRessource.CARTE_MERE,        CARTES_PAR_PROCESSEUR);
                    stock.retirer(TypeRessource.ALLIAGE_THERMIQUE, ALLIAGES_PAR_PROCESSEUR);
                    ticksCycle = 0;
                } catch (RessourceInsuffisanteException e) {
                    // Ne devrait pas arriver (vérification faite juste avant)
                    ouvrier.travailler(ticks);
                    return;
                }
            } else {
                // Ingrédients manquants : l'ingénieur attend sur place
                ouvrier.travailler(ticks);
                return;
            }
        }

        // --- Avancement du cycle ---
        ticksCycle += ticks * efficacite;

        // --- Fin de cycle ---
        if (ticksCycle >= TICKS_PRODUCTION) {
            ticksCycle = -1;
            try {
                stock.ajouter(TypeRessource.PROCESSEUR_VOL, 1);
            } catch (StockException e) {
                System.out.println("[Ingénieur] Stock de PROCESSEUR_VOL plein — produit perdu.");
            }
        }

        ouvrier.travailler(ticks);
    }
}
