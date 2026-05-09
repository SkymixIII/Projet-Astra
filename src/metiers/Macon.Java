package metiers;

import java.util.LinkedHashMap;
import java.util.Map;

import batiments.Batiment;
import batiments.TypeBatiment;
import entites.Ouvrier;
import entites.Role;
import ressources.Stock;
import ressources.TypeRessource;
import exceptions.RessourceInsuffisanteException;
import exceptions.StockException;

/**
 * Métier : MACON
 *
 * Produit des POUTRES en MENUISERIE.
 *   Recette  : 10 BOIS → 1 POUTRE
 *   Durée    : 60 ticks (÷ efficacité)
 *   Niveau minimum : DEBUTANT
 *
 * Fonctionnement par cycle :
 *   1. Si aucun cycle en cours ET ressources disponibles → démarre un cycle
 *      (consomme les 10 BOIS immédiatement, réserve la production).
 *   2. Accumule les ticks efficaces.
 *   3. Quand le cycle est terminé → ajoute 1 POUTRE au stock.
 */
public class Macon implements Metier {

    private static final int BOIS_PAR_POUTRE   = 10;
    private static final int TICKS_PRODUCTION  = 60;

    // Ticks efficaces accumulés sur le cycle en cours (-1 = aucun cycle)
    private double ticksCycle = -1;

    // -------------------------------------------------------------------------

    @Override
    public Role getType() {
        return Role.MACON;
    }

    @Override
    public String getNomAffichage() {
        return "Maçon";
    }

    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        return batiment.getType() == TypeBatiment.MENUISERIE;
    }

    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {

        if (!batiment.isOperationnel()) return;
        if (batiment.getType() != TypeBatiment.MENUISERIE) return;

        double efficacite = ouvrier.getEfficacite();

        // --- Démarrage d'un nouveau cycle si nécessaire ---
        if (ticksCycle < 0) {
            if (stock.contient(TypeRessource.BOIS, BOIS_PAR_POUTRE)) {
                try {
                    stock.retirer(TypeRessource.BOIS, BOIS_PAR_POUTRE);
                    ticksCycle = 0; // cycle lancé
                } catch (RessourceInsuffisanteException e) {
                    // Ne devrait pas arriver (on vient de vérifier), mais on sécurise
                    return;
                }
            } else {
                // Pas assez de bois : on attend
                ouvrier.travailler(ticks); // l'expérience progresse quand même (l'ouvrier est présent)
                return;
            }
        }

        // --- Avancement du cycle ---
        ticksCycle += ticks * efficacite;

        // --- Fin de cycle ---
        if (ticksCycle >= TICKS_PRODUCTION) {
            ticksCycle = -1; // réinitialise pour le prochain cycle
            try {
                stock.ajouter(TypeRessource.POUTRE, 1);
            } catch (StockException e) {
                System.out.println("[Maçon] Stock de POUTRE plein — poutre perdue.");
            }
        }

        ouvrier.travailler(ticks);
    }
}
