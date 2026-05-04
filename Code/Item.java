package code;

public interface Item {
    // coordonnées X, Y nécessaires pour manipuler l'item
    int getX();
    int getY();

    // Calcule la distance euclidienne entre l'item et un autre
    double distance(Item autreItem);

    // Déplace l'item vers des coordonnées spécifiques
    void deplacer(int x, int y);

}