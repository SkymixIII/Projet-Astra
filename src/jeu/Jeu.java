package jeu;

import java.util.List;

import carte.Carte;
import entites.*;
import fusee.Fusee;
import ressources.*;
import batiments.*;
import exceptions.*;

/**
 * Classe orchestratrice principale.
 * Gère la boucle de jeu, la production et les besoins vitaux des ouvriers.
 *
 * Constantes (doc p.1) :
 *   1 tick             = 1 seconde réelle
 *   900 ticks          = 1 demi-journée (matin ou après-midi)
 *   600 ticks          = 1 nuit
 *   1 journée complète = 900 + 900 + 600 = 2400 ticks
 */
public class Jeu {

    // ------------------------------------------------------------------ //
    //  Constantes globales                                                //
    // ------------------------------------------------------------------ //

    public static final int TICKS_DEMI_JOURNEE = 900;
    public static final int TICKS_NUIT         = 600;
    public static final int TICKS_JOURNEE      = 2400; // matin + après-midi + nuit

    // ------------------------------------------------------------------ //
    //  Attributs                                                          //
    // ------------------------------------------------------------------ //

    private Joueur   joueur;
    private Carte    carte;
    private Eventbus bus;
    private Fusee    fusee;
    private Temps    temps;
    private Age      age;

    /** Compteur interne de ticks dans la demi-journée courante (0 à 899). */
    private int ticksDemiJournee = 0;

    /** Compteur interne de ticks dans la nuit courante (0 à 599). */
    private int ticksNuit = 0;

    // ------------------------------------------------------------------ //
    //  Constructeur                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Constructeur de la classe.
     * Initialise tous les composants du jeu.
     */
    public Jeu() {
        this.joueur = new Joueur();
        this.bus    = new Eventbus();
        this.fusee  = new Fusee();
        this.temps  = new Temps();
        this.age    = new Age();
        // this.carte est initialisé dans creationMonde()
    }

    // ------------------------------------------------------------------ //
    //  Point d'entrée                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Point d'entrée principal du simulateur.
     */
    public static void main(String[] args) {
        Jeu projetAstra = new Jeu();
        projetAstra.creationMonde();
        System.out.println("Lancement du projet Astra...");
    }

    // ------------------------------------------------------------------ //
    //  Création du monde                                                  //
    // ------------------------------------------------------------------ //

    /**
     * Méthode pour créer la map.
     * Génère le terrain, les reliefs et les gisements de ressources.
     */
    public void creationMonde() {
        this.carte = new Carte(100, 100, 5);

        // --- Spawn joueur ---
        carte.getTile(50, 50, 0).ajouter(new Joueur());

        // ============================================================
        // SOL z=0 : plaine partout avec île au sud-est
        // ============================================================
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {

                // Île organique au sud-est
                boolean ile =
                    (Math.pow(x - 82, 2) + Math.pow(y - 82, 2) < 25) || // centre
                    (Math.pow(x - 79, 2) + Math.pow(y - 85, 2) < 16) || // lobe sud-ouest
                    (Math.pow(x - 85, 2) + Math.pow(y - 79, 2) < 12);   // lobe nord-est

                boolean bordureIle =
                    (Math.pow(x - 82, 2) + Math.pow(y - 82, 2) < 40) ||
                    (Math.pow(x - 79, 2) + Math.pow(y - 85, 2) < 30) ||
                    (Math.pow(x - 85, 2) + Math.pow(y - 79, 2) < 25);

                TypeSol sol;
                if (ile) sol = TypeSol.SABLE;
                else if (bordureIle && !ile) sol = TypeSol.EAU;
                else sol = TypeSol.HERBE;

                carte.getTile(x, y, 0).ajouter(new Sol(sol));
            }
        }

        // ============================================================
        // SOL z=1 : collines (zone sud-ouest)
        // ============================================================
        for (int x = 22; x <= 45; x++) {
            for (int y = 28; y <= 48; y++) {
                if (carte.getTile(x, y, 1).estVide()) {
                    carte.getTile(x, y, 1).ajouter(new Sol(TypeSol.ROCHE));
                }
            }
        }

        // ============================================================
        // SOL z=2 : montagne (zone nord-ouest)
        // ============================================================
        for (int x = 8; x <= 30; x++) {
            for (int y = 10; y <= 32; y++) {
                if (carte.getTile(x, y, 2).estVide()) {
                    carte.getTile(x, y, 2).ajouter(new Sol(TypeSol.ROCHE_DURE));
                }
            }
        }

        // ============================================================
        // SOL z=3 : flancs des pics (cases voisines des sommets)
        // ============================================================
        carte.getTile(19, 10, 3).ajouter(new Sol(TypeSol.ROCHE_DURE));
        carte.getTile(21, 10, 3).ajouter(new Sol(TypeSol.ROCHE_DURE));
        carte.getTile(20,  9, 3).ajouter(new Sol(TypeSol.ROCHE_DURE));
        carte.getTile(24, 12, 3).ajouter(new Sol(TypeSol.ROCHE_DURE));
        carte.getTile(26, 12, 3).ajouter(new Sol(TypeSol.ROCHE_DURE));
        carte.getTile(25, 11, 3).ajouter(new Sol(TypeSol.ROCHE_DURE));

