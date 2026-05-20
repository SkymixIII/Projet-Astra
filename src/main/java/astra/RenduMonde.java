package astra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import batiments.Batiment;
import entites.Ouvrier;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import jeu.Jeu;

/**
 * « Modèle graphique » du jeu : regroupe tout le sous-système d'affichage et
 * de physique caméra derrière une API unique. La boucle principale appelle
 * simplement {@link #update()} à chaque frame ; l'objet se charge :
 *   - de synchroniser les meshes 3D des entités avec le modèle (ajouts,
 *     retraits, déplacements),
 *   - de mettre à jour la liste des collisions dynamiques,
 *   - de lire les entrées et d'appliquer la physique caméra,
 *   - de reprojeter les étiquettes 2D billboard.
 *
 * À usage typique dans {@code Main} :
 * <pre>
 *     RenduMonde rendu = new RenduMonde(jeu, WIDTH, HEIGHT);
 *     stage.setScene(rendu.getScene());
 *     new AnimationTimer() {
 *         public void handle(long now) { rendu.update(); }
 *     }.start();
 * </pre>
 */
public class RenduMonde {

    private final Jeu    jeu;
    private final double largeur;
    private final double hauteur;

    // ── Contenu 3D ──
    private final Group terrain;
    private final Group groupeOuvriers;
    private final Group groupeBatiments;
    private final Map<Ouvrier, Group> nodesOuvriers = new HashMap<>();
    private final Map<Batiment, Group> nodesBatiments = new HashMap<>();

    // ── Overlay 2D ──
    private final EtiquettesOuvriers etiquettesOuvriers;
    private final EtiquettesBatiment etiquettesBatiments;

    // ── Physique & inputs ──
    private final GestionCollisions collisions;
    private final GestionInputs     inputs;

    // ── Caméra & scène ──
    private final PerspectiveCamera camera;
    private final Rotate camRotX;
    private final Rotate camRotY;
    private final Scene  scene;

