package ressources;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère l'hébergement et le repos des ouvriers.
 * Indispensable pour éviter l'état FATIGUE des travailleurs.
 */
public class Maison implements Batiment {
    private String nom;
    private int x, y;
    private int niveau = 1;
    private int capaciteMax = 4; // Capacité d'hébergement par défaut (ptetre plus jsp ....)
    
    private List<Ouvrier> occupants = new ArrayList<>();

    public Maison(String nom, int x, int y, int capaciteMax) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.capaciteMax = capaciteMax;
    }

    /**
     * Loge un ouvrier pour qu'il puisse se reposer.
     * @param ouvrier L'ouvrier à héberger.
     */
    public void loger(Ouvrier ouvrier) {
        if (ouvrier != null && isOperationnel() && aDeLaPlace()) {
            if (!occupants.contains(ouvrier)) {
                occupants.add(ouvrier); // l'ouvrier occupe la maison
            }
        }
    }

    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        for (Ouvrier o : occupants) {
            o.seReposer(tempsEcoule); 
        }
    }

    @Override
    public boolean aDeLaPlace() {
        return occupants.size() < capaciteMax;
    }

    @Override
    public boolean isOperationnel() {
        return true; // Une maison est toujours opérationnelle si elle existe techniquement
    }

    @Override 
    public void affecterPersonnel(Ouvrier o) { 
        loger(o); 
        }
    @Override 
    public void retirerPersonnel(Ouvrier o) { 
        occupants.remove(o); 
        }
    
    @Override public int getX() { 
        return x; 
        }
    @Override public int getY() { 
        return y; 
        }

    public String getNom() {
        return nom;
        }

    @Override 
    public int getNiveau() {
        return niveau;
        }

    @Override public void ameliorer() {
        this.niveau++; this.capaciteMax += 2;
        }
    @Override public int getConsommationEnergie() { return 2 * niveau; }
    @Override public int getProductionEnergie() { return 0; }

    @Override
    public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }

    @Override 
    public void deplacer(int x, int y) { /* immobile donc jsp */ }
}