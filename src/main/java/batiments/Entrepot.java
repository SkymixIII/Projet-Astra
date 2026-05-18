package batiments;

import java.util.HashMap;
import java.util.Map;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import ressources.Stock;
import ressources.TypeRessource;

/**
 * Gère le stockage physique des ressources de la colonie.
 * Hérite de Batiment.
 */
public class Entrepot implements Batiment {
    private String nom;
    private int x, y;
    private int niveau = 1;
    private int capaciteMax;
    
    // Stockage interne de l'entrepot
    private Map<TypeRessource, Integer> stockInterne;

    public Entrepot(String nom, int x, int y, int capaciteMax) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.capaciteMax = capaciteMax;
        this.stockInterne = new HashMap<>();
        // Initialisation à 0 
        for (TypeRessource type : TypeRessource.values()) {
            stockInterne.put(type, 0);
        }
    }

    /**
     * Ajoute une ressource au stock de l'entrepôt si la capacité le permet.
     * @param type Le type de ressource.
     * @param quantite La quantité à ajouter.
     */
    public void stocker(TypeRessource type, int quantite) {
		if (quantite < 0) return; // on peut pas stocker une quantité négative
        if (type != null && isOperationnel() && (getVolumeActuel() + quantite <= capaciteMax)) {
            int cumul = stockInterne.getOrDefault(type, 0) + quantite;
            stockInterne.put(type, cumul);
        }
    }

    /** Calcule la somme totale des ressources stockées ici. */
    public int getVolumeActuel() {
        int total = 0;
        for (int q : stockInterne.values()) {
            total += q;
        }
        return total;
    }

    @Override
    public boolean isOperationnel() {
        return true; // Un entrepôt marche toujours ig
    }

    @Override
    public TypeBatiment getType() {
        return TypeBatiment.ENTREPOT;
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    public String getNom() { return nom; }

    /* Méthodes à décommenter pour la V2 
    @Override public int getNiveau() { return niveau; }
    @Override public void ameliorer() { 
        this.niveau++; 
        this.capaciteMax *= 2; 
    }
    @Override public int getConsommationEnergie() { return 5 * niveau; }
    @Override public int getProductionEnergie() { return 0; }
    */

    @Override public void affecterPersonnel(Ouvrier o) { /* Pas de personnel nécessaire pour stocker */ }
    @Override public void retirerPersonnel(Ouvrier o) { /* Idem */}
    @Override public boolean aDeLaPlace() { return true; } // un entrepot a toujours de la place

    @Override
    public void mettreAJour(Stock stockGlobal, int tempsEcoule) {
        // jcrois ça sert pas à grand chose de mettre à jour un entrepot ?
    }

    @Override
    public double distance(Item autreItem) {
        return Math.sqrt(Math.pow(autreItem.getX() - this.x, 2) + Math.pow(autreItem.getY() - this.y, 2));
    }

    @Override public void deplacer(int x, int y) { this.x = x; this.y = y; }
    @Override public void deplacer(Direction dir) { /* bouge pas */ }

    public int getQuantite(TypeRessource type) {
        return stockInterne.getOrDefault(type, 0);
    }
}
