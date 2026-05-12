package batiments;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import ressources.Stock;

/**
 * Gère la progression technologique (et aide pour développer patrons conception
 * La vitesse de recherche dépend du nombre d'ouvriers et de l'efficacité du bâtiment
 */
public class CentreRecherche implements Batiment {
    private String nom;
    private int x, y;
    private float vitesseRecherche;
    private int niveau = 1;
    
    // Pourcentage de progression accumulé (utile surtout pour la v2)
    private float progressionTechno = 0.0f;
    
    private java.util.List<Ouvrier> chercheurs = new java.util.ArrayList<>();
    private static final int CAPACITE_MAX = 5;

    public CentreRecherche(String nom, int x, int y, float vitesseRecherche) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.vitesseRecherche = vitesseRecherche;
    }

    /**
     * Contribue à l'avancement technologique.
     * @param pourcentage Valeur de base de la contribution.
     */
    public void contribuerRecherche(float pourcentage) {
        if (pourcentage > 0 && isOperationnel() && !chercheurs.isEmpty()) {
            // on ajoute les boost dû à la vitesse de recherche et le nombre de chercheurs
            float gain = pourcentage * vitesseRecherche * chercheurs.size();
            this.progressionTechno += gain;
        }
    }

    @Override
    public boolean isOperationnel() {
      // supposé opérationnel s'il y a au moins une personne dedans
        return !chercheurs.isEmpty();
    }

    @Override
    public TypeBatiment getType() {
        return TypeBatiment.CENTRE_RECHERCHE;
    }

    @Override
    public void affecterPersonnel(Ouvrier o) {
        if (aDeLaPlace() && !chercheurs.contains(o)) {
            chercheurs.add(o);
        }
    }

    @Override
    public void retirerPersonnel(Ouvrier o) {
        chercheurs.remove(o);
    }

    @Override
    public boolean aDeLaPlace() {
        return chercheurs.size() < CAPACITE_MAX;
    }

    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        // Permet de calculer le nombre de points de recherche
        if (isOperationnel()) {
            contribuerRecherche((float) tempsEcoule / 100.0f);
        }
    }

    @Override 
    public int getX() {
      return x; 
    }
  
    @Override 
    public int getY() {
      return y; 
    }
  
    public String getNom() {
      return nom;
    }
  
    public float getProgressionTechno() {
      return progressionTechno;
    }

    @Override public void deplacer(int x, int y) {
      this.x = x; 
      this.y = y; 
    }
  
    @Override public void deplacer(Direction dir) { /* Immobile ig */ }
  
    @Override 
    public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }
}
