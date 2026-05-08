package ressources;

/**
 * Représente un travailleur de la colonie.
 * Son efficacité dépend de son expérience et de ses besoins vitaux.
 */
public class Ouvrier implements Item {
    private String nom;
    private int x, y;
    
    // Besoins vitaux (0 à 100)
    private double faim = 100.0;
    private double soif = 100.0;
    
    // Système d'expérience
    // Débutant (0,7) -> Apprenti (0,9) -> Confirmé (1,2) -> Expert (1,5) -> Maître (2)
    private double experienceCoeff = 0.7; 
    private int minutesTravaillees = 0;
    
    private String etat = "NORMAL"; // NORMAL, FATIGUE, MOTIVE
    private Batiment posteActuel;

    public Ouvrier(String nom, int x, int y) {
        this.nom = nom;
        this.x = x;
        this.y = y;
    }

    /**
     * Calcule l'efficacité réelle utilisée par l'usine.
     * Formule : efficacité = expérience * état
     */
    public double getEfficacite() {
        double multiplicateurEtat = 1.0;
        
        switch (etat) {
            case "FATIGUE": multiplicateurEtat = 0.8; break;
            case "TRES_FATIGUE": multiplicateurEtat = 0.3; break;
            case "MOTIVE": multiplicateurEtat = 1.2; break;
            case "TRES_MOTIVE": multiplicateurEtat = 1.7; break;
            default: multiplicateurEtat = 1.0; // NORMAL
        }
        
        return experienceCoeff * multiplicateurEtat;
    }

    /**
     * Simule la consommation de ressources et la fatigue.
     */
    public void consommerBesoins(Stock stock) {
        // Diminution passive des besoins
        faim -= 0.5;
        soif -= 0.8;

        // Mise à jour de l'état selon les besoins
        if (faim < 20 || soif < 20) {
            this.etat = "FATIGUE";
        } else if (faim <= 0 || soif <= 0) {
            this.etat = "TRES_FATIGUE";
        }
    }

    /**
     * Fait progresser l'expérience de l'ouvrier.
     */
    public void travailler(int minutes) {
        this.minutesTravaillees += minutes;
        
        // Logique de montée en niveau simplifiée 
        if (minutesTravaillees > 60 && experienceCoeff < 1.5) {
            experienceCoeff = 1.2; // Devient Confirmé
        }
    }

    public void affecterPoste(Batiment b) {
        this.posteActuel = b;
    }

    /**
     * Augmente le repos de l'ouvrier lorsqu'il est logé dans une maison.
     * @param tempsEcoule Temps passé à se reposer.
     */
    public void seReposer(int tempsEcoule) {
        // On restaure l'état à NORMAL dès qu'il passe par une maison.
        // Tu pourras complexifier en ajoutant une jauge de repos (0-100) plus tard.
        this.etat = "NORMAL"; 
        
        // Optionnel : on peut imaginer que le repos soigne un peu la faim/soif
        this.faim = Math.min(100.0, this.faim + (tempsEcoule * 0.05));
    }

    // --- Implémentation de Item ---

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    
    @Override 
    public double distance(Item autreItem) {
        return Math.sqrt(Math.pow(autreItem.getX() - this.x, 2) + Math.pow(autreItem.getY() - this.y, 2));
    }

    @Override 
    public void deplacer(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters pour le debug dans TestUsine
    public String getNom() { return nom; }
    public String getEtat() { return etat; }

    public void setEtat(String nouvelEtat) {
        this.etat = nouvelEtat;
    }

}