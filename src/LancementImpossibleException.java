/**
 * Exception levée lorsqu'une tentative de lancement de la fusée échoue parce
 * que les conditions ne sont pas remplies.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class LancementImpossibleException extends Exception {

    public LancementImpossibleException(String message) {
        super(message);
    }

    public LancementImpossibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
