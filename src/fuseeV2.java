import java.util.Random;

// La V2 de la classe fusée
public class fuseeV2 {
    /* L'état du propulseur en pourcentage */
    float etatPropulseur;
    /* L'état de la charge utile en pourcentage*/
    float etatChargeUtile;
    /* L'état des commandes en pourcentage*/
    float etatCommande;

    /** La fonction responsable du calcul de la
    probabilité de succès du lancement de la fusée*/
    float calculerProbabiliteSucces(){
        float probSucces;
        float qualiteKero;
        int qualiteDecollage;
        int nbrTechnologieDebloquees;
        int nbrTechnologie;
        int malusMoral
        //TODO : Implémenter l'intégration de chimie avancée
        Random r = new Random();
        qualite_kero = r.nextInt(100)/100;
        forceDecollage = 0;
        expMoy = 1.5; // TODO : mettre en place le calcul de la moyenne des ouvriers
        nbrTechnologieDebloquees = 4; // TODO : récupérer cet attribut
        nbrTechnologie = 8; // TODO : récupérer cet attribut
        //TODO : récupérer Moral avec moral un float
        if (Moral > 0.8) {
            malusMoral = 0;
        }
        else if (Moral > 0.6) {
            malusMoral = 6;
        }
        else if (Moral > 0.5) {
            malusMoral = 12;
        }
        else {
            malusMoral = 15;
        }
        // Calcul de la probabilité de succès du lancement de la fusée
        probSucces = base(40) + qualiteKero * 10 + forceDecollage + (expMoy * 10) + (nbrTechnolgieDebloquees / nbrTechnologies) * 20 − malusMoral
        return probSucces;
    }

    /** Fonction de gestion du lancement de la
     * fusée.
     *
     * @return
     */
    boolean lancer{
        //TODO Théo
    }
}