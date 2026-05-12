package astra;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import carte.Carte;
import carte.Case;
import carte.Item;
import carte.Sol;
import carte.TypeSol;
import batiments.LieuDeRessource;
import ressources.TypeRessource;

/**
 * Construit un terrain 3D à partir d'une {@link Carte} (matrice 3D Case[x][y][z]).
 *
 * Pour chaque (x,y), on calcule la couche la plus haute occupée par un Sol :
 * c'est l'altitude visuelle de la case. La couleur reflète soit le
 * {@link LieuDeRessource} de surface (forêt, mine, gisement…), soit le
 * {@link TypeSol} le plus haut.
 */
public class RenduCarte {

    public static Group creerTerrain(Carte carte) {
        int largeur  = carte.getLargeur();
        int longueur = carte.getLongueur();
        int hauteur  = carte.getHauteur();

        int[][] heightmap = new int[largeur][longueur];
        Color[][] couleur = new Color[largeur][longueur];

        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < longueur; j++) {
                int top = 0;
                Sol solHaut = null;
                LieuDeRessource ressource = null;

                for (int k = 0; k < hauteur; k++) {
                    Case c = carte.getTile(i, j, k);
                    if (c == null) continue;
                    for (Item it : c.getContenu()) {
                        if (it instanceof Sol) {
                            top = k + 1;
                            solHaut = (Sol) it;
                        } else if (it instanceof LieuDeRessource) {
                            ressource = (LieuDeRessource) it;
                        }
                    }
                }

                heightmap[i][j] = top;

                if (ressource != null) {
                    couleur[i][j] = couleurRessource(ressource.getRessource());
                } else if (solHaut != null) {
                    couleur[i][j] = couleurSol(solHaut.getType());
                } else {
                    couleur[i][j] = Color.DARKGRAY;
                }
            }
        }

        Group group = new Group();

        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < longueur; j++) {
                float x0 = (float) CoordMonde.worldXCoin(i,     largeur);
                float x1 = (float) CoordMonde.worldXCoin(i + 1, largeur);
                float z0 = (float) CoordMonde.worldZCoin(j,     longueur);
                float z1 = (float) CoordMonde.worldZCoin(j + 1, longueur);

                float y00 = (float) (-hauteurCoin(heightmap, i,     j)     * CoordMonde.ECHELLE_HAUTEUR);
                float y01 = (float) (-hauteurCoin(heightmap, i,     j + 1) * CoordMonde.ECHELLE_HAUTEUR);
                float y10 = (float) (-hauteurCoin(heightmap, i + 1, j)     * CoordMonde.ECHELLE_HAUTEUR);
                float y11 = (float) (-hauteurCoin(heightmap, i + 1, j + 1) * CoordMonde.ECHELLE_HAUTEUR);

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
                vue.setMaterial(new PhongMaterial(couleur[i][j]));
                vue.setCullFace(CullFace.NONE);
                group.getChildren().add(vue);
            }
        }

        return group;
    }

    private static Color couleurSol(TypeSol type) {
        switch (type) {
            case HERBE:      return Color.rgb( 90, 160,  70);
            case SABLE:      return Color.rgb(230, 210, 140);
            case EAU:        return Color.rgb( 50, 110, 190);
            case ROCHE:      return Color.rgb(140, 130, 120);
            case ROCHE_DURE: return Color.rgb(100,  95,  90);
            case NEIGE:      return Color.rgb(240, 245, 250);
            default:         return Color.DARKGRAY;
        }
    }

    private static Color couleurRessource(TypeRessource type) {
        switch (type) {
            case BOIS:     return Color.rgb( 30, 110,  40);
            case FER:      return Color.rgb(170,  80,  60);
            case SILICIUM: return Color.rgb(180, 180, 200);
            case PIERRE:   return Color.rgb(120, 120, 110);
            case PETROLE:  return Color.rgb( 30,  30,  30);
            case GLACE:    return Color.rgb(200, 230, 255);
            default:       return Color.MAGENTA;
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
