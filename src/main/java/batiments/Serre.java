package batiments;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import ressources.Ressource;
import ressources.Stock;
import ressources.TypeRessource;
import exceptions.StockException;

/**
 * La Serre produit de la nourriture de manière continue au fil du temps.
 */
public class Serre implements Batiment {
    private String nom;
    private int x, y;
    private int rendementNourriture;
    
    // Gestion de la pousse (de 0.0 à 100.0)
    private double progresPousse = 0.0;
    private static final int TEMPS_POUSSE_SEC = 60; 

    public Serre(String nom, int x, int y, int rendementNourriture) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.rendementNourriture = rendementNourriture;
    }

    /**
     * Simulation du temps : fait grandir les cultures à chaque tick.
     * Dès que la pousse atteint 100%, la nourriture est ajoutée au stock bc pourquoi pas
     */
    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        if (!isOperationnel()) return;
        double gainProgres = ((double) tempsEcoule / TEMPS_POUSSE_SEC) * 100.0;
        this.progresPousse += gainProgres;
        // Si les plantes ont fini de pousser, on récolte automatiquement
        if (this.progresPousse >= 100.0) {
            try {
                stock.ajouter(TypeRessource.NOURRITURE, this.rendementNourriture);
                this.progresPousse = 0.0; 
                System.out.println("[Serre " + nom + "] Récolte automatique de " + rendementNourriture + " unités de nourriture.");
            } catch (StockException e) {
                // Si l'entrepôt global est plein, la production se bloque
                this.progresPousse = 99.0;
                System.err.println("[Serre " + nom + "] Stock plein, récolte en attente : " + e.getMessage());
            }
        }
    }

    /**
     * Récolte manuelle.
     */
    public Ressource recolter() {
        if (!isOperationnel()) return null;
        return new Ressource(TypeRessource.NOURRITURE, this.rendementNourriture);
    }

    @Override
    public boolean isOperationnel() {
        return true; // La serre fonctionne toujours
    }

    @Override
    public TypeBatiment getType() {
        return TypeBatiment.SERRE;
    }

    @Override public void affecterPersonnel(Ouvrier ouvrier) {}
    @Override public void retirerPersonnel(Ouvrier ouvrier) {}
    @Override public boolean aDeLaPlace() { return true; }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    public String getNom() { return nom; }
    @Override public void deplacer(int x, int y) { this.x = x; this.y = y; }
    @Override public void deplacer(Direction dir) { /* Immobile ig */ }
    @Override public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }

    // Getter au cas où jsp
    public double getProgresPousse() { return progresPousse; }
}