import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.AmbientLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import jeu.Jeu;

public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        // Construction de la map "officielle" du jeu (Jeu.creationMonde()).
        Jeu jeu = new Jeu();
        jeu.creationMonde();

        // Éclairage purement ambiant : toutes les faces gardent leur couleur
        // peu importe l'angle de vue.
        AmbientLight ambient = new AmbientLight(Color.WHITE);

        Group terrain = RenduCarte.creerTerrain(jeu.getCarte());

        Group contenu3D = new Group(terrain, ambient);

        // Caméra placée au-dessus du centre de l'île de départ (cellule (60,50)),
        // à 5 cases de hauteur, orientée à l'horizontale.
        Rotate camRotX = new Rotate(0, Rotate.X_AXIS);
        Rotate camRotY = new Rotate(0,  Rotate.Y_AXIS);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(camRotY, camRotX);
        camera.setTranslateX(84);
        camera.setTranslateY(-30);
        camera.setTranslateZ(4);
        camera.setNearClip(0.1);
        camera.setFarClip(20000);
        camera.setFieldOfView(60);

        // SubScene pour que le 3D ait son propre fill (sinon JavaFX laisse
        // des zones noires autour de la géométrie).
        SubScene sub = new SubScene(contenu3D, WIDTH, HEIGHT, true,
                                    SceneAntialiasing.BALANCED);
        sub.setFill(Color.LIGHTSKYBLUE);
        sub.setCamera(camera);

        Group root = new Group(sub);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        GestionCollisions collisions = new GestionCollisions();
        collisions.construireDepuisCarte(jeu.getCarte());

        GestionInputs inputs = new GestionInputs(scene, camera, camRotX, camRotY, collisions);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                inputs.miseAJour(now);
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
