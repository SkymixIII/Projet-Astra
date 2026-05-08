package code;

import java.util.List;

/**
 * Classe orchestratrice principale.
 * Gère la boucle de jeu, la production et les besoins vitaux des ouvriers.
 *
 * Constantes (doc p.1) :
 *   1 tick            = 1 seconde réelle
 *   900 ticks         = 1 demi-journée (matin ou après-midi)
 *   600 ticks         = 1 nuit
 *   1 journée complète = 900 + 900 + 600 = 2400 ticks
 */
public class Jeu {

    // ------------------------------------------------------------------ //
    //  Constantes globales                                               //
    // ------------------------------------------------------------------ //

    public static final int TICKS_DEMI_JOURNEE = 900;
    public static final int TICKS_NUIT         = 600;
    public static final int TICKS_JOURNEE      = 2400; // matin + après-midi + nuit

    // ------------------------------------------------------------------ //
    //  Attributs                                                          //
    // ------------------------------------------------------------------ //

    private Joueur   joueur;
    private Carte    carte;
    private EventBus bus;
    private Fusee    fusee;
    private Temps    temps;
    private Age      age;

    /** Compteur interne de ticks dans la demi-journée courante (0 à 899). */
    private int ticksDemiJournee = 0;

    /** Compteur interne de ticks dans la nuit courante (0 à 599). */
    private int ticksNuit = 0;

    // ------------------------------------------------------------------ //
    //  Point d'entrée                                                     //
    // ------------------------------------------------------------------ //

    public static void main(String[] args) {
        Jeu projetAstra = new Jeu();
        System.out.println("Lancement du projet Astra...");
    }

    // ------------------------------------------------------------------ //
    //  Boucle principale                                                  //
    // ------------------------------------------------------------------ //

    /**
     * processTick — appelée à chaque tick (1 seconde réelle).
     *
     * Ordre d'exécution :
     *   1. Avancer le temps
     *   2. Production (uniquement matin/après-midi)
     *   3. Besoins vitaux (une fois par demi-journée)
     *   4. Récupération et traitement des commandes
     *   5. Vérification des événements (TODO EventBus)
     *   6. Vérification de la victoire (si fusée assemblée)
     */
    public void processTick() {

        // ── 1. Avancer le temps ──────────────────────────────────────── //
        temps.augmenterHeure();

        // ── 2. Production (bloquée la nuit) ───────────────── //
        if (!temps.estNuit()) {
            mettreAJourProduction();
            ticksDemiJournee++;

            // ── 3. Besoins vitaux — une fois par demi-journée ─ //
            // 900 ticks de travail écoulés = fin d'une demi-journée
            if (ticksDemiJournee >= TICKS_DEMI_JOURNEE) {
                consommerBesoinsVitaux();
                mettreAJourMoral();
                ticksDemiJournee = 0;
            }

        } else {
            // ── Nuit : récupération de fatigue ───────────────────────── //
            ticksNuit++;
            if (ticksNuit >= TICKS_NUIT) {
                recupererFatigue();
                ticksNuit = 0;
            }
        }

        // ── 4. Commandes utilisateur ─────────────────────────────────── //
        recupererCommande();
        traiterCommande();

        // ── 5. Événements (éventuellement à modifier) ─────── //
        bus.traiter(temps, joueur.getStock(), joueur.getOuvriers());

        // ── 6. Victoire (uniquement si la fusée est assemblée) ────────── //
        if (fusee != null && fusee.estAssemblee()) {
            verifierVictoire();
        }
    }

