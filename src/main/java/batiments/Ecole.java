package batiments;

import java.util.ArrayList;
import java.util.List;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import ressources.Stock;

/**
 * L'École accueille les chercheurs pour booster leur expérience au fil du temps.
 */
public class Ecole implements Batiment {
    private String nom;
    private int x, y;
    private float progressionRecherche;
    
    private List<Ouvrier> eleves = new ArrayList<>();
    private static final int CAPACITE_MAX = 5;

    public Ecole(String nom, int x, int y, float progressionRecherche) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.progressionRecherche = progressionRecherche;
    }

    /**
     * À chaque temps, l'école enseigne aux élèves présents
     * et fait progresser leur expérience.
     */
    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        if (!isOperationnel()) return;
        // On fait travailler les ouvriers qui sont dedans
        for (Ouvrier ouvrier : eleves) {
            if (ouvrier.getMetier() != null && "CHERCHEUR".equals(ouvrier.getMetier().getNom())) {
                // L'expérience évolue selon progressionRecherche
                int bonusTicks = (int) (tempsEcoule * (1.0f + progressionRecherche));
                ouvrier.travailler(bonusTicks);
            }
        }
    }

    @Override
    public void affecterPersonnel(Ouvrier ouvrier) {
        if (aDeLaPlace() && !eleves.contains(ouvrier)) {
            eleves.add(ouvrier);
            ouvrier.affecterPoste(this); 
        }
    }

    @Override
    public void retirerPersonnel(Ouvrier ouvrier) {
        if (eleves.remove(ouvrier)) {
            ouvrier.affecterPoste(null);
        }
    }

    @Override
    public boolean aDeLaPlace() {
        return eleves.size() < CAPACITE_MAX;
    }

    @Override
    public boolean isOperationnel() {
        return true; 
    }

    @Override
    public TypeBatiment getType() {
        return TypeBatiment.ECOLE;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    public String getNom() { return nom; }

    @Override
    public void deplacer(int x, int y) { this.x = x; this.y = y; }

    @Override
    public void deplacer(Direction dir) { /* Immobile ig */ }

    @Override
    public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }
}