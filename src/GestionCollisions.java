import java.util.ArrayList;
import java.util.Collection;
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
 *     carrée) et 3 cases de haut, centrée sur la position de la caméra ;
 *   - n'importe quel autre élément affiché (bâtiment, ressource, etc.) peut
 *     être ajouté via {@link #ajouterObstacle(BoiteCollision)}.
 *
 * Les dimensions du monde reprennent celles de {@link RenduCarte} pour rester
 * cohérentes entre l'affichage et la physique.
 */
public class GestionCollisions {

    // Dimensions du moteur de rendu (cf. {@link CoordMonde}).
    public static final double TAILLE_CASE     = CoordMonde.TAILLE_CASE;
    public static final double ECHELLE_HAUTEUR = CoordMonde.ECHELLE_HAUTEUR;

    // Dimensions de la caméra exprimées en cases (base carrée 0,25 x 0,25, hauteur 3).
    public static final double LARGEUR_CAMERA_CASE = 0.25;
    public static final double HAUTEUR_CAMERA_CASE = 3.0;

    // Conversion en coordonnées monde JavaFX.
    public static final double DEMI_LARGEUR_CAMERA = (LARGEUR_CAMERA_CASE * TAILLE_CASE) / 2.0;
    public static final double DEMI_HAUTEUR_CAMERA = (HAUTEUR_CAMERA_CASE * ECHELLE_HAUTEUR) / 2.0;

    // Hauteur maximale d'une "marche" que la caméra peut gravir automatiquement
    // (différence de niveau entre deux cases voisines). Au-delà, c'est un mur.
    public static final double HAUTEUR_MARCHE_MAX = 1.0 * ECHELLE_HAUTEUR;

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

    /** Obstacles du terrain (statiques, construits une fois depuis la carte). */
    private final List<BoiteCollision> obstaclesTerrain = new ArrayList<>();

    /**
     * Obstacles dynamiques (entités mobiles : ouvriers, bâtiments mobiles…).
     * Reconstruits à chaque frame par {@link RenduMonde}.
     */
    private final List<BoiteCollision> obstaclesDynamiques = new ArrayList<>();

    /**
     * (Re)construit la liste des boîtes de collision du terrain à partir de la
     * carte. Pour chaque colonne (i, j), on cherche la hauteur du dernier
     * {@link Sol} (comme dans {@link RenduCarte}) et on crée une AABB allant
     * du sommet visible jusqu'au "fond du monde".
     */
    public void construireDepuisCarte(Carte carte) {
        obstaclesTerrain.clear();
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

                obstaclesTerrain.add(new BoiteCollision(x0, yHaut, z0, x1, yBas, z1));
            }
        }
    }

    /**
     * Remplace l'intégralité des obstacles dynamiques. À appeler à chaque
     * frame par la couche graphique avec les AABB courantes des entités.
     */
    public void setObstaclesDynamiques(Collection<BoiteCollision> boites) {
        obstaclesDynamiques.clear();
        obstaclesDynamiques.addAll(boites);
    }

    /** Construit la boîte de collision de la caméra centrée sur (x, y, z). */
    public BoiteCollision boiteCamera(double x, double y, double z) {
        return new BoiteCollision(
            x - DEMI_LARGEUR_CAMERA, y - DEMI_HAUTEUR_CAMERA, z - DEMI_LARGEUR_CAMERA,
            x + DEMI_LARGEUR_CAMERA, y + DEMI_HAUTEUR_CAMERA, z + DEMI_LARGEUR_CAMERA
        );
    }

    /** True si la boîte donnée intersecte au moins un obstacle (terrain ou dynamique). */
    public boolean enCollision(BoiteCollision boite) {
        for (BoiteCollision o : obstaclesTerrain) {
            if (boite.entreEnCollision(o)) return true;
        }
        for (BoiteCollision o : obstaclesDynamiques) {
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
     *
     * Si un déplacement horizontal (X ou Z) bute sur un obstacle qui dépasse
     * d'au plus {@link #HAUTEUR_MARCHE_MAX} au-dessus des pieds de la caméra,
     * on monte la caméra juste ce qu'il faut pour passer : on glisse sur la
     * pente en montant.
     */
    public Point3D resoudreDeplacementCamera(double x, double y, double z,
                                              double dx, double dy, double dz) {
        double nx = x, ny = y, nz = z;
        if (dx != 0) {
            if (!cameraEnCollision(nx + dx, ny, nz)) {
                nx += dx;
            } else {
                double yMarche = essayerMarche(nx + dx, ny, nz);
                if (yMarche != ny) {
                    nx += dx;
                    ny = yMarche;
                }
            }
        }
        if (dz != 0) {
            if (!cameraEnCollision(nx, ny, nz + dz)) {
                nz += dz;
            } else {
                double yMarche = essayerMarche(nx, ny, nz + dz);
                if (yMarche != ny) {
                    nz += dz;
                    ny = yMarche;
                }
            }
        }
        if (dy != 0 && !cameraEnCollision(nx, ny + dy, nz)) ny += dy;
        return new Point3D(nx, ny, nz);
    }

    /**
     * Cherche la plus petite remontée Y qui permette de placer la caméra en
     * (x, ?, z) sans collision. Renvoie la valeur Y résultante, ou {@code y}
     * inchangé si la marche serait trop haute ou si la position remontée est
     * elle-même bloquée (plafond).
     *
     * En JavaFX, +Y descend : "remonter" la caméra signifie diminuer Y, donc
     * on cherche le {@code minY} d'obstacle le plus haut (le plus petit en
     * valeur numérique) parmi ceux qui bloquent la trajectoire.
     */
    private double essayerMarche(double x, double y, double z) {
        BoiteCollision boite = boiteCamera(x, y, z);
        double yRequis = y;
        boolean bloque = false;
        for (BoiteCollision o : obstaclesTerrain) {
            if (!boite.entreEnCollision(o)) continue;
            bloque = true;
            double yCandidat = o.minY - DEMI_HAUTEUR_CAMERA;
            if (yCandidat < yRequis) yRequis = yCandidat;
        }
        for (BoiteCollision o : obstaclesDynamiques) {
            if (!boite.entreEnCollision(o)) continue;
            bloque = true;
            double yCandidat = o.minY - DEMI_HAUTEUR_CAMERA;
            if (yCandidat < yRequis) yRequis = yCandidat;
        }
        if (!bloque) return y;
        if (y - yRequis > HAUTEUR_MARCHE_MAX) return y;
        if (cameraEnCollision(x, yRequis, z)) return y;
        return yRequis;
    }

    /**
     * Ajoute une boîte de collision arbitraire au terrain statique
     * (bâtiment fixe, décor…). Pour les entités mobiles, préférer
     * {@link #setObstaclesDynamiques(Collection)} reconstruit chaque frame.
     */
    public void ajouterObstacleTerrain(BoiteCollision boite) {
        obstaclesTerrain.add(boite);
    }

    /** Vide la liste des obstacles terrain. */
    public void viderTerrain() {
        obstaclesTerrain.clear();
    }

    public List<BoiteCollision> getObstaclesTerrain()     { return obstaclesTerrain; }
    public List<BoiteCollision> getObstaclesDynamiques()  { return obstaclesDynamiques; }
}
