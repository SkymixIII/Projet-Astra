package exceptions;

/**
 * Exception levée lorsqu'une opération nécessite plus de ressources
 * que ce que le stock du joueur contient.
 *
 * Utilisée par :
 *  - Stock.retirer()
 *  - Stock.consommerRecette()
 *  - Usine.tenterProduction()
 */
public class RessourceInsuffisanteException extends Exception {

    public RessourceInsuffisanteException(String message) {
        super(message);
    }

    public RessourceInsuffisanteException(String message, Throwable cause) {
        super(message, cause);
    }
}
