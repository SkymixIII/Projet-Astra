package batiments;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import ressources.Ressource;
import ressources.Stock;
import ressources.TypeRessource;
import exceptions.StockException;

/**
 * La Station de Pompage extrait l'eau pour approvisionner la colonie.
 * Elle fonctionne de manière continue à chaque temps de jeu.
 */
public class StationPompage implements Batiment {
    private String nom;
    private int x, y;
    private int debitEau;
    
    // Accumulateur de temps pour le pompage automatique
    private double tempsAccumule = 0.0;
    private static final int INTERVALLE_POMPAGE_SEC = 10;

    public StationPompage(String nom, int x, int y, int debitEau) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.debitEau = debitEau;
    }

    /**
     * Toutes les 10 secondes, elle effectue un pompage automatique vers le stock global
     */
    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        if (!isOperationnel()) return;
        this.tempsAccumule += tempsEcoule;
        if (this.tempsAccumule >= INTERVALLE_POMPAGE_SEC) {
            try {
                stock.ajouter(TypeRessource.EAU, this.debitEau);
                this.tempsAccumule = 0.0; // on remet le cycle à 0
                System.out.println("[Station " + nom + "] Pompage automatique de " + debitEau + " unités d'eau.");
            } catch (StockException e) {
                // Si les entrepôts sont pleins, le pompage se stop
                System.err.println("[Station " + nom + "] Impossible de stocker l'eau (entrepôts pleins) : " + e.getMessage());
            }
        }
    }

    /**
     * Permet un pompage manuel ou forcé par le système.
     * @return Une ressource d'EAU dont la quantité est égale au debitEau.
     */
    public Ressource pomper() {
        if (!isOperationnel()) {
            return null; 
        }
        return new Ressource(TypeRessource.EAU, this.debitEau); // pas besoin d'ouvriers
    }

    @Override
    public boolean isOperationnel() {
        return true; // elel marche toujours jsp
    }

    @Override
    public TypeBatiment getType() {
        return TypeBatiment.STATION_POMPAGE;
    }

    @Override public void affecterPersonnel(Ouvrier ouvrier) { /* Optionnel V2 */ }
    @Override public void retirerPersonnel(Ouvrier ouvrier) { /* Optionnel V2 */ }
    @Override public boolean aDeLaPlace() { return true; }
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    public String getNom() { return nom; }
    @Override public void deplacer(int x, int y) { this.x = x; this.y = y; }
    @Override public void deplacer(Direction dir) { /* Immobile */ }
    @Override public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }

    public int getDebitEau() { return debitEau; }
}