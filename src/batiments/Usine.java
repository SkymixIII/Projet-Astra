package batiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import carte.Item;
import entites.Ouvrier;
import ressources.Stock;
import exception.RessourceInsuffisanteException;

/**
 * Gère la transformation des ressources et le personnel associé.
 * Une production prend 5 minutes réelles (300 ticks si 1s = 1 tick).
 */
public class Usine implements Batiment {

    private String nom;
    private int x, y;
    private int niveau = 1;
    
    // Type de ressource produite et sa recette associée
    private TypeRessource produitActuel;
    private Map<TypeRessource, Integer> recetteActuelle;
    
    private List<Ouvrier> personnel = new ArrayList<>();
    private static final int CAPACITE_MAX = 5;

    // Gestion de la progression (0.0 à 100.0)
    private double progresProduction = 0; 
    private static final int TEMPS_BASE_SEC = 300; // 5 min selon le GDD

    public Usine(String nom, int x, int y, TypeRessource produit, Map<TypeRessource, Integer> recette) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.produitActuel = produit;
        this.recetteActuelle = recette;
    }

    /**
     * Méthode appelée par la boucle principale du Jeu.
     * @param stock Le stock global du joueur.
     * @param tempsEcoule Temps en secondes depuis la dernière mise à jour.
     */
    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        if (!isOperationnel() || produitActuel == null) return;
        double efficaciteTotale = 0;
        for (Ouvrier o : personnel) {
            efficaciteTotale += o.getEfficacite();
        }
        double progressionPoints = ((double) tempsEcoule / TEMPS_BASE_SEC) * efficaciteTotale * 100;
        progresProduction += progressionPoints;
        if (progresProduction >= 100) {
            try {
                produire(produitActuel, 1, stock);
                progresProduction = 0; // Réinitialise pour l'objet suivant
            } catch (RessourceInsuffisanteException e) {
                progresProduction = 99.9;
                System.err.println("[Usine " + nom + "] Production bloquée : " + e.getMessage());
            }
        }
    }

    public void produire(TypeRessource type, int quantite, Stock stock) throws RessourceInsuffisanteException {
        if (!isOperationnel()) throw new RessourceInsuffisanteException("Usine non opérationnelle.");
        stock.consommerRecette(recetteActuelle);
        try {
            stock.ajouter(type, quantite);
        } catch (StockException e) {
            // Si le stock est plein (mode difficile), on lève une exception
            throw new RessourceInsuffisanteException("Stock plein : " + e.getMessage());
        }
    }

    @Override 
    public boolean isOperationnel() { 
        return !personnel.isEmpty(); 
    }

    @Override 
    public boolean aDeLaPlace() { 
        return personnel.size() < CAPACITE_MAX; 
    }

    @Override 
    public void affecterPersonnel(Ouvrier o) { 
        if (aDeLaPlace()) personnel.add(o); 
    }

    @Override 
    public void retirerPersonnel(Ouvrier o) { 
        personnel.remove(o);
    }

    @Override 
    public int getNiveau() {
        return niveau; 
    }

    @Override 
    public void ameliorer() {
        this.niveau++; 
    }

    @Override 
    public int getConsommationEnergie() {
        return 10 * niveau; // par contre ça consomme
    }
    
    @Override 
    public int getProductionEnergie() {
        return 0; // normalement ça produit pas d'énergie
    }
    
    @Override 
    public int getX() {
        return x; 
    }
    
    @Override 
    public int getY() {
        return y; 
    }
    
    @Override 
    public void deplacer(int x, int y) {
        this.x = x; this.y = y;
    }
    
    @Override 
    public double distance(Item autre) { 
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2)); 
    }
}
