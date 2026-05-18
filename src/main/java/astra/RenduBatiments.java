package astra;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

import carte.Carte;
import batiments.Batiment;

/**
 * Helpers de rendu 3D pour un bâtiment - pure fonction du modèle, sans état
 * 
 * Chaque bâtiment est représenté par un {@link Group} contenant une base cubique
 * et une cheminée / antenne cylindrique. Le {@link RenduMonde} se charge
 * de créer un node par bâtiment, de le positionner selon {@code batiment.getX() / getY()},
 * et de fournir sa boîte de collision englobante à {@link GestionCollisions}
 */
public final class RenduBatiments {

    /** Mise à l'échelle globale des bâtiments par rapport à la taille d'une case. */
    public static final double RATIO_TAILLE = 0.85;

    public static final double LARGEUR_BASE = RATIO_TAILLE * CoordMonde.TAILLE_CASE;
    public static final double HAUTEUR_BASE = 1.2 * RATIO_TAILLE * CoordMonde.ECHELLE_HAUTEUR;
    public static final double PROFONDEUR_BASE = RATIO_TAILLE * CoordMonde.TAILLE_CASE;

    public static final double RAYON_CHEMINEE = 0.15 * RATIO_TAILLE * CoordMonde.TAILLE_CASE;
    public static final double HAUTEUR_CHEMINEE = 1.0 * RATIO_TAILLE * CoordMonde.ECHELLE_HAUTEUR;

    // Couleurs typées "complexe industriel / base spatiale"
    private static final Color COULEUR_BASE = Color.rgb(100, 110, 120);
    private static final Color COULEUR_CHEMINEE = Color.rgb(180, 70, 60);

    private static final PhongMaterial MAT_BASE = new PhongMaterial(COULEUR_BASE);
    private static final PhongMaterial MAT_CHEMINEE = new PhongMaterial(COULEUR_CHEMINEE);

    private RenduBatiments() {}

    /**
     * Construit le node 3D d'un bâtiment (base + cheminée/antenne), positionné selon sa
     * position sur la carte
     */
    public static Group creerNode(Batiment b, Carte carte) {
        Box base = new Box(LARGEUR_BASE, HAUTEUR_BASE, PROFONDEUR_BASE);
        base.setMaterial(MAT_BASE);

        Cylinder cheminee = new Cylinder(RAYON_CHEMINEE, HAUTEUR_CHEMINEE);
        cheminee.setMaterial(MAT_CHEMINEE);

        Group node = new Group(base, cheminee);
        majPosition(node, b, carte);
        return node;
    }

    /**
     * Repositionne le node d'un bâtiment selon sa position fixe ou mise à jour
     * 
     * Le node contient, dans cet ordre : la base (Box) puis la cheminée (Cylinder)
     */
    public static void majPosition(Group node, Batiment b, Carte carte) {
         double worldX = CoordMonde.worldXCentre(b.getX(), carte);
         double worldZ = CoordMonde.worldZCentre(b.getY(), carte);
         double solY = CoordMonde.solY(carte, b.getX(), b.getY());

         // Positionnement de la grosse base
         Box base = (Box) node.getChildren().get(0);
         base.setTranslateX(worldX);
         base.setTranslateZ(worldZ);
         base.setTranslateY(solY - HAUTEUR_BASE / 2.0);

         // Positionnement de la cheminée sur le toit du bâtiment (décalée un peu sur le côté en X)
         Cylinder cheminee = (Cylinder) node.getChildren().get(1);
         cheminee.setTranslateX(worldX + LARGEUR_BASE * 0.2);
         cheminee.setTranslateZ(worldZ);
         cheminee.setTranslateY(solY - HAUTEUR_BASE - HAUTEUR_CHEMINEE / 2.0);
    }

    /**
     * AABB englobante complète du bâtiment, essentielle pour empêcher les ouvriers
     * de traverser les murs d'une usine via {@link GestionCollisions}
     */
    public static GestionCollisions.BoiteCollision boiteCollision(Batiment b, Carte carte) {
        double worldX = CoordMonde.worldXCentre(b.getX(), carte);
        double worldZ = CoordMonde.worldZCentre(b.getY(), carte);
        double solY = CoordMonde.solY(carte, b.getX(), b.getY());

    double demiLargeur = LARGEUR_BASE / 2.0;
    double demiProfondeur = PROFONDEUR_BASE / 2.0;

    double yHaut = solY - HAUTEUR_BASE - HAUTEUR_CHEMINEE;
    double yBas = solY;

    return new GestionCollisions.BoiteCollision(
        worldX - demiLargeur, yHaut, worldZ - demiProfondeur,
        worldX + demiLargeur, yBas, worldZ + demiProfondeur);
    }
}