package code;

/**
 * Classe Temps — gère le cycle journalier de la simulation.
 *
 * Constantes :
 *   1 tick              = 1 seconde réelle
 *   900 ticks           = 1 demi-journée (matin ou après-midi)
 *   600 ticks           = 1 nuit
 *   1 journée complète  = 900 + 900 + 600 = 2400 ticks
 *
 * Cycle d'une journée :
 *   MATIN       -> 900 ticks -> travail normal
 *   APRES_MIDI  -> 900 ticks -> travail normal
 *   NUIT        -> 600 ticks -> sommeil automatique, fatigue récupérée
 */
public class Temps {

    // ------------------------------------------------------------------ //
    //  Constantes globales                                               //
    // ------------------------------------------------------------------ //

    public static final int TICKS_DEMI_JOURNEE = 900;
    public static final int TICKS_NUIT         = 600;
    public static final int TICKS_JOURNEE      = 2400;

    // ------------------------------------------------------------------ //
    //  Enum des phases                                                   //
    // ------------------------------------------------------------------ //

    public enum Phase {
        MATIN,      // ticks 0    → 899
        APRES_MIDI, // ticks 900  → 1799
        NUIT        // ticks 1800 → 2399
    }

    // ------------------------------------------------------------------ //
    //  Attributs                                                          //
    // ------------------------------------------------------------------ //

    /** Compteur de ticks dans la journée courante (0 à 2399). */
    private int ticksDansJournee;

    /** Numéro du jour depuis le début de la partie (commence à 1). */
    private int jourActuel;

    /** Nombre total de ticks depuis le début de la partie. */
    private int totalTicks;

    /** Phase courante de la journée. */
    private Phase phaseActuelle;

    // ------------------------------------------------------------------ //
    //  Constructeur                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Au démarrage, tous les ouvriers commencent en phase MATIN
     */
    public Temps() {
        this.ticksDansJournee = 0;
        this.jourActuel       = 1;
        this.totalTicks       = 0;
        this.phaseActuelle    = Phase.MATIN;
    }

    // ------------------------------------------------------------------ //
    //  Méthode principale                                                 //
    // ------------------------------------------------------------------ //

    /**
     * Avance d'un tick et met à jour la phase courante.
     * Appelée par Jeu.processTick() à chaque cycle.
     */
    public void augmenterHeure() {
        totalTicks++;
        ticksDansJournee++;

        // Fin de journée → on repart à 0
        if (ticksDansJournee >= TICKS_JOURNEE) {
            ticksDansJournee = 0;
            jourActuel++;
        }

        // Mise à jour de la phase selon la position dans la journée
        if (ticksDansJournee < TICKS_DEMI_JOURNEE) {
            phaseActuelle = Phase.MATIN;
        } else if (ticksDansJournee < TICKS_DEMI_JOURNEE * 2) {
            phaseActuelle = Phase.APRES_MIDI;
        } else {
            phaseActuelle = Phase.NUIT;
        }
    }

    // ------------------------------------------------------------------ //
    //  Méthodes de consultation                                           //
    // ------------------------------------------------------------------ //

    /** Retourne true si c'est la nuit — les ouvriers ne travaillent pas. */
    public boolean estNuit() {
        return phaseActuelle == Phase.NUIT;
    }

    /** Retourne true si les ouvriers peuvent travailler (matin ou après-midi). */
    public boolean estPeriodeDeTravail() {
        return phaseActuelle == Phase.MATIN || phaseActuelle == Phase.APRES_MIDI;
    }

    /**
     * Retourne true si on est à la fin d'une demi-journée.
     * Utilisé par Jeu pour déclencher consommerBesoinsVitaux() au bon moment.
     */
    public boolean estFinDemiJournee() {
        return ticksDansJournee == TICKS_DEMI_JOURNEE - 1       // fin du matin
            || ticksDansJournee == TICKS_DEMI_JOURNEE * 2 - 1;  // fin de l'après-midi
    }

    /**
     * Retourne true si on est à la fin de la nuit.
     * Utilisé par Jeu pour déclencher recupererFatigue().
     */
    public boolean estFinNuit() {
        return ticksDansJournee == TICKS_JOURNEE - 1;
    }

    // ------------------------------------------------------------------ //
    //  Accesseurs                                                         //
    // ------------------------------------------------------------------ //

    public Phase getPhase()            { return phaseActuelle; }
    public int   getJourActuel()       { return jourActuel; }
    public int   getTicksDansJournee() { return ticksDansJournee; }
    public int   getTotalTicks()       { return totalTicks; }

    @Override
    public String toString() {
        return String.format("Jour %d — %s [tick %d/%d]",
            jourActuel, phaseActuelle, ticksDansJournee, TICKS_JOURNEE);
    }
}