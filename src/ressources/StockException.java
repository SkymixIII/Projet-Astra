package ressources;

/**
 * Exception levée quand une opération sur le stock est impossible.
 * Hérite de RessourceInsuffisanteException (votre binôme 1).
 */
public class StockException extends RessourceInsuffisanteException {

    public StockException(String message) {
        super(message);
    }
}