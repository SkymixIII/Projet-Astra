package astra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import batiments.Batiment;
import carte.Carte;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;

/**
 * Etiquettes 2D billboard au-dessus de chaque bâtiment (Nom/Type)
 * Toujours lisibles à l'écran, calculés dynamiquement
 */
public class EtiquettesBatiment {

    /** Marge en pixels entre le haut du bâtiment (cheminée comprise) et le bas de l'étiquette */
    private static final double MARGE_PX = 6.0;
    private static final double LARGEUR_BARRE = 80;

    private static final String STYLE_CARTE =
        "-fx-background-color: rgba(25, 30, 35, 0.85);"
        + "-fx-padding: 4 8 4 8;"
        + "-fx-background-radius: 4;"
        + "-fx-border-color: #555555;"
        +  "-fx-border-radius: 4;"
        + "-fx-border-width: 0.5;";
    private static final String STYLE_NOM =
        "-fx-text-fill: #00ffcc;"
        + "-fx-font-size: 11px;"
        + "-fx-font-weight: bold;"
        + "-fx-font-family: 'Monospaced';";
    private static final String STYLE_SOUS =
        "-fx-text-fill: #aaaaaa;"
        + "-fx-font-size: 10px;"
        + "-fx-font-family: 'Monospaced';";
    private static final String STYLE_LETTRE =
        "-fx-text-fill: white;"
        + "-fx-font-size: 9px;"
        + "-fx-font-family: 'Monospaced';"
        + "-fx-min-width: 45;"; // Un peu plus large pour faire tenir 'Santé' et 'Staff'
    private static final String STYLE_VALEUR =
        "-fx-text-fill: #cccccc;"
        + "-fx-font-size: 9px;"
        + "-fx-font-family: 'Monospaced';"
        + "-fx-min-width: 30;";
    
    private final Carte carte;
    private final List<Batiment> batiments; // Référence vivante à la liste du modèle/joueur
    private final Map<Batiment, CarteBatiment> cartes = new HashMap<>();

    private final PerspectiveCamera camera;
    private final Rotate camRotX;
    private final Rotate camRotY;

    private final double largeur;
    private final double hauteur;
    private final double aspect;
    private final double tanDemiFov;

    private final Group overlay;

    private boolean modeDetaille = false;

    public EtiquettesBatiment(Carte carte, List<Batiment> batiments, 
                            PerspectiveCamera camera, Rotate camRotX, Rotate camRotY,
                            double largeur, double hauteur) {
        this.carte = carte;
        this.batiments = batiments;
        this.camera = camera;
        this.camRotX = camRotX;
        this.camRotY = camRotY;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.aspect = largeur / hauteur;
        this.tanDemiFov = Math.tan(Math.toRadians(camera.getFieldOfView()) / 2.0);
        this.overlay = new Group();
    }

    public Group getOverlay() {
        return overlay;
    }

    public void toggleModeDetaille() {
        setModeDetaille(!modeDetaille);
    }

    public void setModeDetaille(boolean detaille) {
        this.modeDetaille = detaille;
        for (CarteBatiment c : cartes.values()) c.setModeDetaille(detaille);
    }

    public boolean isModeDetaille() {
        return modeDetaille;
    }

    /** Appelé à chaque frame pour synchroniser et mettre à jour la production */
    public void miseAJour() {
        synchroniser();
        reprojeter();
    }

