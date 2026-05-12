package entites;

import batiments.Batiment;
import carte.Item;
import carte.Direction;
import metiers.Metier;

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

	 // Besoins vitaux (doc p.2-3)
    private double  moral = 1.0;         // [0.0 – 1.0], source de vérité pour mettreAJourEtat
    private boolean aUnLit = false;
    private boolean aMangeEtBu = false;  // remis à false à chaque demi-journée

    // Jauges continues — augmentent quand un besoin n'est pas satisfait,
    // retombent à 0 quand l'ouvrier mange et boit.
    private double faim = 0.0;           // [0.0 = repu — 1.0 = affamé]
    private double soif = 0.0;           // [0.0 = hydraté — 1.0 = déshydraté]

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

    @Override
    public void deplacer(Direction direction) {
        this.x += direction.getDx();
        this.y += direction.getDy();
    }


    // Getters pour le debug dans TestUsine
    public String getNom() { return nom; }
    public EtatOuvrier getEtat() { return etat; }

	// -------------------------------------------------------------------------
    // BESOINS VITAUX & MORAL (appelés par Jeu)
    // -------------------------------------------------------------------------

    /** Identifiant lisible (nom) — alias demandé par Jeu. */
    public String getIdentifiant() { return nom; }

    /**
     * Signale un manque de nourriture : dégrade l'état d'un cran (doc p.3).
     * Marque également que les besoins ne sont pas satisfaits.
     */
    public void signalerManqueNourriture() {
        modifierMoral(-0.10);
        faim = Math.min(1.0, faim + 0.25);
        mettreAJourEtat();
        aMangeEtBu = false;
    }

    /**
     * Signale un manque d'eau : dégrade l'état d'un cran (doc p.3).
     */
    public void signalerManqueEau() {
        modifierMoral(-0.10);
        soif = Math.min(1.0, soif + 0.25);
        mettreAJourEtat();
        aMangeEtBu = false;
    }


    /**
     * Modifie le moral (borné [0.0 – 1.0]).
     * mettreAJourEtat() synchronise ensuite l'EtatOuvrier selon les seuils :
     *   moral >= 0.9 → TRES_MOTIVE  (×1.7)
     *   moral >= 0.7 → MOTIVE       (×1.2)
     *   moral >= 0.5 → NORMAL       (×1.0)
     *   moral >= 0.3 → FATIGUE      (×0.8)
     *   moral >= 0.1 → TRES_FATIGUE (×0.3)
     *   moral <  0.1 → MALADE       (×0.01)
     */
    public void modifierMoral(double delta) {
        moral = Math.max(0.0, Math.min(1.0, moral + delta));
    }

    public double getMoral() { return moral; }

    /**
     * Synchronise l'EtatOuvrier depuis le moral courant (doc p.3).
     * Appelé par Jeu après chaque modifierMoral().
     */
    public void mettreAJourEtat() {
        if      (moral >= 0.9) etat = EtatOuvrier.TRES_MOTIVE;
        else if (moral >= 0.7) etat = EtatOuvrier.MOTIVE;
        else if (moral >= 0.5) etat = EtatOuvrier.NORMAL;
        else if (moral >= 0.3) etat = EtatOuvrier.FATIGUE;
        else if (moral >= 0.1) etat = EtatOuvrier.TRES_FATIGUE;
        else                   etat = EtatOuvrier.MALADE;
    }

    /**
     * Récupération nocturne : améliore l'état d'un cran après une nuit complète (doc p.3).
     * Réinitialise le flag aMangeEtBu pour la demi-journée suivante.
     */
    public void recupererNuit() {
        modifierMoral(+0.10);
        mettreAJourEtat();
        aMangeEtBu = false;
    }

    /** @return true si les besoins (nourriture + eau) ont été satisfaits cette demi-journée. */
    public boolean aMangeEtBu() { return aMangeEtBu; }

    /** Appelé par Joueur après distribution réussie nourriture + eau. */
    public void setAMangeEtBu(boolean valeur) {
        this.aMangeEtBu = valeur;
        if (valeur) { // besoins satisfaits → jauges remises à zéro
            this.faim = 0.0;
            this.soif = 0.0;
        }
    }

    public double getFaim() { return faim; }
    public double getSoif() { return soif; }

    /** @return true si l'ouvrier dispose d'un lit. */
    public boolean aUnLit() { return aUnLit; }

    /** Mis à jour par Joueur selon les lits disponibles. */
    public void setAUnLit(boolean valeur) { this.aUnLit = valeur; }

}