    // ------------------------------------------------------------------ //
    //  Production                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Parcourt tous les bâtiments et déclenche la production.
     * Appelée à chaque tick de travail (matin + après-midi).
     */
    public void mettreAJourProduction() {
        List<Batiment> batiments = joueur.getBatiments();

        for (Batiment b : batiments) {
            if (b instanceof Usine) {
                Usine u = (Usine) b;
                if (u.peutProduire()) {
                    try {
                        u.mettreAJour(joueur.getStock(), 1); // 1 tick écoulé
                    } catch (Exception e) {
                        System.err.println("[Production] " + e.getMessage());
                    }
                }
            } else if (b instanceof LieuDeRessource) {
                ((LieuDeRessource) b).extraire();
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Besoins vitaux — une fois par demi-journée (doc p.2)              //
    // ------------------------------------------------------------------ //

    /**
     * Consomme nourriture et eau pour chaque ouvrier.
     * Déclenché une fois par demi-journée travaillée (900 ticks).
     *
     * Consommation par ouvrier par demi-journée (doc p.2) :
     *   - Nourriture : 2 unités
     *   - Eau        : 3 unités
     */
    public void consommerBesoinsVitaux() {
        List<Ouvrier> equipage = joueur.getOuvriers();
        Stock stock = joueur.getStock();

        for (Ouvrier o : equipage) {
            // Nourriture
            try {
                stock.retirer(TypeRessource.NOURRITURE, 2);
            } catch (RessourceInsuffisanteException e) {
                o.signalerManqueNourriture(); // baisse moral -0.10 (doc p.3)
                System.err.println("[Besoins] Nourriture insuffisante pour " + o.getIdentifiant());
            }

            // Eau potable
            try {
                stock.retirer(TypeRessource.EAU_POTABLE, 3);
            } catch (RessourceInsuffisanteException e) {
                o.signalerManqueEau(); // baisse moral -0.10 (doc p.3)
                System.err.println("[Besoins] Eau insuffisante pour " + o.getIdentifiant());
            }

            // Mise à jour état de l'ouvrier selon son moral
            o.mettreAJourEtat();
        }
    }

    // ------------------------------------------------------------------ //
    //  Moral — une fois par demi-journée (doc p.3)                       //
    // ------------------------------------------------------------------ //

    /**
     * Applique les facteurs de montée/descente de moral (doc p.3).
     * Appelée en même temps que consommerBesoinsVitaux().
     */
    private void mettreAJourMoral() {
        List<Ouvrier> equipage = joueur.getOuvriers();
        int litsDisponibles    = joueur.getNombreLitsDisponibles();
        int totalOuvriers      = equipage.size();

        // Taux d'occupation des lits (doc p.3 : surpopulation > 95%)
        boolean surpopulation  = (double) totalOuvriers / Math.max(litsDisponibles, 1) > 0.95;

        for (Ouvrier o : equipage) {
            // Facteurs positifs (doc p.3)
            if (o.aMangeEtBu()) {
                o.modifierMoral(+0.05); // besoins satisfaits
            }
            if (o.aUnLit()) {
                o.modifierMoral(+0.02); // lit disponible
            }

            // Facteurs négatifs (doc p.3)
            if (!o.aUnLit()) {
                o.modifierMoral(-0.05); // pas de lit
            }
            if (surpopulation) {
                o.modifierMoral(-0.03); // surpopulation
            }

            // Mise à jour de l'état selon le nouveau moral
            o.mettreAJourEtat();
        }
    }

    // ------------------------------------------------------------------ //
    //  Nuit — récupération (doc p.2)                                     //
    // ------------------------------------------------------------------ //

    /**
     * Après une nuit complète (600 ticks), les ouvriers récupèrent de la fatigue.
     * Un ouvrier TRES_FATIGUE repasse à FATIGUE après 1 nuit + repas + eau (doc p.3).
     */
    private void recupererFatigue() {
        for (Ouvrier o : joueur.getOuvriers()) {
            o.recupererNuit(); // logique de récupération dans Ouvrier
        }
    }

    // ------------------------------------------------------------------ //
    //  Victoire (doc p.12-13)                                            //
    // ------------------------------------------------------------------ //

    /**
     * Vérifie si les conditions de lancement sont remplies.
     * Seuil minimum : prob >= 70 pour que le bouton soit actif (doc p.13).
     */
    private void verifierVictoire() {
        try {
            double prob = fusee.calculerProbabiliteSucces();
            if (prob >= 70) {
                System.out.println("[Victoire] Lancement possible ! Probabilité : " + prob + "%");
                // TODO : activer le bouton de lancement dans l'interface
            }
        } catch (LancementImpossibleException e) {
            // Conditions non remplies → bouton grisé, pas d'erreur critique
            System.out.println("[Fusée] " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Commandes (à compléter avec EventBus)                             //
    // ------------------------------------------------------------------ //

    /**
     * Écoute les entrées utilisateur.
     * à brancher sur l'EventBus quand il sera implémenté.
     *
     * Exemples de commandes à gérer :
     *   - Construire un bâtiment
     *   - Affecter un ouvrier
     *   - Lancer une recherche
     *   - Lancer la fusée
     */
    public void recupererCommande() {
        // TODO : Commande cmd = bus.prochaine(Canal.SYSTEME);
    }

    /**
     * Traite les commandes récupérées.
     * à compléter selon les types de commandes.
     */
    public void traiterCommande() {
        // TODO : switch sur le type de commande
        // case CONSTRUIRE  → joueur.construire(batiment, x, y)
        // case AFFECTER    → usine.affecterPersonnel(ouvrier)
        // case RECHERCHER  → arbreTechno.lancerRecherche(nom)
        // case LANCER      → fusee.lancer()
    }

    // ------------------------------------------------------------------ //
    //  Getters                                                            //
    // ------------------------------------------------------------------ //

    public Joueur   getJoueur() { return joueur; }
    public Carte    getCarte()  { return carte; }
    public Fusee    getFusee()  { return fusee; }
    public Age      getAge()    { return age; }
    public Temps    getTemps()  { return temps; }
}