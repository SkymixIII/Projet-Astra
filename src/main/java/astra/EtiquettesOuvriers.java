package astra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;

import carte.Carte;
import entites.Ouvrier;

/**
 * Étiquettes 2D billboard au-dessus de chaque ouvrier (nom + détails : niveau,
 * état, moral, faim, soif). Toujours lisibles, jamais tournées, cachées quand
 * l'ouvrier sort du champ.
 *
 * Sync dynamique : à chaque {@link #miseAJour()}, on compare la liste
 * d'ouvriers du modèle avec les cartes existantes, on crée/supprime ce qu'il
 * faut, puis on reprojette toutes les positions vers l'écran en inversant les
 * transforms de la caméra (camRotY⁻¹ puis camRotX⁻¹, dans cet ordre).
 */
public class EtiquettesOuvriers {

    /** Marge en pixels entre le sommet de la tête et le bas de l'étiquette. */
    private static final double MARGE_PX     = 4.0;
    private static final double LARGEUR_BARRE = 80;

    private static final String STYLE_CARTE =
              "-fx-background-color: rgba(0, 0, 0, 0.7);"
            + "-fx-padding: 4 8 4 8;"
            + "-fx-background-radius: 4;";
    private static final String STYLE_NOM =
              "-fx-text-fill: white;"
            + "-fx-font-size: 11px;"
            + "-fx-font-weight: bold;"
            + "-fx-font-family: 'Monospaced';";
    private static final String STYLE_SOUS =
              "-fx-text-fill: #cccccc;"
            + "-fx-font-size: 10px;"
            + "-fx-font-family: 'Monospaced';";
    private static final String STYLE_LETTRE =
              "-fx-text-fill: white;"
            + "-fx-font-size: 9px;"
            + "-fx-font-family: 'Monospaced';"
            + "-fx-min-width: 32;";
    private static final String STYLE_VALEUR =
              "-fx-text-fill: #cccccc;"
            + "-fx-font-size: 9px;"
            + "-fx-font-family: 'Monospaced';"
            + "-fx-min-width: 24;";

    private final Carte                   carte;
    private final List<Ouvrier>           ouvriers;     // référence vivante au modèle
    private final Map<Ouvrier, CarteOuvrier> cartes = new HashMap<>();

    private final PerspectiveCamera camera;
    private final Rotate camRotX;
    private final Rotate camRotY;

    private final double largeur;
    private final double hauteur;
    private final double aspect;
    private final double tanDemiFov;

    private final Group overlay;

    private boolean modeDetaille = false;

    public EtiquettesOuvriers(Carte carte, List<Ouvrier> ouvriers,
                              PerspectiveCamera camera, Rotate camRotX, Rotate camRotY,
                              double largeur, double hauteur) {
        this.carte    = carte;
        this.ouvriers = ouvriers;
        this.camera   = camera;
        this.camRotX  = camRotX;
        this.camRotY  = camRotY;
        this.largeur  = largeur;
        this.hauteur  = hauteur;
        this.aspect   = largeur / hauteur;
        this.tanDemiFov = Math.tan(Math.toRadians(camera.getFieldOfView()) / 2.0);
        this.overlay = new Group();
    }

    /** Le groupe 2D à placer à la racine de la {@code Scene}, au-dessus de la SubScene. */
    public Group getOverlay() {
        return overlay;
    }

    /** Bascule l'affichage compact (nom seul) / détaillé (toutes les infos). */
    public void toggleModeDetaille() {
        setModeDetaille(!modeDetaille);
    }

    public void setModeDetaille(boolean detaille) {
        this.modeDetaille = detaille;
        for (CarteOuvrier c : cartes.values()) c.setModeDetaille(detaille);
    }

    public boolean isModeDetaille() {
        return modeDetaille;
    }

    /** À appeler chaque frame : synchronise les cartes avec le modèle, puis reprojette. */
    public void miseAJour() {
        synchroniser();
        reprojeter();
    }

    // ------------------------------------------------------------------ //
    //  Synchronisation modèle ↔ cartes                                    //
    // ------------------------------------------------------------------ //

    private void synchroniser() {
        Set<Ouvrier> presents = new HashSet<>(ouvriers);

        for (Ouvrier o : ouvriers) {
            CarteOuvrier c = cartes.get(o);
            if (c == null) {
                c = new CarteOuvrier(o);
                c.setModeDetaille(modeDetaille);
                cartes.put(o, c);
                overlay.getChildren().add(c.node);
            }
        }

        cartes.entrySet().removeIf(e -> {
            if (!presents.contains(e.getKey())) {
                overlay.getChildren().remove(e.getValue().node);
                return true;
            }
            return false;
        });
    }

    // ------------------------------------------------------------------ //
    //  Reprojection monde → écran                                         //
    // ------------------------------------------------------------------ //

