import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * Construit un terrain 3D à partir d'une heightmap (int[x][y] = hauteur).
 *
 * Stratégie : les sommets vivent aux COINS des cases, pas au centre.
 * La hauteur d'un coin = moyenne des (1 à 4) cases qui le touchent.
 * Conséquence :
 *   - 4 cases voisines à la même hauteur → leur coin commun = cette hauteur
 *     → le quad rendu pour chacune est plat.
 *   - hauteurs qui diffèrent → coins à hauteurs différentes → quad incliné = pente.
 */
public class RenduCarte {

    public static final double TAILLE_CASE = 50.0;
    public static final double ECHELLE_HAUTEUR = 30.0;

    public static Group creerTerrain(int[][] heightmap) {
        int largeur = heightmap.length;
        int longueur = heightmap[0].length;

        Group group = new Group();
        int total = largeur * longueur;

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

                int idx = i * longueur + j;
                double hue = (idx * 360.0 / total) % 360.0;
                MeshView vue = new MeshView(mesh);
                vue.setMaterial(new PhongMaterial(Color.hsb(hue, 0.7, 0.9)));
                vue.setCullFace(CullFace.NONE);
                group.getChildren().add(vue);
            }
        }

        return group;
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
