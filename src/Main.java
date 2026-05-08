import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private static final int[][] HEIGHTMAP_DEMO = {
        {2, 2, 2, 2, 1, 1, 1, 1},
        {2, 3, 3, 2, 1, 1, 1, 1},
        {2, 3, 3, 2, 1, 0, 0, 1},
        {2, 2, 2, 2, 1, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 2, 2, 1, 1, 1, 1},
        {1, 1, 2, 2, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1}
    };

    @Override
    public void start(Stage stage) {
        AmbientLight ambient = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        PointLight pointLight = new PointLight(Color.color(1.0, 1.0, 0.95));
        pointLight.setTranslateX(-300);
        pointLight.setTranslateY(-500);
        pointLight.setTranslateZ(-300);

        Group terrain = RenduCarte.creerTerrain(HEIGHTMAP_DEMO);
        terrain.getTransforms().addAll(
            new Rotate(25, Rotate.Y_AXIS),
            new Rotate(20, Rotate.X_AXIS)
        );

        Group root = new Group(terrain, ambient, pointLight);

        Rotate camRotX = new Rotate(0, Rotate.X_AXIS);
        Rotate camRotY = new Rotate(0, Rotate.Y_AXIS);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(camRotY, camRotX);
        camera.setTranslateZ(-500);
        camera.setNearClip(0.1);
        camera.setFarClip(3000);
        camera.setFieldOfView(60);

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.GRAY);
        scene.setCamera(camera);

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
