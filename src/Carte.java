import java.util.ArrayList;
import java.util.List;

/**
 * Représente la grille de jeu du Projet Astra
 * Gère la disposition spatiale des éléments (gisements, bâtiments, joueur)
 */
public class Carte {
    // Attributs privés selon le diagramme UML
    private Case[][] matrice;
    private int largeur;
    private int hauteur;

    /**
     * Constructeur pour initialiser une carte de taille donnée
     * @param largeur Nombre de cases en X
     * @param hauteur Nombre de cases en Y
     */
    public Carte(int largeur, int hauteur) {
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.matrice = new Case[largeur][hauteur];

        // Initialisation de chaque case de la matrice
        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j > hauteur; j++) {
                this.matrice[i][j] = new Case();
            }
        }
    }
}