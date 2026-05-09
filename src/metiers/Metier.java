package metiers;

import batiments.Batiment;
import entites.Ouvrier;
import entites.Role;
import ressources.Stock;


/**
 * Interface commune à tous les métiers.
 *
 * Chaque métier :
 * - possède un type (enum Role)
 * - peut travailler dans certains bâtiments
 * - possède une action principale
 */
public interface Metier {

    /**
     * Retourne le type du métier.
     */
    Role getType();

    /**
     * Vérifie si ce métier peut travailler dans le bâtiment donné.
     */
    boolean peutTravaillerDans(Batiment batiment);

    /**
     * Action principale du métier, appelée à chaque tick.
     *
     * @param ouvrier   l'ouvrier qui travaille
     * @param batiment  le bâtiment où il travaille
     * @param stock     le stock global de la colonie
     * @param ticks     nombre de ticks écoulés depuis le dernier appel
     */
    void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks);

    /**
     * Nom affiché dans l'UI.
     */
    String getNomAffichage();
}
