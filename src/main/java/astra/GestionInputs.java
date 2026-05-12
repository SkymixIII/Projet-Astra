package astra;

import java.util.HashSet;
import java.util.Set;

import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;

public class GestionInputs {

    private static final double VITESSE_DEPLACEMENT = 1.5;
    private static final double VITESSE_ROTATION = 2.0;

    // Physique verticale. En JavaFX, +Y pointe vers le bas : la gravité est
    // donc positive et le saut applique une vitesse négative.
    // On exprime gravité et saut en "cases / s²" et "cases / s", puis on
    // convertit en unités monde via ECHELLE_HAUTEUR.
    // Gravité asymétrique : plus forte à la redescente qu'à la montée pour un
    // saut plus sec et une chute plus pesante.
    private static final double GRAVITE_MONTEE    = 40.0 * GestionCollisions.ECHELLE_HAUTEUR;
    private static final double GRAVITE_DESCENTE  = 40.0 * GestionCollisions.ECHELLE_HAUTEUR;
    private static final double VITESSE_INITIALE_SAUT = 9.5 * GestionCollisions.ECHELLE_HAUTEUR;
    private static final double DT_MAX = 0.05;

    private final Set<KeyCode> pressed = new HashSet<>();
    private final PerspectiveCamera camera;
    private final Rotate camRotX;
    private final Rotate camRotY;
    private final GestionCollisions collisions;

    private final double initX;
    private final double initY;
    private final double initZ;

    private double velociteY = 0;
    private boolean auSol = false;
    private boolean spaceDejaPresse = false;
    private long lastNanoTime = -1;

    public GestionInputs(Scene scene, PerspectiveCamera camera, Rotate camRotX, Rotate camRotY,
                         GestionCollisions collisions) {
        this.camera = camera;
        this.camRotX = camRotX;
        this.camRotY = camRotY;
        this.collisions = collisions;
        this.initX = camera.getTranslateX();
        this.initY = camera.getTranslateY();
        this.initZ = camera.getTranslateZ();

        scene.setOnKeyPressed(e -> pressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));
    }

    public void miseAJour(long now) {
        double dt = (lastNanoTime < 0) ? 0 : (now - lastNanoTime) / 1.0e9;
        lastNanoTime = now;
        if (dt > DT_MAX) dt = DT_MAX;

        Point3D forward = camRotY.transform(new Point3D(0, 0, 1));
        Point3D right   = camRotY.transform(new Point3D(1, 0, 0));

        double dx = 0, dz = 0;
        if (pressed.contains(KeyCode.Z)) { dx += forward.getX(); dz += forward.getZ(); }
        if (pressed.contains(KeyCode.S)) { dx -= forward.getX(); dz -= forward.getZ(); }
        if (pressed.contains(KeyCode.D)) { dx += right.getX();   dz += right.getZ(); }
        if (pressed.contains(KeyCode.Q)) { dx -= right.getX();   dz -= right.getZ(); }

        // Saut : déclenché uniquement au front montant de SPACE et si on est au sol.
        boolean spaceMaintenant = pressed.contains(KeyCode.SPACE);
        if (spaceMaintenant && !spaceDejaPresse && auSol) {
            velociteY = -VITESSE_INITIALE_SAUT;
            auSol = false;
        }
        spaceDejaPresse = spaceMaintenant;

        // Gravité appliquée à chaque frame, plus forte en redescente.
        double gravite = (velociteY >= 0) ? GRAVITE_DESCENTE : GRAVITE_MONTEE;
        velociteY += gravite * dt;
        double dy = velociteY * dt;

        double cx = camera.getTranslateX();
        double cy = camera.getTranslateY();
        double cz = camera.getTranslateZ();
        Point3D resolu = collisions.resoudreDeplacementCamera(
            cx, cy, cz,
            dx * VITESSE_DEPLACEMENT,
            dy,
            dz * VITESSE_DEPLACEMENT);
        camera.setTranslateX(resolu.getX());
        camera.setTranslateY(resolu.getY());
        camera.setTranslateZ(resolu.getZ());

        // Si on voulait descendre et qu'on n'a pas bougé en Y → on est au sol.
        // Si on voulait monter et qu'on est bloqué → on tape un plafond.
        boolean yBloque = (dy != 0) && (resolu.getY() == cy);
        if (yBloque && velociteY > 0) {
            auSol = true;
            velociteY = 0;
        } else if (yBloque && velociteY < 0) {
            velociteY = 0;
            auSol = false;
        } else {
            auSol = false;
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
            velociteY = 0;
            auSol = false;
        }
    }
}
