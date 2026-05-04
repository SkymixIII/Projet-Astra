package Code;

public class Ouvrier implements Item {
    private Role role;
    private EtatOuvrier etat;
    private boolean disponibilite = true;
    private float experience = 0.0f;

    // Besoins vitaux
    private int faim = 0;   // 0 = la vie est belle, 100 = je meurs
    private int soif = 0;
    private int repos = 100; // 100 = cool
    private int moral = 100;

    // Coordonnées
    private int x, y;

    public Ouvrier(Role role) {
        this.role = role;
        this.etat = EtatOuvrier.TRAVAIL;
    }

    // JOB

    /**
     * Lie l'ouvrier à un bâtiment (Usine, École, etc.)
     */
    public void affecterPoste(Batiment batiment) {
        if (batiment != null && batiment.isOperationnel() && this.disponibilite) {
            batiment.affecterPersonnel(this); 
        }
    }

    // BESOIN

    public void mettreAJourBesoins() {
        // Décrémentation/Incrémentation des besoins à chaque journée
        this.faim += 5;
        this.soif += 5;
        this.repos -= 2;
        this.moral -= 1;
        if (this.faim >= 100 || this.soif >= 100 || this.repos <= 0) {
            this.etat = EtatOuvrier.MALADE;
        }
    }

    // GETTER/SETTER
    
    public boolean isDisponible() { return disponibilite; }
    public void setDisponibilite(boolean disponibilite) { this.disponibilite = disponibilite; }
    
    @Override
    public double distance(Item autreItem) {
        return Math.sqrt(Math.pow(autreItem.getX() - this.x, 2) + Math.pow(autreItem.getY() - this.y, 2));
    }
    
    @Override public void deplacer(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
    
}