    private void synchroniser() {
        Set<Batiment> presents = new HashSet<>(batiments);

        for (Batiment b : batiments) {
            CarteBatiment c = cartes.get(b);
            if (c == null) {
                c = new CarteBatiment(b);
                c.setModeDetaille(modeDetaille);
                cartes.put(b, c);
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

    private void reprojeter() {
        double yawRad = Math.toRadians(-camRotY.getAngle());
        double pitchRad = Math.toRadians(-camRotX.getAngle());
        double cy = Math.cos(yawRad), sy = Math.sin(yawRad);
        double cx = Math.cos(pitchRad), sx = Math.sin(pitchRad);

        double camX = camera.getTranslateX();
        double camY = camera.getTranslateY();
        double camZ = camera.getTranslateZ();

        for (Batiment b : batiments) {
            CarteBatiment c = cartes.get(b);
            if (c == null) continue;
            c.miseAJour(b);

            // Calcul de l'ancre en haut de la structure (Toit de la box + Hauteur de la cheminée)
            double ancX = CoordMonde.worldXCentre(b.getX(), carte);
            double ancZ = CoordMonde.worldZCentre(b.getY(), carte);
            double ancY = CoordMonde.solY(carte, b.getX(), b.getY())
                        - RenduBatiments.HAUTEUR_BASE - RenduBatiments.HAUTEUR_CHEMINEE;
            
            double dx = ancX - camX;
            double dy = ancY - camY;
            double dz = ancZ - camZ;

            // Matrice monde vers local caméra
            double x1 = cy * dx + sy * dz;
            double y1 = dy;
            double z1 = -sy * dx + cy * dz;

            double x2 = x1;
            double y2 = cx * y1 - sx * z1;
            double z2 = sx * y1 + cx * z1;

            if (z2 <= 0.001) {
                c.node.setVisible(false);
                continue;
            }

            // Projection perspective sur le plan d'affichage 2D
            double ndcX = x2 / (z2 * tanDemiFov * aspect);
            double ndcY = y2 / (z2 * tanDemiFov);
            double pxX = (ndcX + 1) * 0.5 * largeur;
            double pxY = (ndcY + 1) * 0.5 * hauteur;

            // Scaling basé sur la distance : réduction de la taille avec la profondeur
            // z2 est la profondeur. On utilise une scaling progressive pour garder lisibilité
            double scale = Math.min(1.0, 50.0 / z2);
            scale = Math.max(scale, 0.6);

            double lw = c.node.getWidth() * scale;
            double lh = c.node.getHeight() * scale;
            c.node.setScaleX(scale);
            c.node.setScaleY(scale);
            c.node.setTranslateX(pxX - lw / 2.0);
            c.node.setTranslateY(pxY - lh / MARGE_PX);

            boolean horsEcran = pxX < - lw || pxX > largeur + lw
                                || pxY < -lh || pxY > hauteur + lh;
            c.node.setVisible(!horsEcran);
        }
    }

    // ------------------------------------------------------------------ //
    //  Sous-classe : Carte d'infos UI du bâtiment                       //
    // ------------------------------------------------------------------ //

    private static final class CarteBatiment {
        final VBox node;
        final Label sousTitre;
        final ProgressBar barSante;
        final ProgressBar barPersonnel;
        final Label valSante;
        final Label valPersonnel;

        CarteBatiment(Batiment b) {
            // Utilise le nom de la classe concrète (Usine, Entrepot, etc.) comme titre principal
            Label nom = new Label(b.getClass().getSimpleName().toUpperCase());
            nom.setStyle(STYLE_NOM);

            this.sousTitre = new Label();
            this.sousTitre.setStyle(STYLE_SOUS);

            this.barSante = miniBarre("#ff6b6b");
            this.valSante = new Label("--");
            this.valSante.setStyle(STYLE_VALEUR);

            this.barPersonnel = miniBarre("#4ecdc4");
            this.valPersonnel = new Label("--");
            this.valPersonnel.setStyle(STYLE_VALEUR);

            HBox santeBox = creerLigneStatut("Santé", barSante, valSante);
            HBox personnelBox = creerLigneStatut("Staff", barPersonnel, valPersonnel);

            this.node = new VBox(4);
            this.node.setStyle(STYLE_CARTE);
            this.node.getChildren().addAll(nom, sousTitre, santeBox, personnelBox);
            this.node.setVisible(false);
        }
    

        void setModeDetaille(boolean detaille) {
            // Affiche/masque les lignes de statut (index 2 et 3)
            if (node.getChildren().size() > 2) {
                node.getChildren().get(2).setVisible(detaille);
                node.getChildren().get(2).setManaged(detaille);
            }
            if (node.getChildren().size() > 3) {
                node.getChildren().get(3).setVisible(detaille);
                node.getChildren().get(3).setManaged(detaille);
            }
        }
    

        void miseAJour(Batiment b) {
            String statut = b.isOperationnel() ? "OPERATIONNEL" : "HS/EN PANNE";
            sousTitre.setText("Statut : " + statut);
        }
    }

    private static ProgressBar miniBarre(String couleurAccent) {
        ProgressBar b = new ProgressBar(0);
        b.setPrefSize(LARGEUR_BARRE, 6);
        b.setMinHeight(6);
        b.setMaxHeight(6);
        b.setStyle("-fx-accent: " + couleurAccent + ";");
        return b;
    }

    private static HBox creerLigneStatut(String label, ProgressBar barre, Label valeur) {
        Label lab = new Label(label);
        lab.setStyle(STYLE_LETTRE);
        HBox h = new HBox(4, lab, barre, valeur);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }
}
