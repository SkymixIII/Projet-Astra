package carte;

/**
 * Interface définissant tout élément possédant une position sur la carte
 */
public interface Item {

    // Récupération des coordonnées
    int getX();
    int getY();

    /**
     * Calcule la distance euclidienne par rapport à un autre Item.
     * Utile pour vérifier la proximité des ressources ou batiments
     */
    double distance(Item autre);

    /**
     * Déplace l'item vers des coordonnées absolues
     */
    void deplacer(int x, int y);

    /**
     * Déplace l'item d'une case selon une direction cardinale
     */
    void deplacer(Direction direction);
}
