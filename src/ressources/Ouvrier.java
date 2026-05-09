package ressources;

/**
 * Représente un travailleur de la colonie.
 * Son efficacité dépend de son expérience et de ses besoins vitaux.
 */
public class Ouvrier implements Item {

    public enum NiveauExp {
        DEBUTANT (0.7,  600),
        APPRENTI (0.9,  1200),
        CONFIRME (1.2,  1800),
        EXPERT   (1.5,  3000),
        MAITRE   (2.0,  Integer.MAX_VALUE); // niveau max, jamais dépassé

        public final double multiplicateur;
        public final int ticksPourMonter;

        NiveauExp(double multiplicateur, int ticksPourMonter) {
            this.multiplicateur = multiplicateur;
            this.ticksPourMonter = ticksPourMonter;
        }

        public double getMultiplicateur() {
            return multiplicateur;
        }

        public int getTicksPourMonter() {
            return ticksPourMonter;
        }
    }

    public enum EtatOuvrier {
        TRES_FATIGUE (0.3),
        FATIGUE      (0.8),
        NORMAL       (1.0),
        MOTIVE       (1.2),
        TRES_MOTIVE  (1.7);

        public final double multiplicateur;

        EtatOuvrier(double multiplicateur) {
            this.multiplicateur = multiplicateur;
        }
    }

    public enum TypeMetier {
        MINEUR,
        BUCHERON,
        MACON,
        FERMIER,
        TECHNICIEN,
        INGENIEUR
    }
    
    private String nom;
    private int x, y;
    
    // Système d'expérience
    private NiveauExp niveau;
    private int ticksAccumules;

    // Etat de l'ouvrier
    private EtatOuvrier etat;

    /*Le compteur dexpérience repart à 0 au changement de métier.
    * null = ouvrier non spécialisé (état de départ des 6 ouvriers initiaux).
    */
    private Metier metier;
    private Batiment posteActuel;

    // -------------------------------------------------------------------------
    // CONSTRUCTEUR
    // -------------------------------------------------------------------------

    public Ouvrier(String nom, int x, int y) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.etat = EtatOuvrier.NORMAL;
        this.niveau = NiveauExp.DEBUTANT;
        this.ticksAccumules = 0;
        this.metier = null;
        this.posteActuel = null;
    }

    // Calcule l'efficacité réelle utilisée par l'usine. Formule : efficacité = expérience * état

     public double getEfficacite() {
        return niveau.multiplicateur * etat.multiplicateur;
    }

    //Compte le nombre d'heure de travail d'un ouvirer pour le farie progresser
    public void travailler(int ticks) {
        if (metier == null) return; // pas de métier = pas d'expérience

        ticksAccumules += ticks;

        // Progression séquentielle : on monte d'un niveau si le seuil est atteint
        if (niveau != NiveauExp.MAITRE && ticksAccumules >= niveau.ticksPourMonter) {
            ticksAccumules = 0; // repart à 0 pour le niveau suivant
            niveau = NiveauExp.values()[niveau.ordinal() + 1];
        }
    }

    //Affecte l'ouvrier à un bâtiment.
    public void affecterPoste(Batiment batiment) {
        this.posteActuel = batiment;
    }

    /**
     * Assigne un nouveau métier.
     *
     * Si changement de métier :
     * - expérience remise à zéro
     * - retour niveau débutant
     */
    public void setMetier(Metier nouveauMetier) {
        if (nouveauMetier == null) {
            this.metier = null;
        } else {
            // Changement de TYPE de métier → remise à zéro de l'expérience
            if (this.metier == null
                    || this.metier.getType() != nouveauMetier.getType()) {
                this.ticksAccumules = 0;
                this.niveau         = NiveauExp.DEBUTANT;
            }
            this.metier = nouveauMetier;
        }
    }

     /**
     * Modifie l'état de l'ouvrier.
     */
    public void setEtat(EtatOuvrier etat) {
        this.etat = etat;
    }

    // --- Implémentation de Item ---

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }

    public NiveauExp getNiveau() { return niveau; }
    public Metier    getMetier() { return metier; }
    public Batiment  getPosteActuel() { return posteActuel; }
    
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
    public EtatOuvrier getEtat() { return etat; }


}