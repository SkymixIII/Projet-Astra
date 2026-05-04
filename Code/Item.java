package Code;

public interface Item {

    // Calcule la distance euclidienne entre cet item et un autre
    double distance(Item autreItem);

    // Déplace l'item vers des coordonnées spécifiques
    void deplacer(int x, int y);

}