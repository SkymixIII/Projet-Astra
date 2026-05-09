import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * Construit un terrain 3D à partir d'une matrice 3D voxelisée.
 *
 * Format attendu : map[z][x][y] (z = couche du sol au sommet).
 * Une valeur != 0 signifie qu'un bloc est présent. La couleur dépend du
 * code (1=HERBE, 2=SABLE, 3=EAU, 4=ROCHE, 5=NEIGE, 6=BOIS, 7=FER).
 *
 * Stratégie de rendu : un quad par case (x,y), avec une hauteur égale à
 * la couche la plus haute occupée. Les coins du quad reprennent la
 * moyenne des hauteurs des cases voisines pour produire des pentes
 * douces là où l'altitude change.
 */
public class RenduCarte {

    public static final double TAILLE_CASE = 50.0;
    public static final double ECHELLE_HAUTEUR = 30.0;

    public static Group creerTerrain(int[][][] map3d) {
        int hauteur  = map3d.length;
        int largeur  = map3d[0].length;
        int longueur = map3d[0][0].length;

        int[][] heightmap = new int[largeur][longueur];
        int[][] typeTop   = new int[largeur][longueur];
        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < longueur; j++) {
                int top = 0;
                int type = 0;
                for (int k = 0; k < hauteur; k++) {
                    if (map3d[k][i][j] != 0) {
                        top = k + 1;
                        type = map3d[k][i][j];
                    }
                }
                heightmap[i][j] = top;
                typeTop[i][j]   = type;
            }
        }

        Group group = new Group();

        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < longueur; j++) {
                float x0 = (float) ((i     - largeur  / 2.0) * TAILLE_CASE);
                float x1 = (float) ((i + 1 - largeur  / 2.0) * TAILLE_CASE);
                float z0 = (float) ((j     - longueur / 2.0) * TAILLE_CASE);
                float z1 = (float) ((j + 1 - longueur / 2.0) * TAILLE_CASE);

                float y00 = (float) (-hauteurCoin(heightmap, i,     j)     * ECHELLE_HAUTEUR);
                float y01 = (float) (-hauteurCoin(heightmap, i,     j + 1) * ECHELLE_HAUTEUR);
                float y10 = (float) (-hauteurCoin(heightmap, i + 1, j)     * ECHELLE_HAUTEUR);
                float y11 = (float) (-hauteurCoin(heightmap, i + 1, j + 1) * ECHELLE_HAUTEUR);

                TriangleMesh mesh = new TriangleMesh();
                mesh.getPoints().addAll(
                    x0, y00, z0,
                    x0, y01, z1,
                    x1, y10, z0,
                    x1, y11, z1
                );
                mesh.getTexCoords().addAll(0, 0);
                mesh.getFaces().addAll(
                    0, 0, 3, 0, 2, 0,
                    0, 0, 1, 0, 3, 0
                );

                MeshView vue = new MeshView(mesh);
                vue.setMaterial(new PhongMaterial(couleurType(typeTop[i][j])));
                vue.setCullFace(CullFace.NONE);
                group.getChildren().add(vue);
            }
        }

        return group;
    }

    private static Color couleurType(int type) {
        switch (type) {
            case 1: return Color.rgb( 90, 160,  70); // HERBE
            case 2: return Color.rgb(230, 210, 140); // SABLE
            case 3: return Color.rgb( 50, 110, 190); // EAU
            case 4: return Color.rgb(140, 130, 120); // ROCHE
            case 5: return Color.rgb(240, 245, 250); // NEIGE
            case 6: return Color.rgb( 30, 110,  40); // BOIS
            case 7: return Color.rgb(170,  80,  60); // FER
            default: return Color.DARKGRAY;          // 0 = vide
        }
    }

    private static double hauteurCoin(int[][] heightmap, int sx, int sy) {
        int largeur = heightmap.length;
        int longueur = heightmap[0].length;
        double somme = 0;
        int n = 0;
        for (int dx = -1; dx <= 0; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                int cx = sx + dx;
                int cy = sy + dy;
                if (cx >= 0 && cx < largeur && cy >= 0 && cy < longueur) {
                    somme += heightmap[cx][cy];
                    n++;
                }
            }
        }
        return n > 0 ? somme / n : 0;
    }
}
