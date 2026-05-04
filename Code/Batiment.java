package code;

public interface Batiment extends Item {

    // État du bâtiment (utile pour faire évoluer certains batiments)
    boolean isOperationnel();
    int getNiveau();
    void ameliorer();
    
    // Gestion du Personnel
    void affecterPersonnel(Ouvrier ouvrier);
    void retirerPersonnel(Ouvrier ouvrier);
    boolean aDeLaPlace(); // Utile pour savoir si on peut affecter un ouvrier

    // Gestion de l'Énergie
    int getConsommationEnergie();
    int getProductionEnergie();

    // Logique de temps
    void mettreAJour(Stock stock, int tempsEcoule);

}
