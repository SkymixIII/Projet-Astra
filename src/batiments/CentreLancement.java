package batiments;

import fusee.Fusee;
import entites.Ouvrier;
import carte.Item;
import ressources.Stock;

/**
 * Bâtiment qui permet de lancer la fusée pour terminer le jeu.
 */
public class CentreLancement implements Batiment {
    private String nom; // non utile mais nice 
    private int x, y;
    private int niveau = 1; // niveau de base ; utile si jamais on peut upgrade le centre
    private Fusee fusee;

    public CentreLancement(String nom, int x, int y) {
        this.nom = nom;
        this.x = x;
        this.y = y;
    }

    /**
     * Installe la fusée terminée sur le pas de tir 
     */
    public void setFusee(Fusee f) {
        this.fusee = f;
    }

    /**
     * Déclenche le lancement de la fusée
     * @return true si le lancement est un succès, false si ça kaputt
     */
    public boolean lancerFusee() {
        if (isOperationnel() && fusee != null) {
            return fusee.lancer();
        }
        return false;
    }

    @Override
    public boolean isOperationnel() {
        // RAJOUTER LES CONDITIONS CAR JSP LESQUELLES 
        return true; 
    }

    // méthodes basiques en tas
    @Override 
    public int getX() {
        return x; 
    }

    @Override 
    public int getY() { 
        return y; 
    }
    
    @Override 
    public String getNom() { 
        return nom; 
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
        return 100; // à changer si nécessaire
    } 
    
    @Override 
    public int getProductionEnergie() { 
        return 0; 
    }
    
    @Override public void affecterPersonnel(Ouvrier o) {/* pas nécessaire */}
    @Override public void retirerPersonnel(Ouvrier o) {/* pas nécessaire */}
    @Override public boolean aDeLaPlace() { return true; /* puisque pas d'ouvrier, toujours vrai */ }
    @Override public void mettreAJour(Stock stock, int tempsEcoule) {/* pas nécessaire car pas de stock */}
    @Override public void deplacer(int x, int y) {/* immobile */}
    
    @Override public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }
}
