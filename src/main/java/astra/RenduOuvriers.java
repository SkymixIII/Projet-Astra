package astra;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;

import carte.Carte;
import entites.Ouvrier;

/**
 * Helpers de rendu 3D pour un ouvrier — pure fonction du modèle, sans état.
 *
 * Chaque ouvrier est représenté par un {@link Group} contenant un corps
 * cylindrique surmonté d'une tête sphérique. Le {@link RenduMonde} se charge
 * de créer un node par ouvrier, de le repositionner à chaque tick selon
 * {@code ouvrier.getX() / getY()}, et de fournir sa boîte de collision
 * englobante à {@link GestionCollisions}.
 *
 * Les dimensions sont dérivées de {@link CoordMonde} pour rester alignées
 * avec le terrain.
 */
public final class RenduOuvriers {

    /** Mise à l'échelle globale des ouvriers par rapport à leurs proportions de base. */
    public static final double RATIO_TAILLE = 0.66;

    public static final double RAYON_CORPS   = 0.15 * RATIO_TAILLE * CoordMonde.TAILLE_CASE;
    public static final double HAUTEUR_CORPS = 1.5  * RATIO_TAILLE * CoordMonde.ECHELLE_HAUTEUR;
    public static final double RAYON_TETE    = 0.3  * RATIO_TAILLE * CoordMonde.ECHELLE_HAUTEUR;

    private static final Color COULEUR_CORPS = Color.rgb(220, 180,  60);
    private static final Color COULEUR_TETE  = Color.rgb(230, 200, 160);

    private static final PhongMaterial MAT_CORPS = new PhongMaterial(COULEUR_CORPS);
    private static final PhongMaterial MAT_TETE  = new PhongMaterial(COULEUR_TETE);

    private RenduOuvriers() {}

    /**
     * Construit le node 3D d'un ouvrier (corps + tête), positionné selon sa
     * position courante sur la carte.
     */
    public static Group creerNode(Ouvrier o, Carte carte) {
        Cylinder corps = new Cylinder(RAYON_CORPS, HAUTEUR_CORPS);
        corps.setMaterial(MAT_CORPS);

        Sphere tete = new Sphere(RAYON_TETE);
        tete.setMaterial(MAT_TETE);

        Group node = new Group(corps, tete);
        majPosition(node, o, carte);
        return node;
    }

    /**
     * Repositionne le node d'un ouvrier selon sa position actuelle dans le
     * modèle. À appeler à chaque tick par {@link RenduMonde}.
     *
     * Le node est supposé contenir, dans cet ordre : le corps (cylindre) puis
     * la tête (sphère) — c'est ce que produit {@link #creerNode}.
     */
    public static void majPosition(Group node, Ouvrier o, Carte carte) {
        double worldX = CoordMonde.worldXCentre(o.getX(), carte);
        double worldZ = CoordMonde.worldZCentre(o.getY(), carte);
        double solY   = CoordMonde.solY(carte, o.getX(), o.getY());

        Cylinder corps = (Cylinder) node.getChildren().get(0);
        corps.setTranslateX(worldX);
        corps.setTranslateZ(worldZ);
        corps.setTranslateY(solY - HAUTEUR_CORPS / 2.0);

        Sphere tete = (Sphere) node.getChildren().get(1);
        tete.setTranslateX(worldX);
        tete.setTranslateZ(worldZ);
        tete.setTranslateY(solY - HAUTEUR_CORPS - RAYON_TETE);
    }

    /**
     * AABB englobante (corps + tête) d'un ouvrier, à utiliser comme obstacle
     * dynamique dans {@link GestionCollisions}.
     */
    public static GestionCollisions.BoiteCollision boiteCollision(Ouvrier o, Carte carte) {
        double worldX = CoordMonde.worldXCentre(o.getX(), carte);
        double worldZ = CoordMonde.worldZCentre(o.getY(), carte);
        double solY   = CoordMonde.solY(carte, o.getX(), o.getY());
        double demi   = RAYON_TETE;
        double yHaut  = solY - HAUTEUR_CORPS - 2 * RAYON_TETE;
        double yBas   = solY;
        return new GestionCollisions.BoiteCollision(
                worldX - demi, yHaut, worldZ - demi,
                worldX + demi, yBas,  worldZ + demi);
    }
}
