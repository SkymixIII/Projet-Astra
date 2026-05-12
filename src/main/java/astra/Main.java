package astra;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

import jeu.Jeu;

/**
 * Point d'entrée minimal : construit le modèle, instancie le {@link RenduMonde}
 * et appelle son {@code update()} à chaque frame. Toute la complexité
 * graphique (terrain, ouvriers, étiquettes, collisions, caméra, inputs) est
 * encapsulée derrière cette seule classe.
 */
public class Main extends Application {

    private static final int WIDTH  = 1280;
    private static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        Jeu jeu = new Jeu();
        jeu.creationMonde();

        RenduMonde rendu = new RenduMonde(jeu, WIDTH, HEIGHT);

        stage.setTitle("Projet Astra");
        stage.setScene(rendu.getScene());
        stage.show();
        rendu.getScene().getRoot().requestFocus();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                rendu.update();
            }
        }.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
