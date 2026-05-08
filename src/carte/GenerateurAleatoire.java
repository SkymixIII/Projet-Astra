package carte;

import java.util.Random;
import ressources.TypeRessource;
import batiments.LieuDeRessource;
import jeu.Difficulte;

/**
 * Responsable de la génération procédurale des ressources sur la carte.
 * Remplit les cases vides avec des objets de type LieuDeRessource.
 */
public class GenerateurAleatoire {
    private Random random;

    public GenerateurAleatoire() {
        this.random = new Random();
    }

    /**
     * Méthode principale de génération pour initialiser la carte
     * @param carte La carte à remplir
     */
    public void genererMonde(Carte carte) {
        // Définition des densités (pourcentage de couverture de la carte)
        placerGisement(carte, TypeRessource.FER, 0.05);         // 5% de fer
        placerGisement(carte, TypeRessource.SILICIUM, 0.04);    // Sable/Terres rares
        placerGisement(carte, TypeRessource.PETROLE, 0.03);     // Pétrole
        placerGisement(carte, TypeRessource.EAU, 0.06);         // Sources d'eau
        placerGisement(carte, TypeRessource.BOIS, 0.08);        // Forêts
    }

    /**
     * Place des gisements d'un type spécifique de manière aléatoire
     */
    private void placerGisement(Carte carte, TypeRessource type, double densite) {
        int totalCases = carte.getLargeur() * carte.getLongueur();
        int nbGisements = (int) (totalCases * densite);

        for (int i = 0; i < nbGisements; i++) {
            int x = random.nextInt(carte.getLargeur());
            int y = random.nextInt(carte.getLongueur());

            Case tile = carte.getTile(x, y, 0);

            // On ne place une ressource que si la case est vide
            if (tile != null && !tile.estOccupee()) {
                int quantiteInitiale = 5 + random.nextInt(15); // Entre 5 et 20 unités

                LieuDeRessource gisement = new LieuDeRessource(type, x, y, quantiteInitiale);
                tile.ajouter(gisement);
            } else {
                // Si la case est occupée, on tente de placer ailleurs (décrémenter i pour compenser)
                i--;
            }
        } 
    }

    /**
     * Tirage d'un évènement aléatoire en fonction de la difficulté
     * @param diff Le mode de jeu choisi
     */
    public Evenement tirageEvenement(Difficulte diff) {
        float chance = random.nextFloat();
        float seuil = (diff == Difficulte.DIFFICILE) ? 0.2f : 0.05f;

        if (chance < seuil) {
            // Logique de création d'une Tempête ou Grève
            return new Tempete(1 + random.nextInt(5));
        }

        return null;
    }
}
