import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    /**
     * Carte 3D codée en dur — format MAP_3D[z][x][y].
     * Chaque entier = type de bloc :
     *   0 = vide,
     *   1 = HERBE, 2 = SABLE, 3 = EAU,
     *   4 = ROCHE, 5 = NEIGE,
     *   6 = BOIS,  7 = FER.
     *
     * 4 couches (z=0 au sol → z=3 au sommet).
     */
    private static final int[][][] MAP_3D = {
        // ----- z = 0 (sol) -----
        {
            {1, 1, 1, 1, 1, 1, 1, 1, 2, 3},
            {1, 1, 1, 1, 1, 1, 1, 2, 3, 3},
            {1, 1, 4, 4, 1, 1, 1, 1, 2, 3},
            {1, 1, 4, 4, 1, 6, 1, 1, 1, 2},
            {1, 1, 4, 1, 1, 6, 6, 1, 1, 1},
            {1, 1, 1, 1, 6, 6, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {3, 2, 1, 1, 1, 1, 1, 1, 1, 1},
        },
        // ----- z = 1 (collines / 2e étage de la montagne) -----
        {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
            {0, 0, 4, 7, 0, 0, 0, 0, 0, 0},
            {0, 0, 4, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        },
        // ----- z = 2 (pic) -----
        {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 4, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 4, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        },
        // ----- z = 3 (sommet enneigé) -----
        {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 5, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        },
    };

    @Override
    public void start(Stage stage) {
        // Éclairage purement ambiant : toutes les faces gardent leur couleur
        // peu importe l'angle de vue (sinon les back-faces deviennent noires
        // dans certaines orientations).
        AmbientLight ambient = new AmbientLight(Color.WHITE);

        Group terrain = RenduCarte.creerTerrain(MAP_3D);

        Group contenu3D = new Group(terrain, ambient);

        // Caméra en hauteur, inclinée vers le bas → on voit la map à plat.
        Rotate camRotX = new Rotate(35, Rotate.X_AXIS);
        Rotate camRotY = new Rotate(0,  Rotate.Y_AXIS);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(camRotY, camRotX);
        camera.setTranslateY(-300);
        camera.setTranslateZ(-400);
        camera.setNearClip(0.1);
        camera.setFarClip(20000);
        camera.setFieldOfView(60);

        // Le 3D vit dans un SubScene avec son propre fill — sinon le buffer 3D
        // de JavaFX laisse des zones noires aux endroits sans géométrie.
        SubScene sub = new SubScene(contenu3D, WIDTH, HEIGHT, true,
                                    SceneAntialiasing.BALANCED);
        sub.setFill(Color.LIGHTSKYBLUE);
        sub.setCamera(camera);

        Group root = new Group(sub);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        GestionInputs inputs = new GestionInputs(scene, camera, camRotX, camRotY);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                inputs.miseAJour();
            }
        }.start();

        stage.setTitle("Projet Astra");
        stage.setScene(scene);
        stage.show();
        scene.getRoot().requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