    private void reprojeter() {
        double yawRad   = Math.toRadians(-camRotY.getAngle());
        double pitchRad = Math.toRadians(-camRotX.getAngle());
        double cy = Math.cos(yawRad),   sy = Math.sin(yawRad);
        double cx = Math.cos(pitchRad), sx = Math.sin(pitchRad);

        double camX = camera.getTranslateX();
        double camY = camera.getTranslateY();
        double camZ = camera.getTranslateZ();

        for (Ouvrier o : ouvriers) {
            CarteOuvrier c = cartes.get(o);
            if (c == null) continue;
            c.miseAJour(o);

            // Ancre = sommet de la tête, recalculée chaque frame (le modèle peut bouger).
            double ancX = CoordMonde.worldXCentre(o.getX(), carte);
            double ancZ = CoordMonde.worldZCentre(o.getY(), carte);
            double ancY = CoordMonde.solY(carte, o.getX(), o.getY())
                        - RenduOuvriers.HAUTEUR_CORPS - 2 * RenduOuvriers.RAYON_TETE;

            double dx = ancX - camX;
            double dy = ancY - camY;
            double dz = ancZ - camZ;

            // transforms = [camRotY, camRotX]  ⇒  local→monde = camRotY · camRotX
            // donc monde→local = camRotX⁻¹ · camRotY⁻¹ : on applique yaw⁻¹ d'abord.
            double x1 =  cy * dx + sy * dz;
            double y1 =  dy;
            double z1 = -sy * dx + cy * dz;

            double x2 = x1;
            double y2 = cx * y1 - sx * z1;
            double z2 = sx * y1 + cx * z1;

            if (z2 <= 0.001) {
                c.node.setVisible(false);
                continue;
            }

            double ndcX = x2 / (z2 * tanDemiFov * aspect);
            double ndcY = y2 / (z2 * tanDemiFov);
            double pxX = (ndcX + 1) * 0.5 * largeur;
            double pxY = (ndcY + 1) * 0.5 * hauteur;

            double lw = c.node.getWidth();
            double lh = c.node.getHeight();
            c.node.setTranslateX(pxX - lw / 2.0);
            c.node.setTranslateY(pxY - lh - MARGE_PX);

            boolean horsEcran = pxX < -lw || pxX > largeur + lw
                             || pxY < -lh || pxY > hauteur + lh;
            c.node.setVisible(!horsEcran);
        }
    }

    // ------------------------------------------------------------------ //
    //  Carte d'infos par ouvrier                                          //
    // ------------------------------------------------------------------ //

    private static final class CarteOuvrier {
        final VBox        node;
        final Label       sousTitre;
        final ProgressBar barMoral;
        final ProgressBar barFaim;
        final ProgressBar barSoif;
        final Label       valMoral;
        final Label       valFaim;
        final Label       valSoif;

        CarteOuvrier(Ouvrier o) {
            Label nom = new Label(o.getNom());
            nom.setStyle(STYLE_NOM);

            this.sousTitre = new Label();
            this.sousTitre.setStyle(STYLE_SOUS);

            this.barMoral = miniBarre("#e0c040");
            this.barFaim  = miniBarre("#e08040");
            this.barSoif  = miniBarre("#40b0e0");

            this.valMoral = valeur();
            this.valFaim  = valeur();
            this.valSoif  = valeur();

            HBox ligneMoral = ligne("Moral", barMoral, valMoral);
            HBox ligneFaim  = ligne("Faim",  barFaim,  valFaim);
            HBox ligneSoif  = ligne("Soif",  barSoif,  valSoif);

            this.node = new VBox(2, nom, sousTitre, ligneMoral, ligneFaim, ligneSoif);
            this.node.setStyle(STYLE_CARTE);
            this.node.setMouseTransparent(true);
            setModeDetaille(false);
        }

        void setModeDetaille(boolean detaille) {
            // Première ligne (nom) toujours visible.
            for (int i = 1; i < node.getChildren().size(); i++) {
                Node enfant = node.getChildren().get(i);
                enfant.setVisible(detaille);
                enfant.setManaged(detaille);
            }
        }

        void miseAJour(Ouvrier o) {
            sousTitre.setText(o.getNiveau() + " · " + o.getEtat());
            barMoral.setProgress(o.getMoral());
            barFaim .setProgress(o.getFaim());
            barSoif .setProgress(o.getSoif());
            valMoral.setText(pourcent(o.getMoral()));
            valFaim .setText(pourcent(o.getFaim()));
            valSoif .setText(pourcent(o.getSoif()));
        }

        private static ProgressBar miniBarre(String couleurAccent) {
            ProgressBar b = new ProgressBar(0);
            b.setPrefSize(LARGEUR_BARRE, 6);
            b.setMinHeight(6);
            b.setMaxHeight(6);
            b.setStyle("-fx-accent: " + couleurAccent + ";");
            return b;
        }

        private static Label valeur() {
            Label l = new Label();
            l.setStyle(STYLE_VALEUR);
            return l;
        }

        private static HBox ligne(String libelle, ProgressBar b, Label val) {
            Label lab = new Label(libelle);
            lab.setStyle(STYLE_LETTRE);
            HBox h = new HBox(4, lab, b, val);
            h.setAlignment(Pos.CENTER_LEFT);
            return h;
        }

        private static String pourcent(double r) {
            int p = (int) Math.round(Math.max(0, Math.min(1, r)) * 100);
            return p + "%";
        }
    }
}
