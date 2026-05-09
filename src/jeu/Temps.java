package jeu;

/**
 * Gère le temps interne du jeu (ticks).
 *
 * Cycle complet : 2400 ticks
 *   - Matin      :    0 – 899   (900 ticks)
 *   - Après-midi :  900 – 1799  (900 ticks)
 *   - Nuit       : 1800 – 2399  (600 ticks)
 */
public class Temps {

    private int tick = 0;

    /** Avance d'un tick. Repart à 0 après un cycle complet (2400 ticks). */
    public void augmenterHeure() {
        tick = (tick + 1) % Jeu.TICKS_JOURNEE;
    }

    /**
     * Retourne true si le tick courant se situe dans la plage nocturne
     * (ticks 1800 à 2399 inclus).
     */
    public boolean estNuit() {
        return tick >= (Jeu.TICKS_DEMI_JOURNEE * 2); // >= 1800
    }

    /** Retourne le tick courant dans le cycle journalier. */
    public int getTick() {
        return tick;
    }
}