        // ============================================================
        // SOL z=4 : sommets enneigés
        // ============================================================
        carte.getTile(20, 10, 4).ajouter(new Sol(TypeSol.NEIGE));
        carte.getTile(25, 12, 4).ajouter(new Sol(TypeSol.NEIGE));

        // ============================================================
        // RESSOURCES
        // ============================================================

        // --- Forêts en plaine (z=0) ---
        carte.getTile(35, 45, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(40, 50, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(45, 55, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(55, 45, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(60, 50, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(50, 60, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(42, 42, 0).ajouter(new Gisement(TypeRessource.BOIS));
        carte.getTile(58, 58, 0).ajouter(new Gisement(TypeRessource.BOIS));

        // --- Fer x7 : montagne nord-ouest (z=2) ---
        carte.getTile(15, 20, 2).ajouter(new Gisement(TypeRessource.FER));
        carte.getTile(18, 22, 2).ajouter(new Gisement(TypeRessource.FER));
        carte.getTile(20, 18, 2).ajouter(new Gisement(TypeRessource.FER));
        carte.getTile(12, 25, 2).ajouter(new Gisement(TypeRessource.FER));
        carte.getTile(22, 15, 2).ajouter(new Gisement(TypeRessource.FER));
        carte.getTile(25, 20, 2).ajouter(new Gisement(TypeRessource.FER));
        carte.getTile(17, 28, 2).ajouter(new Gisement(TypeRessource.FER));

        // --- Silicium x3 : montagne nord-ouest (z=2) ---
        carte.getTile(25, 22, 2).ajouter(new Gisement(TypeRessource.SILICIUM));
        carte.getTile(15, 28, 2).ajouter(new Gisement(TypeRessource.SILICIUM));
        carte.getTile(16, 28, 2).ajouter(new Gisement(TypeRessource.SILICIUM));

        // --- Pierre x5 : collines (z=1) ---
        carte.getTile(30, 35, 1).ajouter(new Gisement(TypeRessource.PIERRE));
        carte.getTile(33, 38, 1).ajouter(new Gisement(TypeRessource.PIERRE));
        carte.getTile(28, 40, 1).ajouter(new Gisement(TypeRessource.PIERRE));
        carte.getTile(35, 32, 1).ajouter(new Gisement(TypeRessource.PIERRE));
        carte.getTile(38, 42, 1).ajouter(new Gisement(TypeRessource.PIERRE));

        // --- Pétrole x6 : enfoui en plaine (z=0) ---
        carte.getTile(60, 40, 0).ajouter(new GisementEnfoui(TypeRessource.PETROLE));
        carte.getTile(65, 45, 0).ajouter(new GisementEnfoui(TypeRessource.PETROLE));
        carte.getTile(62, 50, 0).ajouter(new GisementEnfoui(TypeRessource.PETROLE));
        carte.getTile(70, 42, 0).ajouter(new GisementEnfoui(TypeRessource.PETROLE));
        carte.getTile(68, 55, 0).ajouter(new GisementEnfoui(TypeRessource.PETROLE));
        carte.getTile(72, 48, 0).ajouter(new GisementEnfoui(TypeRessource.PETROLE));

        // --- Glace x2 : pics enneigés (z=4) ---
        carte.getTile(20, 10, 4).ajouter(new Gisement(TypeRessource.GLACE));
        carte.getTile(25, 12, 4).ajouter(new Gisement(TypeRessource.GLACE));
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
     *   5. Vérification des événements (EventBus)
     *   6. Vérification de la victoire (si fusée assemblée)
     */
    public void processTick() {

        // ── 1. Avancer le temps ──────────────────────────────────────── //
        this.temps.augmenterHeure();

        // ── 2. Production (bloquée la nuit) ─────────────────────────── //
        if (!temps.estNuit()) {
            mettreAJourProduction();
            ticksDemiJournee++;

            // ── 3. Besoins vitaux — une fois par demi-journée ─────────── //
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

        // ── 5. Événements ────────────────────────────────────────────── //
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
        List<Batiment> batiments = this.joueur.getBatiments();

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
        List<Ouvrier> equipage = this.joueur.getOuvriers();
        Stock stock = this.joueur.getStock();

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
            // Si l'ouvrier n'est plus en état (STRESSE ou FATIGUE), cela
            // affectera sa productivité lors du prochain tick
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
        List<Ouvrier> equipage  = this.joueur.getOuvriers();
        int litsDisponibles     = this.joueur.getNombreLitsDisponibles();
        int totalOuvriers       = equipage.size();

        // Taux d'occupation des lits (doc p.3 : surpopulation > 95%)
        boolean surpopulation = (double) totalOuvriers / Math.max(litsDisponibles, 1) > 0.95;

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
        for (Ouvrier o : this.joueur.getOuvriers()) {
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
     * À brancher sur l'EventBus quand il sera implémenté.
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
     * À compléter selon les types de commandes.
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