    public RenduMonde(Jeu jeu, double largeur, double hauteur) {
        this.jeu     = jeu;
        this.largeur = largeur;
        this.hauteur = hauteur;

        // 3D : terrain (statique) + groupe d'entités (dynamique) + lumière ambiante.
        this.terrain        = RenduCarte.creerTerrain(jeu.getCarte());
        this.groupeOuvriers = new Group();
        this.groupeBatiments = new Group();
        AmbientLight ambient = new AmbientLight(Color.WHITE);
        Group contenu3D = new Group(terrain, groupeBatiments, groupeOuvriers, ambient);

        // Caméra : centre de l'île de départ, orientée à l'horizontale.
        this.camRotX = new Rotate(0, Rotate.X_AXIS);
        this.camRotY = new Rotate(0, Rotate.Y_AXIS);
        this.camera  = new PerspectiveCamera(true);
        camera.getTransforms().addAll(camRotY, camRotX);
        camera.setTranslateX(84);
        camera.setTranslateY(-30);
        camera.setTranslateZ(4);
        camera.setNearClip(0.1);
        camera.setFarClip(20000);
        camera.setFieldOfView(60);

        SubScene sub = new SubScene(contenu3D, largeur, hauteur, true,
                                    SceneAntialiasing.BALANCED);
        sub.setFill(Color.LIGHTSKYBLUE);
        sub.setCamera(camera);

        // Physique : terrain seul ici, le dynamique est repeuplé par update().
        this.collisions = new GestionCollisions();
        collisions.construireDepuisCarte(jeu.getCarte());

        // Étiquettes 2D.
        this.etiquettesOuvriers = new EtiquettesOuvriers(
                jeu.getCarte(), jeu.getJoueur().getOuvriers(),
                camera, camRotX, camRotY, largeur, hauteur);
        this.etiquettesBatiments = new EtiquettesBatiment(
                jeu.getCarte(), jeu.getJoueur().getBatiments(),
                camera, camRotX, camRotY, largeur, hauteur);

        // Scène = SubScene 3D + overlay 2D (ouvriers + bâtiments).
        Group root = new Group(sub, etiquettesOuvriers.getOverlay(), etiquettesBatiments.getOverlay());
        this.scene = new Scene(root, largeur, hauteur);

        // Inputs (a besoin de la Scene pour s'enregistrer aux events clavier).
        this.inputs = new GestionInputs(scene, camera, camRotX, camRotY, collisions);

        // Touche I = bascule compact/détaillé des étiquettes ouvriers + bâtiments.
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.I) {
                etiquettesOuvriers.toggleModeDetaille();
                etiquettesBatiments.toggleModeDetaille();
            }
        });

        // Initial : créer les nodes d'ouvriers existants.
        update();
    }

    // ------------------------------------------------------------------ //
    //  API publique                                                       //
    // ------------------------------------------------------------------ //

    /** La {@link Scene} prête à être posée sur le {@code Stage}. */
    public Scene getScene() {
        return scene;
    }

    /** Accès au moteur de collisions (pour brancher d'autres entités, debug…). */
    public GestionCollisions getCollisions() {
        return collisions;
    }

    /**
     * À appeler à chaque frame depuis la boucle principale. Une seule entrée :
     * la couche graphique se synchronise sur le modèle (ouvriers ajoutés,
     * supprimés, déplacés), reconstruit ses collisions dynamiques, lit les
     * inputs et reprojette les étiquettes.
     */
    public void update() {
        long now = System.nanoTime();
        synchroniserBatiments();
        synchroniserOuvriers();
        inputs.miseAJour(now);
        etiquettesOuvriers.miseAJour();
        etiquettesBatiments.miseAJour();
    }

    // ------------------------------------------------------------------ //
    //  Synchronisation modèle → 3D + collisions                           //
    // ------------------------------------------------------------------ //

    private void synchroniserBatiments() {
        List<Batiment> batiments = jeu.getJoueur().getBatiments();
        Set<Batiment> presents   = new HashSet<>(batiments);

        // Ajouts + mise à jour position.
        for (Batiment b : batiments) {
            Group node = nodesBatiments.get(b);
            if (node == null) {
                node = RenduBatiments.creerNode(b, jeu.getCarte());
                nodesBatiments.put(b, node);
                groupeBatiments.getChildren().add(node);
            } else {
                RenduBatiments.majPosition(node, b, jeu.getCarte());
            }
        }

        // Retraits : bâtiments disparus du modèle.
        nodesBatiments.entrySet().removeIf(e -> {
            if (!presents.contains(e.getKey())) {
                groupeBatiments.getChildren().remove(e.getValue());
                return true;
            }
            return false;
        });

        // Collisions dynamiques reconstruites à chaque frame (bâtiments + ouvriers).
        List<GestionCollisions.BoiteCollision> aabbs = new ArrayList<>();
        for (Batiment b : batiments) {
            aabbs.add(RenduBatiments.boiteCollision(b, jeu.getCarte()));
        }
        List<Ouvrier> ouvriers = jeu.getJoueur().getOuvriers();
        for (Ouvrier o : ouvriers) {
            aabbs.add(RenduOuvriers.boiteCollision(o, jeu.getCarte()));
        }
        collisions.setObstaclesDynamiques(aabbs);
    }

    private void synchroniserOuvriers() {
        List<Ouvrier> ouvriers = jeu.getJoueur().getOuvriers();
        Set<Ouvrier> presents  = new HashSet<>(ouvriers);

        // Ajouts + mise à jour position.
        for (Ouvrier o : ouvriers) {
            Group node = nodesOuvriers.get(o);
            if (node == null) {
                node = RenduOuvriers.creerNode(o, jeu.getCarte());
                nodesOuvriers.put(o, node);
                groupeOuvriers.getChildren().add(node);
            } else {
                RenduOuvriers.majPosition(node, o, jeu.getCarte());
            }
        }

        // Retraits : ouvriers disparus du modèle.
        nodesOuvriers.entrySet().removeIf(e -> {
            if (!presents.contains(e.getKey())) {
                groupeOuvriers.getChildren().remove(e.getValue());
                return true;
            }
            return false;
        });
    }
}
