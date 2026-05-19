package astra;

/**
 * Launcher de contournement pour le JAR, qui délègue à l'application JavaFX principale.
 * Cela permet d'éviter les problèmes de chargement de classes JavaFX lors de l'exécution du JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        // On appelle le main de l'application JavaFX sans hériter de Application
        astra.Main.main(args);
    }
}