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

    // ------------------------------------------------------------------ //
    //  Création du monde                                                  //
    // ------------------------------------------------------------------ //

   	public void creationMonde() {
		this.carte = new Carte(100, 100, 5);
		// --- Spawn joueur (centre de la plaine) ---
	    carte.getTile(50, 60, 0).ajouter(this.joueur);
	
	    // ============================================================
	    // SOL z=0 : île principale + île secondaire
	    // ============================================================
	    for (int x = 0; x < 100; x++) {
	        for (int y = 0; y < 100; y++) {
	
	            boolean bordure = (x < 8 || x > 91 || y < 8 || y > 91);
	
	            // Île principale (grand cercle)
	            boolean ile =
	                (Math.pow((x - 50) / 38.0, 2) + Math.pow((y - 50) / 38.0, 2) < 1);
	
	            // Bord sable (anneau)
	            boolean bordIle =
	                (Math.pow((x - 50) / 40.0, 2) + Math.pow((y - 50) / 40.0, 2) < 1)
	                && !ile;
	
	            // Petite île sud-est
	            boolean ileSE =
	                (Math.pow(x - 85, 2) + Math.pow(y - 85, 2) < 20);
	
	            boolean bordIleSE =
	                (Math.pow(x - 85, 2) + Math.pow(y - 85, 2) < 30)
	                && !ileSE;
	
	            TypeSol sol;
	            if (bordure || bordIle || bordIleSE) sol = TypeSol.EAU;
	            else if (ileSE) sol = TypeSol.SABLE;
	            else if (ile) sol = TypeSol.HERBE;
	            else sol = TypeSol.EAU;
	
	            carte.getTile(x, y, 0).ajouter(new Sol(sol, x, y));
	        }
	    }
	
	    // ============================================================
	    // COLLINES (sud-ouest)
	    // ============================================================
	    for (int x = 30; x <= 45; x++) {
	        for (int y = 60; y <= 75; y++) {
	            carte.getTile(x, y, 1).ajouter(new Sol(TypeSol.ROCHE, x, y));
	        }
	    }
	
	    // ============================================================
	    // MONTAGNE CENTRALE
	    // ============================================================
	    for (int x = 40; x <= 60; x++) {
	        for (int y = 35; y <= 55; y++) {
	            carte.getTile(x, y, 2).ajouter(new Sol(TypeSol.ROCHE_DURE, x, y));
	        }
	    }
	
	    // ============================================================
	    // FLANCS (z=3)
	    // ============================================================
	    int[][] flancs = {
	        {50, 35}, {48, 37}, {52, 37},
	        {45, 45}, {55, 45}, {50, 55}
	    };
	
	    for (int[] p : flancs) {
	        carte.getTile(p[0], p[1], 3).ajouter(new Sol(TypeSol.ROCHE_DURE, p[0], p[1]));
	    }
	
	    // ============================================================
	    // SOMMETS NEIGE (centre)
	    // ============================================================
	    int[][] sommets = {
	        {50, 45}, {48, 43}, {52, 43}, {50, 47}
	    };
	
	    for (int[] p : sommets) {
	        carte.getTile(p[0], p[1], 4).ajouter(new Sol(TypeSol.NEIGE, p[0], p[1]));
	    }
	
	    // ============================================================
	    // RESSOURCES
	    // ============================================================
	
	    // --- FORÊTS (plaine répartie) ---
	    int[][] forets = {
	        {40, 60}, {45, 65}, {55, 60}, {60, 55},
	        {50, 70}, {35, 55}, {65, 65}, {50, 50}
	    };
	
	    for (int i = 0; i < forets.length; i++) {
	        int x = forets[i][0];
	        int y = forets[i][1];
	        carte.getTile(x, y, 0).ajouter(
	            new LieuDeRessource("Forêt " + (i+1), TypeRessource.BOIS, 1000, x, y)
	        );
	    }
	
	    // --- FER (montagne centrale) ---
	    int[][] fer = {
	        {48, 45}, {52, 45}, {50, 42}, {46, 48},
	        {54, 48}, {50, 50}, {47, 43}
	    };
	
	    for (int i = 0; i < fer.length; i++) {
	        int x = fer[i][0];
	        int y = fer[i][1];
	        carte.getTile(x, y, 2).ajouter(
	            new LieuDeRessource("Mine de fer " + (i+1), TypeRessource.FER, 800, x, y)
	        );
	    }
	
	    // --- SILICIUM (montagne) ---
	    int[][] silicium = {
	        {49, 44}, {51, 44}, {50, 46}
	    };
	
	    for (int i = 0; i < silicium.length; i++) {
	        int x = silicium[i][0];
	        int y = silicium[i][1];
	        carte.getTile(x, y, 2).ajouter(
	            new LieuDeRessource("Silicium " + (i+1), TypeRessource.SILICIUM, 600, x, y)
	        );
	    }
	
	    // --- PIERRE (collines sud-ouest) ---
	    int[][] pierre = {
	        {35, 65}, {38, 68}, {32, 70}, {42, 62}, {40, 72}
	    };
	
	    for (int i = 0; i < pierre.length; i++) {
	        int x = pierre[i][0];
	        int y = pierre[i][1];
	        carte.getTile(x, y, 1).ajouter(
	            new LieuDeRessource("Carrière " + (i+1), TypeRessource.PIERRE, 900, x, y)
	        );
	    }
	
	    // --- PÉTROLE (plaine EST, hors collines) ---
	    int[][] petrole = {
	        {65, 55}, {70, 50}, {75, 60},
	        {68, 65}, {72, 45}, {78, 55}
	    };
	
	    for (int i = 0; i < petrole.length; i++) {
	        int x = petrole[i][0];
	        int y = petrole[i][1];
	        carte.getTile(x, y, 0).ajouter(
	            new LieuDeRessource("Pétrole " + (i+1), TypeRessource.PETROLE, 600, x, y)
	        );
	    }
	
	    // --- GLACE (UNIQUEMENT sur neige) ---
	    int[][] glace = {
	        {50, 45}, {48, 43}
	    };
	
	    for (int i = 0; i < glace.length; i++) {
	        int x = glace[i][0];
	        int y = glace[i][1];
	        carte.getTile(x, y, 4).ajouter(
	            new LieuDeRessource("Glace " + (i+1), TypeRessource.GLACE, 500, x, y)
	        );
	    }
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
