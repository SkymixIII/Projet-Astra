package jeu;

import java.util.List;

import carte.Carte;
import carte.TypeSol;
import carte.Sol;
import entites.*;
import fusee.Fusee;
import ressources.*;
import batiments.*;
import exceptions.*;
import evenements.*;

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
    private EventBus bus;
    private Fusee    fusee;
    private Temps    temps;
    //private Age      age;

    // ------------------------------------------------------------------ //
    //  Constructeur                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Constructeur de la classe.
     * Initialise tous les composants du jeu.
     */
    public Jeu() {
        this.joueur = new Joueur(50,50); // spawn initial au centre de la carte
        this.bus    = new EventBus();
        this.fusee  = new Fusee();
        this.temps  = new Temps();
        //this.age    = new Age();
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

   public void creationMonde() {
    this.carte = new Carte(100, 100, 5);

    // ============================================================
    // SOL z=0 : Îles et Eau
    // ============================================================
    for (int x = 0; x < 100; x++) {
        for (int y = 0; y < 100; y++) {
            
            // 1. Île principale (un peu décalée vers le haut/droite)
            boolean ilePrincipale = (Math.pow((x - 60) / 35.0, 2) + Math.pow((y - 50) / 35.0, 2) < 1);
            
            // 2. Île Sud-Ouest (nouvelle île séparée)
            boolean ileSO = (Math.pow((x - 25) / 15.0, 2) + Math.pow((y - 25) / 12.0, 2) < 1);
            
            // 3. Petite île de lancement (Sud-Est)
            boolean ileLancement = (Math.pow(x - 85, 2) + Math.pow(y - 15, 2) < 25);

            // Gestion du Sable (bordure de 2 pixels autour des îles)
            boolean bordureSable = 
                (Math.pow((x - 60) / 37.0, 2) + Math.pow((y - 50) / 37.0, 2) < 1 && !ilePrincipale) ||
                (Math.pow((x - 25) / 17.0, 2) + Math.pow((y - 25) / 14.0, 2) < 1 && !ileSO) ||
                (Math.pow(x - 85, 2) + Math.pow(y - 15, 2) < 40 && !ileLancement);

            TypeSol sol;
            if (ilePrincipale || ileSO || ileLancement) sol = TypeSol.HERBE;
            else if (bordureSable) sol = TypeSol.SABLE;
            else sol = TypeSol.EAU;

            carte.getTile(x, y, 0).ajouter(new Sol(sol, x, y));
        }
    }

    // ============================================================
    // RELIEF (Z=1 à Z=4)
    // ============================================================

    // PIERRE (z=1) : Colline sur l'île Sud-Ouest + base Montagne Nord
    for (int x = 0; x < 100; x++) {
        for (int y = 0; y < 100; y++) {
            // Colline SO
            if (Math.pow(x - 25, 2) + Math.pow(y - 25, 2) < 50) {
                carte.getTile(x, y, 1).ajouter(new Sol(TypeSol.ROCHE, x, y));
            }
            // Base Montagne Nord
            if (Math.pow((x - 50) / 15.0, 2) + Math.pow((y - 80) / 10.0, 2) < 1) {
                carte.getTile(x, y, 1).ajouter(new Sol(TypeSol.ROCHE, x, y));
            }
        }
    }

    // MONTAGNE (z=2 & 3) : Fer et Silicium
    for (int x = 40; x <= 60; x++) {
        for (int y = 75; y <= 85; y++) {
            if (Math.pow((x - 50) / 10.0, 2) + Math.pow((y - 80) / 6.0, 2) < 1) {
                carte.getTile(x, y, 2).ajouter(new Sol(TypeSol.ROCHE_DURE, x, y));
                // Étage supérieur réduit
                if (Math.pow((x - 50) / 6.0, 2) + Math.pow((y - 80) / 4.0, 2) < 1) {
                    carte.getTile(x, y, 3).ajouter(new Sol(TypeSol.ROCHE_DURE, x, y));
                }
            }
        }
    }

    // SOMMET NEIGE (z=4) : Glace
    carte.getTile(50, 80, 4).ajouter(new Sol(TypeSol.NEIGE, 50, 80));
    carte.getTile(51, 80, 4).ajouter(new Sol(TypeSol.NEIGE, 51, 80));

    // ============================================================
    // RESSOURCES 
    // ============================================================

 // --- FORÊTS (Z=0) ---
    int[][] forets = {
        {65, 60}, {70, 55}, {75, 45}, {80, 50}, // Est
        {20, 25}, {25, 30}, {30, 20},           // Île Sud-Ouest
        {45, 40}, {50, 35},                     // Centre
        {60, 70}, {65, 75}, {70, 80},           // Bordure forêt Nord-Est
        {40, 25}, {45, 20}                      // Petit bosquet central sud
    };
    for (int i = 0; i < forets.length; i++) {
        int x = forets[i][0], y = forets[i][1];
        carte.getTile(x, y, 0).ajouter(new LieuDeRessource("Forêt", TypeRessource.BOIS, 1000, x, y));
    }

    // --- PÉTROLE (Z=0) ---
    int[][] petrole = { {40, 45}, {35, 35}, {60, 30}, {75, 30}, {25, 15} };
    for (int[] p : petrole) {
        carte.getTile(p[0], p[1], 0).ajouter(new LieuDeRessource("Pétrole", TypeRessource.PETROLE, 800, p[0], p[1]));
    }

    // --- FER & SILICIUM (Z=2 & 3) ---
    int[][] minerais = { 
        {48, 80}, {52, 80}, {50, 78}, {50, 82}, // Sommet montagne
        {45, 78}, {55, 82}, {46, 82}, {54, 78}  // Filons sur les flancs de la montagne
    };
    for (int[] p : minerais) {
        carte.getTile(p[0], p[1], 2).ajouter(new LieuDeRessource("Fer", TypeRessource.FER, 700, p[0], p[1]));
        carte.getTile(p[0], p[1], 3).ajouter(new LieuDeRessource("Silicium", TypeRessource.SILICIUM, 500, p[0], p[1]));
    }

    // --- PIERRE (Z=1) ---
    int[][] pierres = { 
        {25, 25}, {27, 23}, {50, 75}, {55, 78},
        {22, 28}, {28, 22}, {24, 24},           // roches sur l'île SO
        {45, 80}, {55, 80}                      // Base de la montagne Nord
    };
    for (int[] p : pierres) {
        carte.getTile(p[0], p[1], 1).ajouter(new LieuDeRessource("Pierre", TypeRessource.PIERRE, 900, p[0], p[1]));
    }
    // --- GLACE (Z=4) ---
    carte.getTile(50, 80, 4).ajouter(new LieuDeRessource("Glace", TypeRessource.GLACE, 400, 50, 80));
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

            // ── 3. Besoins vitaux — une fois par demi-journée ─────────── //
            if (temps.estFinDemiJournee()) {
                consommerBesoinsVitaux();
                mettreAJourMoral();
            }

        } else {
            // ── Nuit : récupération de fatigue ───────────────────────── //
            if (temps.estFinNuit()) {
                recupererFatigue();
            }
        }

        // ── 4. Commandes utilisateur ─────────────────────────────────── //
        recupererCommande();
        traiterCommande();

        // ── 5. Événements ────────────────────────────────────────────── //
        bus.traiter(temps, joueur.getStock(), joueur.getOuvriers());

        // ── 6. Victoire (uniquement si la fusée est assemblée) ────────── //
		// cette condition est amenée à changer dans la V2
        if (fusee != null && fusee.tousModulesAssembles() && fusee.isIngenieurABord()) {
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
                if (u.isOperationnel()) {
                    try {
                        u.mettreAJour(joueur.getStock(), 1); // 1 tick écoulé
                    } catch (Exception e) {
                        System.err.println("[Production] " + e.getMessage());
                    }
                }
            } else if (b instanceof LieuDeRessource) {
                b.mettreAJour(joueur.getStock(), 1);
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
            // Facteurs positifs (doc p.3)*
			//TODO à reprendre pour la V2, j'ai arangé avec Ouvrier mais c'est incomplet
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
        
        double prob = fusee.calculerProbabiliteSucces();
		if (prob >= 70) {
            System.out.println("[Victoire] Lancement possible ! Probabilité : " + prob + "%");
            // TODO : activer le bouton de lancement dans l'interface
        } else {
			System.out.println("[Défaite] Lancement impossible : probabilité de succès insuffisante (" + prob + "%)");
			// TODO : garder le bouton de lancement grisé
		}
		/* A décommenter pour la V2
		try {
            if (prob >= 70) {
                System.out.println("[Victoire] Lancement possible ! Probabilité : " + prob + "%");
                // TODO : activer le bouton de lancement dans l'interface
            }
        } catch (LancementImpossibleException e) {
            // Conditions non remplies → bouton grisé, pas d'erreur critique
            System.out.println("[Fusée] " + e.getMessage());
        }*/
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
    //public Age      getAge()    { return age; }
    public Temps    getTemps()  { return temps; }
}
