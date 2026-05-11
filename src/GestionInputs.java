import java.util.HashSet;
import java.util.Set;

import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;

public class GestionInputs {

    private static final double VITESSE_DEPLACEMENT = 5.0;
    private static final double VITESSE_ROTATION = 2.0;

    private final Set<KeyCode> pressed = new HashSet<>();
    private final PerspectiveCamera camera;
    private final Rotate camRotX;
    private final Rotate camRotY;

    private final double initX;
    private final double initY;
    private final double initZ;

    public GestionInputs(Scene scene, PerspectiveCamera camera, Rotate camRotX, Rotate camRotY) {
        this.camera = camera;
        this.camRotX = camRotX;
        this.camRotY = camRotY;
        this.initX = camera.getTranslateX();
        this.initY = camera.getTranslateY();
        this.initZ = camera.getTranslateZ();

        scene.setOnKeyPressed(e -> pressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));
    }

    public void miseAJour() {
        Point3D forward = camRotY.transform(new Point3D(0, 0, 1));
        Point3D right   = camRotY.transform(new Point3D(1, 0, 0));

        double dx = 0, dy = 0, dz = 0;
        if (pressed.contains(KeyCode.Z)) { dx += forward.getX(); dz += forward.getZ(); }
        if (pressed.contains(KeyCode.S)) { dx -= forward.getX(); dz -= forward.getZ(); }
        if (pressed.contains(KeyCode.D)) { dx += right.getX();   dz += right.getZ(); }
        if (pressed.contains(KeyCode.Q)) { dx -= right.getX();   dz -= right.getZ(); }
        if (pressed.contains(KeyCode.SPACE)) dy -= 1;
        if (pressed.contains(KeyCode.SHIFT)) dy += 1;

        if (dx != 0 || dy != 0 || dz != 0) {
            camera.setTranslateX(camera.getTranslateX() + dx * VITESSE_DEPLACEMENT);
            camera.setTranslateY(camera.getTranslateY() + dy * VITESSE_DEPLACEMENT);
            camera.setTranslateZ(camera.getTranslateZ() + dz * VITESSE_DEPLACEMENT);
        }

        if (pressed.contains(KeyCode.LEFT))  camRotY.setAngle(camRotY.getAngle() - VITESSE_ROTATION);
        if (pressed.contains(KeyCode.RIGHT)) camRotY.setAngle(camRotY.getAngle() + VITESSE_ROTATION);
        if (pressed.contains(KeyCode.UP))    camRotX.setAngle(camRotX.getAngle() - VITESSE_ROTATION);
        if (pressed.contains(KeyCode.DOWN))  camRotX.setAngle(camRotX.getAngle() + VITESSE_ROTATION);

        if (pressed.contains(KeyCode.R)) {
            camera.setTranslateX(initX);
            camera.setTranslateY(initY);
            camera.setTranslateZ(initZ);
            camRotX.setAngle(0);
            camRotY.setAngle(0);
        }
    }
}
