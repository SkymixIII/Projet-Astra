import java.util.HashSet;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.AmbientLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final double MOVE_SPEED = 5.0;

    @Override
    public void start(Stage stage) {
        double s = 100, t = 1;
        Box front  = coloredFace(s, s, t, Color.RED);     front.setTranslateZ(-s/2);
        Box back   = coloredFace(s, s, t, Color.GREEN);   back.setTranslateZ(s/2);
        Box left   = coloredFace(t, s, s, Color.BLUE);    left.setTranslateX(-s/2);
        Box right  = coloredFace(t, s, s, Color.YELLOW);  right.setTranslateX(s/2);
        Box top    = coloredFace(s, t, s, Color.MAGENTA); top.setTranslateY(-s/2);
        Box bottom = coloredFace(s, t, s, Color.CYAN);    bottom.setTranslateY(s/2);

        Group cube = new Group(front, back, left, right, top, bottom);
        cube.getTransforms().addAll(new Rotate(25, Rotate.Y_AXIS), new Rotate(20, Rotate.X_AXIS));

        AmbientLight ambient = new AmbientLight(Color.color(0.7, 0.7, 0.7));

        Group root = new Group(cube, ambient);

        Rotate camRotX = new Rotate(0, Rotate.X_AXIS);
        Rotate camRotY = new Rotate(0, Rotate.Y_AXIS);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(camRotY, camRotX);
        camera.setTranslateZ(-500);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        camera.setFieldOfView(60);

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.GRAY);
        scene.setCamera(camera);

        Set<KeyCode> pressed = new HashSet<>();
        scene.setOnKeyPressed(e -> pressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                Point3D forward = camRotY.transform(camRotX.transform(new Point3D(0, 0, 1)));
                Point3D right   = camRotY.transform(camRotX.transform(new Point3D(1, 0, 0)));

                double dx = 0, dy = 0, dz = 0;
                if (pressed.contains(KeyCode.Z)) { dx += forward.getX(); dy += forward.getY(); dz += forward.getZ(); }
                if (pressed.contains(KeyCode.S)) { dx -= forward.getX(); dy -= forward.getY(); dz -= forward.getZ(); }
                if (pressed.contains(KeyCode.D)) { dx += right.getX();   dy += right.getY();   dz += right.getZ(); }
                if (pressed.contains(KeyCode.Q)) { dx -= right.getX();   dy -= right.getY();   dz -= right.getZ(); }

                if (dx != 0 || dy != 0 || dz != 0) {
                    camera.setTranslateX(camera.getTranslateX() + dx * MOVE_SPEED);
                    camera.setTranslateY(camera.getTranslateY() + dy * MOVE_SPEED);
                    camera.setTranslateZ(camera.getTranslateZ() + dz * MOVE_SPEED);
                }

                if (pressed.contains(KeyCode.LEFT))  camRotY.setAngle(camRotY.getAngle() - 2);
                if (pressed.contains(KeyCode.RIGHT)) camRotY.setAngle(camRotY.getAngle() + 2);
                if (pressed.contains(KeyCode.UP))    camRotX.setAngle(camRotX.getAngle() - 2);
                if (pressed.contains(KeyCode.DOWN))  camRotX.setAngle(camRotX.getAngle() + 2);

                if (pressed.contains(KeyCode.R)) {
                    camera.setTranslateX(0);
                    camera.setTranslateY(0);
                    camera.setTranslateZ(-500);
                    camRotX.setAngle(0);
                    camRotY.setAngle(0);
                }
            }
        }.start();

        stage.setTitle("Projet Astra");
        stage.setScene(scene);
        stage.show();
        scene.getRoot().requestFocus();
    }

    private static Box coloredFace(double w, double h, double d, Color c) {
        Box b = new Box(w, h, d);
        b.setMaterial(new PhongMaterial(c));
        return b;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
