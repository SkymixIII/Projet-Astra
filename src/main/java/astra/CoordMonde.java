package astra;

import carte.Carte;
import carte.Case;
import carte.Item;
import carte.Sol;

/**
 * Conversions entre coordonnées grille (Carte, x∈[0..largeur[, y∈[0..longueur[)
 * et coordonnées monde JavaFX (Group / SubScene 3D).
 *
 * Source de vérité pour les dimensions visuelles : tous les modules graphiques
 * (rendu terrain, ouvriers, étiquettes, collisions, inputs) doivent piocher
 * ici plutôt que définir leurs propres constantes.
 *
 * Convention JavaFX : +X vers la droite, +Y vers le bas, +Z vers le fond.
 */
public final class CoordMonde {

    public static final double TAILLE_CASE     = 8.0;
    public static final double ECHELLE_HAUTEUR = 6.0;

    private CoordMonde() {}

    /** Centre de la case (gx, _) projeté sur l'axe X monde. */
    public static double worldXCentre(int gx, Carte carte) {
        return (gx + 0.5 - carte.getLargeur() / 2.0) * TAILLE_CASE;
    }

    /** Centre de la case (_, gy) projeté sur l'axe Z monde. */
    public static double worldZCentre(int gy, Carte carte) {
        return (gy + 0.5 - carte.getLongueur() / 2.0) * TAILLE_CASE;
    }

    /** Coin de case en X monde (pour le rendu terrain qui maille par coins). */
    public static double worldXCoin(int i, int largeur) {
        return (i - largeur / 2.0) * TAILLE_CASE;
    }

    /** Coin de case en Z monde. */
    public static double worldZCoin(int j, int longueur) {
        return (j - longueur / 2.0) * TAILLE_CASE;
    }

    /**
     * Renvoie l'altitude (en cases) du dernier {@link Sol} présent sur la
     * colonne (gx, gy). 0 si la colonne est vide.
     */
    public static int topColonne(Carte carte, int gx, int gy) {
        int top = 0;
        for (int k = 0; k < carte.getHauteur(); k++) {
            Case c = carte.getTile(gx, gy, k);
            if (c == null) continue;
            for (Item it : c.getContenu()) {
                if (it instanceof Sol) top = k + 1;
            }
        }
        return top;
    }

    /** Y monde (JavaFX +Y descend) du sommet visible de la colonne (gx, gy). */
    public static double solY(Carte carte, int gx, int gy) {
        return -topColonne(carte, gx, gy) * ECHELLE_HAUTEUR;
    }
}
