import java.util.Random;

public class fusee {
    /* L'état du propulseur en pourcentage */
    float etatPropulseur;
    /* L'état de la charge utile en pourcentage*/
    float etatChargeUtile;
    /* L'état des commandes en pourcentage*/
    float etatCommande;

    /** La fonction responsable de l'évaluation de la
     * du succès du lancement de la fusée.
     * @return probSucces la probabilité de succès
     */
    float calculerProbabiliteSucces() {
        // L' équipage reste à implanter certainement comme attribut de fusee
        probSucces = (this.etatPropulseur + this.etatChargeUtile + this.etatCommande) / 3;
        return probSucces;
    }

    /** La fonction de gestion du lancement de la
     * fusée.
     * @return
     */
    boolean lancer(){
        //TODO Théo
    }
}