package astra;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point3D;

import carte.Carte;
import carte.Case;
import carte.Item;
import carte.Sol;

/**
 * Gère la détection et la résolution des collisions du monde 3D.
 *
 * Tout ce qui est affiché à l'écran est modélisé par une boîte de collision
 * alignée sur les axes (AABB) :
 *   - chaque colonne du terrain donne une AABB qui s'étend du sommet visible
 *     vers le bas (sol "plein") ;
 *   - la caméra est représentée par une AABB de 0,25 case de large (base
 *     carrée) et 0,5 case de haut, centrée sur la position de la caméra ;
 *   - n'importe quel autre élément affiché (bâtiment, ressource, etc.) peut
 *     être ajouté via {@link #ajouterObstacle(BoiteCollision)}.
 *
 * Les dimensions du monde reprennent celles de {@link RenduCarte} pour rester
 * cohérentes entre l'affichage et la physique.
 */
public class GestionCollisions {

    // Dimensions du moteur de rendu (1 case = TAILLE_CASE en X/Z, ECHELLE_HAUTEUR en Y).
    public static final double TAILLE_CASE = RenduCarte.TAILLE_CASE;
    public static final double ECHELLE_HAUTEUR = RenduCarte.ECHELLE_HAUTEUR;

    // Dimensions de la caméra exprimées en cases (base carrée 0,25 x 0,25, hauteur 0,5).
    public static final double LARGEUR_CAMERA_CASE = 0.25;
    public static final double HAUTEUR_CAMERA_CASE = 0.5;

    // Conversion en coordonnées monde JavaFX.
    public static final double DEMI_LARGEUR_CAMERA = (LARGEUR_CAMERA_CASE * TAILLE_CASE) / 2.0;
    public static final double DEMI_HAUTEUR_CAMERA = (HAUTEUR_CAMERA_CASE * ECHELLE_HAUTEUR) / 2.0;

    // Y "fond du monde" : valeur largement positive pour rendre les colonnes
    // de terrain solides vers le bas (en JavaFX, +Y pointe vers le bas).
    private static final double Y_FOND_DU_MONDE = 1.0e6;

    /**
     * Boîte de collision alignée sur les axes (Axis-Aligned Bounding Box).
     * Convention : minX < maxX, minY < maxY, minZ < maxZ.
     */
    public static final class BoiteCollision {
        public final double minX, minY, minZ;
        public final double maxX, maxY, maxZ;

        public BoiteCollision(double minX, double minY, double minZ,
                              double maxX, double maxY, double maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }

        /** Intersection stricte de deux AABB. Les contacts par face ne comptent pas. */
        public boolean entreEnCollision(BoiteCollision autre) {
            return (minX < autre.maxX && maxX > autre.minX)
                && (minY < autre.maxY && maxY > autre.minY)
                && (minZ < autre.maxZ && maxZ > autre.minZ);
        }

        public BoiteCollision translater(double dx, double dy, double dz) {
            return new BoiteCollision(minX + dx, minY + dy, minZ + dz,
                                       maxX + dx, maxY + dy, maxZ + dz);
        }
    }

    private final List<BoiteCollision> obstacles = new ArrayList<>();

    /**
     * (Re)construit la liste des boîtes de collision à partir de la carte.
     * Pour chaque colonne (i, j), on cherche la hauteur du dernier {@link Sol}
     * (comme dans {@link RenduCarte}) et on crée une AABB allant du sommet
     * visible jusqu'au "fond du monde".
     */
    public void construireDepuisCarte(Carte carte) {
        obstacles.clear();
        int largeur  = carte.getLargeur();
        int longueur = carte.getLongueur();
        int hauteur  = carte.getHauteur();

        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < longueur; j++) {
                int top = 0;
                for (int k = 0; k < hauteur; k++) {
                    Case c = carte.getTile(i, j, k);
                    if (c == null) continue;
                    for (Item it : c.getContenu()) {
                        if (it instanceof Sol) {
                            top = k + 1;
                        }
                    }
                }
                if (top <= 0) continue;

                double x0 = (i     - largeur  / 2.0) * TAILLE_CASE;
                double x1 = (i + 1 - largeur  / 2.0) * TAILLE_CASE;
                double z0 = (j     - longueur / 2.0) * TAILLE_CASE;
                double z1 = (j + 1 - longueur / 2.0) * TAILLE_CASE;

                // En JavaFX, +Y descend : le sommet visible est en Y négatif,
                // le terrain s'étend en Y positif vers le fond du monde.
                double yHaut = -top * ECHELLE_HAUTEUR;
                double yBas  = Y_FOND_DU_MONDE;

                obstacles.add(new BoiteCollision(x0, yHaut, z0, x1, yBas, z1));
            }
        }
    }

    /** Construit la boîte de collision de la caméra centrée sur (x, y, z). */
    public BoiteCollision boiteCamera(double x, double y, double z) {
        return new BoiteCollision(
            x - DEMI_LARGEUR_CAMERA, y - DEMI_HAUTEUR_CAMERA, z - DEMI_LARGEUR_CAMERA,
            x + DEMI_LARGEUR_CAMERA, y + DEMI_HAUTEUR_CAMERA, z + DEMI_LARGEUR_CAMERA
        );
    }

    /** True si la boîte donnée intersecte au moins un obstacle. */
    public boolean enCollision(BoiteCollision boite) {
        for (BoiteCollision o : obstacles) {
            if (boite.entreEnCollision(o)) return true;
        }
        return false;
    }

    /** True si la caméra placée en (x, y, z) entre en collision avec un obstacle. */
    public boolean cameraEnCollision(double x, double y, double z) {
        return enCollision(boiteCamera(x, y, z));
    }

    /**
     * Résout un déplacement de la caméra par balayage axe par axe : on tente
     * le déplacement sur X, puis Y, puis Z, et chaque composante est rejetée
     * indépendamment si elle entraîne une collision. Cela permet de glisser
     * le long d'un mur au lieu d'être complètement bloqué.
     */
    public Point3D resoudreDeplacementCamera(double x, double y, double z,
                                              double dx, double dy, double dz) {
        double nx = x, ny = y, nz = z;
        if (dx != 0 && !cameraEnCollision(nx + dx, ny, nz)) nx += dx;
        if (dy != 0 && !cameraEnCollision(nx, ny + dy, nz)) ny += dy;
        if (dz != 0 && !cameraEnCollision(nx, ny, nz + dz)) nz += dz;
        return new Point3D(nx, ny, nz);
    }

    /**
     * Ajoute une boîte de collision arbitraire (bâtiment, ressource, fusée…).
     * Tout objet affiché qui n'est pas issu directement de la heightmap doit
     * passer par ce point d'entrée pour rester cohérent avec la règle
     * "tout ce qui est affiché est une collision".
     */
    public void ajouterObstacle(BoiteCollision boite) {
        obstacles.add(boite);
    }

    /** Vide la liste des obstacles (utile pour reconstruire la carte). */
    public void vider() {
        obstacles.clear();
    }

    public List<BoiteCollision> getObstacles() {
        return obstacles;
    }
}
