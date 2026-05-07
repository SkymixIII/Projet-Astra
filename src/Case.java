import java.util.ArrayList;
import java.util.List;

/**
 * Représente une unité élémentaire de la Carte.
 * Une case peut contenir plusieurs objets de type Item (Joueur, Ouvrier, Batiment, etc.)
 */
public class Case {
    // Liste des éléments présents sur cette case précise
    private List<Item> contenu;

    /**
     * Constructeur initialisant une case vide
     */
    public Case() {
        this.contenu = new ArrayList<>();
    }

    /**
     * Ajoute un élément sur la case
     * @param item L'objet (Batiment, Ouvrier, etc.) à ajouter
     */
    public void ajouter(Item item) {
        if (item != null) {
            this.contenu.add(item);
        }
    }

    /**
     * Retire un élément de la case
     * @param item l'objet à supprimer
     */
    public void supprimer(Item item) {
        this.contenu.remove(item);
    }

    /**
     * Vérifie si la case contient au moins un élément.
     * @return true si la case n'est pas vide
     */
    public boolean estOccupee() {
        return !this.contenu.isEmpty();
    }

    /**
     * Retourne la liste de tous les items présents sur la case
     * Utile pour l'affichage ou pour interagir avec les objets d'une case
     */
    public List<Item> getContenu() {
        return new ArrayList<>(this.contenu);
    }
}