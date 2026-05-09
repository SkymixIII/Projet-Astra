package ressources;

/**
 * Interface commune à tous les métiers.
 *
 * Chaque métier :
 * - possède un type (enum TypeMetier)
 * - peut travailler dans certains bâtiments
 * - possède une action principale
 *
 * Cette interface permet d'éviter les gros switch/case
 * partout dans le code.
 */
public interface Metier {

    /**
     * Retourne le type du métier.
     */
    Ouvrier.TypeMetier getType();

    /**
     * Vérifie si ce métier peut travailler
     * dans le bâtiment donné.
     */
    boolean peutTravaillerDans(Batiment batiment);

    /**
     * Action principale du métier.
     *
     * Exemple :
     * - Mineur -> extrait minerai/pierre
     * - Bucheron -> coupe du bois
     * - Technicien -> fabrique des ressources transformées
     */
    void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks);

    /**
     * Nom affiché dans l'UI.
     */
    String getNomAffichage();


    boolean peutTravaillerDans(Batiment batiment);
}