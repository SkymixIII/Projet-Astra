package ressources;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Gère l'inventaire du joueur sous forme de Map<TypeRessource, Integer>.
 *
 * Deux modes selon la difficulté :
 *  - FACILE  : limite par type de ressource
 *  - DIFFICILE : limite globale toutes ressources confondues
 */
public class Stock {

    // ------------------------------------------------------------------ //
    //  Constantes de capacité                                             //
    // ------------------------------------------------------------------ //

    /** @param CAP_PAR_TYPE_FACILE Capacité max par type en mode FACILE (peut être surchargée au run-time). */
    private static final int CAP_PAR_TYPE_FACILE = 1000;

    /** @param CAP_GLOBALE_DIFFICILE Capacité globale en mode DIFFICILE (toutes ressources confondues). */
    private static final int CAP_GLOBALE_DIFFICILE = 100_000;

    // ------------------------------------------------------------------ //
    //  Attributs                                                          //
    // ------------------------------------------------------------------ //

    /** Stockage principal. EnumMap est plus performant qu'un HashMap pour les enums. */
    private final Map<TypeRessource, Integer> stocks;

    /** true = mode difficile, false = mode facile */
    private final boolean modeDifficile;

    // ------------------------------------------------------------------ //
    //  Constructeurs                                                      //
    // ------------------------------------------------------------------ //

    public Stock(boolean modeDifficile) {
        this.modeDifficile = modeDifficile;
        this.stocks = new EnumMap<>(TypeRessource.class);
        // Initialise toutes les ressources à 0
        for (TypeRessource t : TypeRessource.values()) {
            stocks.put(t, 0);
        }
    }

    // ------------------------------------------------------------------ //
    //  Méthodes principales                                               //
    // ------------------------------------------------------------------ //

    /**
     * Ajoute une quantité d'une ressource au stock. 
     * ATTENTION on tient compte du poids du type concerné.
     *
     * @param type type de ressource
     * @param quantite quantité à ajouter (> 0)
     * @throws StockException si la capacité serait dépassée
     * @throws IllegalArgumentException si quantite <= 0
     */
    public void ajouter(TypeRessource type, int quantite) throws StockException {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité à ajouter doit être positive.");
        }

        int actuel = stocks.get(type);

        if (modeDifficile) {
            if (totalActuel() + quantite * type.getPoids() > CAP_GLOBALE_DIFFICILE) {
                throw new StockException(
                    "Stock global plein ! Impossible d'ajouter " + quantite
                    + " de " + type + ". Capacité globale : " + CAP_GLOBALE_DIFFICILE
                );
            }
        } else {
            if ((actuel + quantite) * type.getPoids() > CAP_PAR_TYPE_FACILE) {
                throw new StockException(
                    "Stock de " + type + " plein ! Limite : " + CAP_PAR_TYPE_FACILE
                );
            }
        }

        stocks.put(type, actuel + quantite);  
    }

    /**
     * Retire une quantité d'une ressource du stock.
     *
     * @param type     type de ressource
     * @param quantite quantité à retirer (> 0)
     * @throws RessourceInsuffisanteException si stock insuffisant
     */
    public void retirer(TypeRessource type, int quantite) throws RessourceInsuffisanteException {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité à retirer doit être positive.");
        }

        int actuel = stocks.get(type);
        if (actuel < quantite) {
            throw new RessourceInsuffisanteException(
                "Stock insuffisant en " + type
                + " : demandé=" + quantite + ", disponible=" + actuel
            );
        }

        stocks.put(type, actuel - quantite);
    }

    /**
     * Vérifie si le stock contient au moins la quantité demandée.
     * Utile avant de lancer une production (Usine) sans lever d'exception.
     */
    public boolean contient(TypeRessource type, int quantite) {
        // 0 est renvoyé si pas de valeur associée
        return stocks.getOrDefault(type, 0) >= quantite;
    }

    /**
     * Vérifie si TOUTES les ressources d'une recette sont disponibles.
     * Pratique pour valider une construction ou une production en une seule vérification.
     *
     * @param recette Map ressource -> quantité nécessaire
     */
    public boolean contientTout(Map<TypeRessource, Integer> recette) {
        for (Map.Entry<TypeRessource, Integer> entree : recette.entrySet()) {
            if (!contient(entree.getKey(), entree.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Consomme toutes les ressources d'une recette en une seule transaction.
     * Lance RessourceInsuffisanteException si l'une d'elles manque (atomicité).
     */
    public void consommerRecette(Map<TypeRessource, Integer> recette)
            throws RessourceInsuffisanteException {
        // Vérification d'abord (tout ou rien)
        if (!contientTout(recette)) {
            throw new RessourceInsuffisanteException(
                "Ressources insuffisantes pour cette recette : " + recette
            );
        }
        // Retrait effectif
        for (Map.Entry<TypeRessource, Integer> entree : recette.entrySet()) {
            retirer(entree.getKey(), entree.getValue());
        }
    }

    /**
     * Retourne la quantité disponible d'une ressource.
     */
    public int getQuantite(TypeRessource type) {
        return stocks.getOrDefault(type, 0);
    }

    /**
    * Calcule le volume total occupé (quantité × poids) pour le mode difficile.
    */
    public int totalActuel() {
        int total = 0;
        for (Map.Entry<TypeRessource, Integer> entree : stocks.entrySet()) {
            total += entree.getValue() * entree.getKey().getPoids();
                    //  ^quantité stockée    ^poids associé au type
        }
        return total;
}

    /**
     * Retourne une vue non-modifiable du stock (utile pour l'affichage UI).
     */
    public Map<TypeRessource, Integer> getStocksImmutables() {
        return Collections.unmodifiableMap(stocks);
    }

    /**
     * Affichage lisible du stock (pour debug ou tableau de bord).
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== INVENTAIRE ===\n");
        for (Map.Entry<TypeRessource, Integer> e : stocks.entrySet()) {
            if (e.getValue() > 0) {
                sb.append(String.format("  %-20s : %d%n", e.getKey(), e.getValue()));
            }
        }
        if (modeDifficile) {
            sb.append(String.format("  TOTAL : %d / %d%n", totalActuel(), CAP_GLOBALE_DIFFICILE));
        }
        return sb.toString();
    }
}