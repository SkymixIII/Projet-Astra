package batiments;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import ressources.TypeRessource;
import ressources.Stock;
/**
 * Représente un gisement de ressources (Mine, Forêt, Lac).
 * Contrairement à l'Usine, il ne transforme pas mais fournit une ressource brute.
 */
public class LieuDeRessource implements Batiment {
    private String nom;
    private int x, y;
    private TypeRessource type;
    private int quantite; // Quantité totale disponible dans le gisement
    private int niveau = 1;

    public LieuDeRessource(String nom, TypeRessource type, int quantite, int x, int y) {
        this.nom = nom;
        this.type = type;
        this.quantite = quantite;
        this.x = x;
        this.y = y;
    }

    /**
     * Extrait une quantité de ressource du gisement.
     * @param quantite Demandée par l'ouvrier ou le système.
     * @return La quantité réellement extraite.
     */
    public int exploiter(int quantiteDemandee) {
        if (quantiteDemandee <= 0) return 0;
        // On ne peut pas extraire plus que ce qui reste
        int extrait = Math.min(quantiteDemandee, this.quantite);
        this.quantite -= extrait;
        return extrait;
    }

    @Override
    public boolean isOperationnel() {
        return this.quantite > 0; // Un gisement épuisé n'est plus opérationnel
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    public String getNom() { return nom; }

	@Override
	public TypeBatiment getType() {
		return TypeBatiment.LIEU_DE_RESSOURCE;
	}
	/* Ces méthodes sont a décommenter pour la V2
    @Override public int getNiveau() { return niveau; }
    @Override public void ameliorer() { this.niveau++; }

    // Un gisement ne consomme généralement pas d'énergie au début, mais ptetre que oui si c'est profond
    @Override public int getConsommationEnergie() { return 0; }
    @Override public int getProductionEnergie() { return 0; }
	*/
    @Override public void affecterPersonnel(Ouvrier o) { /* Logique à ajouter si besoin */ }
    @Override public void retirerPersonnel(Ouvrier o) { /* Logique à ajouter si besoin */ }
    @Override public boolean aDeLaPlace() { return true; }

    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        // OFaudrait optimiser le truc si l'ouvrier est présent dedans
    }

    @Override
    public double distance(Item autreItem) {
        return Math.sqrt(Math.pow(autreItem.getX() - this.x, 2) + Math.pow(autreItem.getY() - this.y, 2));
    }

    @Override public void deplacer(int x, int y) { /* Immobile */ }

	@Override
	public void deplacer(Direction dir) { /* Immobile */ }

    public int getQuantiteRestante() { return quantite; }
}
