import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        Box cube = new Box(100, 100, 100);
        PhongMaterial material = new PhongMaterial(Color.CORNFLOWERBLUE);
        cube.setMaterial(material);

        Group root = new Group(cube);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-500);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.BLACK);
        scene.setCamera(camera);

        stage.setTitle("Projet Astra");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
