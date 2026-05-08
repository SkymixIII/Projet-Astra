
import java.util.Random;

public class Fusee {

    /* L'état du propulseur en pourcentage (ex: 0.5f pour 50%, 1.0f pour 100%) */
    float etatPropulseur = 0.0f;
    /* L'état de la charge utile en pourcentage*/
    float etatChargeUtile = 0.0f;
    /* L'état des commandes en pourcentage*/
    float etatCommande = 0.0f;

    // Attribut pour l'équipage
    boolean ingenieurABord = false;

    /**
     * La fonction responsable de l'évaluation du succès du lancement de la
     * fusée.
     *
     * @return probSucces la probabilité de succès (la moyenne de l'assemblage)
     */
    float calculerProbabiliteSucces() {
        float probSucces = (this.etatPropulseur + this.etatChargeUtile + this.etatCommande) / 3.0f;
        return probSucces;
    }

    /**
     * La fonction de gestion du lancement de la fusée.
     *
     * @return true si le lancement réussit, false sinon.
     */
    boolean lancer() {
        // 1. On récupère la moyenne calculée par la méthode juste au-dessus
        float probabilite = this.calculerProbabiliteSucces();

        // 2. les 3 modules doivent être à 100% assemblés.
        // Si les 3 modules sont à 1.0f, alors leur moyenne sera exactement de 1.0f !
        boolean modulesPrets = false;
        if (probabilite >= 1.0f) {
            modulesPrets = true;
        }

        // 3. On vérifie si l'ingénieur est à bord.
        // Si les modules sont prêts ET que l'ingénieur est là, c'est gagné !
        if (modulesPrets == true && this.ingenieurABord == true) {
            System.out.println("Décollage réussi ! C'est une victoire !");
            return true;
        } else {
            System.out.println("Lancement impossible : la fusée n'est pas finie ou l'équipage est absent.");
            return false;
        }
    }
}
