package batiments;

import java.util.Random;

import entites.Ouvrier;
import entites.EtatOuvrier;
import ressources.Stock;
import carte.Direction;
import carte.Item;

/**
 * Gère le soin des ouvriers malades.
 * L'efficacité des soins détermine la probabilité de guérison.
 */
public class Hopital implements Batiment {
    private String nom;
    private int x, y;
    private int niveau = 1;
    private float efficaciteSoins; // on va dire entre 0% et 100% 
    private Random random = new Random();

    public Hopital(String nom, int x, int y, float efficaciteSoins) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.efficaciteSoins = efficaciteSoins;
    }

    /**
     * Tente de soigner un ouvrier.
     * @param ouvrier L'ouvrier à réparer.
     */
    public void soigner(Ouvrier ouvrier) {
        if (ouvrier != null && ouvrier.getEtat().equals(EtatOuvrier.MALADE) && isOperationnel()) {
            if (random.nextFloat() <= efficaciteSoins) {
                ouvrier.setEtat(EtatOuvrier.NORMAL); // Il retourne au travail en étant normal
            }
        }
    }

    @Override
    public boolean isOperationnel() {
        return true; 
    }

	/* Ces méthodes sont a décommenter pour la V2
    @Override 
    public int getConsommationEnergie() {
        return 15 * niveau;
        }

    @Override 
    public int getProductionEnergie() {
        return 0;
        }

    @Override 
    public int getNiveau() {
        return niveau;
        }
    @Override 
    public void ameliorer() { 
        this.niveau++; this.efficaciteSoins += 0.1; 
        }
	*/
    public String getNom() { 
        return nom; 
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
	public TypeBatiment getType() {
		return TypeBatiment.LIEUX_DE_REPOS;
	}

    @Override 
    public void affecterPersonnel(Ouvrier o) {
        // BOITE NOIIIIIRE
    }

    @Override 
    public void retirerPersonnel(Ouvrier o) {
        // c'est une boite noire
    }

    @Override 
    public boolean aDeLaPlace() { 
        // ici on suppose qu'un hopital a toujours de la place ?
        return true;
    }

    @Override 
    public void mettreAJour(Stock stock, int tempsEcoule) {
        // l'hopital évolue pas je pense 
    }

    @Override 
    public void deplacer(int x, int y) {
        // ça se déplace pas un hopital jpense
    }

	@Override
	public void deplacer(Direction dir) { /* Immobile */ }

    @Override 
    public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }
}
