package jeu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import batiments.Batiment;
import batiments.LieuDeRessource;
import batiments.Usine;
import carte.Carte;
import carte.Sol;
import carte.TypeSol;
import entites.Joueur;
import entites.Ouvrier;
import evenements.EventBus;
import exceptions.StockException;
import fusee.Fusee;
import ressources.Stock;
import ressources.TypeRessource;

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

    private boolean partieTerminee = false;

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
        Jeu jeu = new Jeu();
        jeu.creationMonde();
 
        // Remplir le stock de départ pour pouvoir tester la production
        jeu.initialiserStockDeDepart();
 
        Scanner scanner = new Scanner(System.in);
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║                PROJET ASTRA                  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        afficherAide();
        while (!jeu.isPartieTerminee()) {
            System.out.print("\n> Commande : ");
            String ligne = scanner.nextLine().trim();
            if (ligne.isEmpty()) continue;
 
            String[] parts = ligne.split("\\s+");
            String cmd = parts[0].toLowerCase();
 
            switch (cmd) {
 
                // ── Avancer le temps ─────────────────────────────────── //
                case "tick": {
                    // [AJOUT] "tick [n]" — exécute n ticks (défaut : 1)
                    int n = 1;
                    if (parts.length >= 2) {
                        try { n = Integer.parseInt(parts[1]); }
                        catch (NumberFormatException e) {
                            System.out.println("[Erreur] Usage : tick [nombre]");
                            break;
                        }
                    }
                    for (int i = 0; i < n && !jeu.isPartieTerminee(); i++) {
                        jeu.processTick();
                    }
                    System.out.println("[Temps] " + jeu.getTemps());
                    break;
                }
                
                // ── Placer une usine ──────────────────────────────────── //
                case "construire": {
                    // [AJOUT] "construire <type> <x> <y>"
                    // Types disponibles : MENUISERIE, FONDERIE, RAFFINERIE,
                    //                     ELECTRONIQUE, ASSEMBLAGE
                    if (parts.length < 4) {
                        System.out.println("[Erreur] Usage : construire <type> <x> <y>");
                        System.out.println("  Types : MENUISERIE  FONDERIE  RAFFINERIE  ELECTRONIQUE  ASSEMBLAGE");
                        break;
                    }
                    try {
                        int bx = Integer.parseInt(parts[2]);
                        int by = Integer.parseInt(parts[3]);
                        jeu.construireUsine(parts[1].toUpperCase(), bx, by);
                    } catch (NumberFormatException e) {
                        System.out.println("[Erreur] Les coordonnées doivent être des entiers.");
                    }
                    break;
                }
                
                // ── Affecter un ouvrier à une usine ───────────────────── //
                case "affecter": {
                    // [AJOUT] "affecter <nomOuvrier> <indexBatiment>"
                    // indexBatiment : numéro affiché par "etat"
                    if (parts.length < 3) {
                        System.out.println("[Erreur] Usage : affecter <nomOuvrier> <indexBatiment>");
                        break;
                    }
                    try {
                        int idx = Integer.parseInt(parts[2]);
                        jeu.affecterOuvrier(parts[1], idx);
                    } catch (NumberFormatException e) {
                        System.out.println("[Erreur] L'index doit être un entier.");
                    }
                    break;
                }
 
                // ── Retirer un ouvrier d'une usine ────────────────────── //
                case "retirer": {
                    // [AJOUT] "retirer <nomOuvrier> <indexBatiment>"
                    if (parts.length < 3) {
                        System.out.println("[Erreur] Usage : retirer <nomOuvrier> <indexBatiment>");
                        break;
                    }
                    try {
                        int idx = Integer.parseInt(parts[2]);
                        jeu.retirerOuvrier(parts[1], idx);
                    } catch (NumberFormatException e) {
                        System.out.println("[Erreur] L'index doit être un entier.");
                    }
                    break;
                }
 
                // ── Afficher l'état complet ───────────────────────────── //
                case "etat": {
                    jeu.afficherEtat();
                    break;
                }
 
                // ── Afficher uniquement le stock ──────────────────────── //
                case "stock": {
                    jeu.afficherStock();
                    break;
                }
 
                // ── Aide ─────────────────────────────────────────────── //
                case "aide":
                case "help": {
                    afficherAide();
                    break;
                }
 
                // ── Quitter ───────────────────────────────────────────── //
                case "quitter":
                case "quit":
                case "exit": {
                    System.out.println("Au revoir.");
                    scanner.close();
                    return;
                }
 
                default: {
                    System.out.println("[?] Commande inconnue. Tapez 'aide' pour la liste.");
                }
            }
        }
 
        // Fin de partie (victoire déclenchée dans verifierVictoire)
        System.out.println("\nPartie terminée. Merci d'avoir joué !");
        scanner.close();
    }

    /**
     *Remplit le stock de départ avec des ressources de base
     * pour permettre de tester la production sans extraction préalable.
     *
     * Stock de départ (débris de fusée, doc §2.2) :
     *   BOIS 200, PIERRE 100, FER 100, PETROLE 60, SILICIUM 40
     */

    private void initialiserStockDeDepart() {
        Stock s = joueur.getStock();
		try {
			s.ajouter(TypeRessource.BOIS,     200);
			s.ajouter(TypeRessource.PIERRE,   100);
			s.ajouter(TypeRessource.FER,      100);
			s.ajouter(TypeRessource.PETROLE,   60);
			s.ajouter(TypeRessource.SILICIUM,  40);
        	System.out.println("[Init] Stock de départ chargé.");
		} catch (StockException e) {
			System.err.println("[Erreur] Impossible d'initialiser le stock de départ : " + e.getMessage());
		}
    }

    /**
     * Construit une usine du type demandé aux coordonnées (x, y)
     * et l'ajoute à la liste des bâtiments du joueur.
     *
     * Recettes conformes au doc §6.2 (version fonctionnelle).
     *
     * @param type  Nom du type parmi : MENUISERIE, FONDERIE, RAFFINERIE,
     *              ELECTRONIQUE, ASSEMBLAGE
     * @param x     Coordonnée X sur la carte (z=0)
     * @param y     Coordonnée Y sur la carte (z=0)
     */
    public void construireUsine(String type, int x, int y) {
        TypeRessource produit;
        Map<TypeRessource, Integer> recette = new HashMap<>();
 
        switch (type) {
            case "MENUISERIE":
                produit = TypeRessource.POUTRE;
                recette.put(TypeRessource.BOIS, 10);      // 10 bois → 1 poutre
                break;
            case "FONDERIE":
                produit = TypeRessource.PLAQUE_ACIER;
                recette.put(TypeRessource.FER, 10);        // 10 minerai → 1 plaque
                break;
            case "RAFFINERIE":
                produit = TypeRessource.KEROSENE;
                recette.put(TypeRessource.PETROLE, 10);    // 10 pétrole → 1 kérosène
                break;
            case "ELECTRONIQUE":
                produit = TypeRessource.CARTE_MERE;
                recette.put(TypeRessource.SILICIUM, 5);    // 5 silicium + 2 plaques → 1 carte mère
                recette.put(TypeRessource.PLAQUE_ACIER, 2);
                break;
            case "ASSEMBLAGE":
                // La zone d'assemblage est gérée par la classe Fusee ;
                // on crée quand même une usine marqueur pour la carte.
                produit = TypeRessource.POUTRE; // placeholder
                recette.put(TypeRessource.POUTRE, 1);
                break;
            default:
                System.out.println("[Erreur] Type inconnu : " + type);
                System.out.println("  Types valides : MENUISERIE  FONDERIE  RAFFINERIE  ELECTRONIQUE  ASSEMBLAGE");
                return;
        }
 
        Usine usine = new Usine(type, x, y, produit, recette);
        joueur.getBatiments().add(usine);
        carte.getTile(x, y, 0).ajouter(usine);
 
        System.out.println("[Construction] Usine " + type
                + " placée en (" + x + "," + y + ")."
                + " Index : " + (joueur.getBatiments().size() - 1));
    }
 

    /**
     * Affecte l'ouvrier nommé {@code nomOuvrier} au bâtiment
     * à l'index {@code indexBatiment} dans la liste du joueur.
     *
     * L'ouvrier est aussi notifié de son nouveau poste (via affecterPoste).
     */
    public void affecterOuvrier(String nomOuvrier, int indexBatiment) {
        Ouvrier ouvrier = trouverOuvrier(nomOuvrier);
        if (ouvrier == null) {
            System.out.println("[Erreur] Ouvrier introuvable : " + nomOuvrier);
            return;
        }
 
        List<Batiment> batiments = joueur.getBatiments();
        if (indexBatiment < 0 || indexBatiment >= batiments.size()) {
            System.out.println("[Erreur] Index bâtiment invalide : " + indexBatiment
                    + " (0 à " + (batiments.size() - 1) + ")");
            return;
        }
 
        Batiment bat = batiments.get(indexBatiment);
        if (!bat.aDeLaPlace()) {
            System.out.println("[Erreur] Le bâtiment #" + indexBatiment + " est plein (5 ouvriers max).");
            return;
        }
 
        bat.affecterPersonnel(ouvrier);
        ouvrier.affecterPoste(bat);
        System.out.println("[Affectation] " + nomOuvrier + " → bâtiment #" + indexBatiment
                + " (" + bat.getType() + " en " + bat.getX() + "," + bat.getY() + ")");
    }

    /**
     * Retire l'ouvrier nommé {@code nomOuvrier} du bâtiment
     * à l'index {@code indexBatiment}.
     */
    public void retirerOuvrier(String nomOuvrier, int indexBatiment) {
        Ouvrier ouvrier = trouverOuvrier(nomOuvrier);
        if (ouvrier == null) {
            System.out.println("[Erreur] Ouvrier introuvable : " + nomOuvrier);
            return;
        }
 
        List<Batiment> batiments = joueur.getBatiments();
        if (indexBatiment < 0 || indexBatiment >= batiments.size()) {
            System.out.println("[Erreur] Index bâtiment invalide : " + indexBatiment);
            return;
        }
 
        batiments.get(indexBatiment).retirerPersonnel(ouvrier);
        ouvrier.affecterPoste(null);
        System.out.println("[Retrait] " + nomOuvrier + " retiré du bâtiment #" + indexBatiment);
    }
 
    /**
     *Cherche un ouvrier par son nom (insensible à la casse).
     * @return l'Ouvrier trouvé, ou null si absent.
     */
    private Ouvrier trouverOuvrier(String nom) {
        for (Ouvrier o : joueur.getOuvriers()) {
            if (o.getNom().equalsIgnoreCase(nom)) return o;
        }
        return null;
    }

        // ------------------------------------------------------------------ //
    //  Affichage console                                         //
    // ------------------------------------------------------------------ //
 
    /**
     *  Affiche l'état complet du jeu : temps, ouvriers, bâtiments.
     */
    public void afficherEtat() {
        System.out.println("\n══════════════ ÉTAT DU JEU ══════════════");
        System.out.println("  " + temps);
 
        // Ouvriers
        System.out.println("\n── Ouvriers (" + joueur.getOuvriers().size() + ") ──");
        for (Ouvrier o : joueur.getOuvriers()) {
            String poste = (o.getPosteActuel() != null)
                    ? "→ " + o.getPosteActuel().getType()
                               + " (" + o.getPosteActuel().getX()
                               + "," + o.getPosteActuel().getY() + ")"
                    : "→ sans poste";
            System.out.printf("  %-10s  %-10s  exp:%-9s  %s%n",
                    o.getNom(),
                    o.getEtat(),
                    o.getNiveau(),
                    poste);
        }
 
        // Bâtiments
        System.out.println("\n── Bâtiments (" + joueur.getBatiments().size() + ") ──");
        List<Batiment> bats = joueur.getBatiments();
        for (int i = 0; i < bats.size(); i++) {
            Batiment b = bats.get(i);
            String op = b.isOperationnel() ? "ACTIF" : "inactif";
            System.out.printf("  [%2d]  %-14s  (%3d,%3d)  %s%n",
                    i, b.getType(), b.getX(), b.getY(), op);
        }
 
        // Stock résumé
        System.out.println();
        afficherStock();
        System.out.println("═════════════════════════════════════════");
    }

    /**
     * Affiche uniquement les ressources non nulles du stock.
     */
    public void afficherStock() {
        System.out.println("── Stock ──");
        Stock s = joueur.getStock();
        boolean vide = true;
        for (TypeRessource t : TypeRessource.values()) {
            int qte = s.getQuantite(t);
            if (qte > 0) {
                System.out.printf("  %-22s : %d%n", t, qte);
                vide = false;
            }
        }
        if (vide) System.out.println("  (vide)");
    }
 
        /**
     * Affiche la liste des commandes disponibles.
     */
    private static void afficherAide() {
        System.out.println("\n── Commandes disponibles ──────────────────────────────────────");
        System.out.println("  tick [n]                      Avance de n ticks (défaut : 1)");
        System.out.println("  construire <type> <x> <y>     Place une usine");
        System.out.println("    types : MENUISERIE  FONDERIE  RAFFINERIE  ELECTRONIQUE  ASSEMBLAGE");
        System.out.println("  affecter <nom> <index>        Affecte un ouvrier à un bâtiment");
        System.out.println("  retirer  <nom> <index>        Retire un ouvrier d'un bâtiment");
        System.out.println("  etat                          Affiche ouvriers + bâtiments + stock");
        System.out.println("  stock                         Affiche uniquement le stock");
        System.out.println("  aide                          Cette aide");
        System.out.println("  quitter                       Quitte le jeu");
        System.out.println("───────────────────────────────────────────────────────────────");
        System.out.println("  Exemple de partie rapide :");
        System.out.println("    construire MENUISERIE 62 45");
        System.out.println("    affecter Alice 0");
        System.out.println("    tick 300");
        System.out.println("    stock");
        System.out.println("───────────────────────────────────────────────────────────────");
    }

   public void creationMonde() {
    this.carte = new Carte(100, 100, 5);

    // --- Spawn joueur (près des débris de la fusée, centre-est île principale) ---
    carte.getTile(62, 52, 0).ajouter(this.joueur);

    // ============================================================
    // SOL z=0 : Îles et Eau
    // ============================================================
    for (int x = 0; x < 100; x++) {
        for (int y = 0; y < 100; y++) {

            // 1. Île principale
            boolean ilePrincipale = (Math.pow((x - 52) / 38.0, 2) + Math.pow((y - 55) / 28.0, 2) < 1);

            // 2. Île Sud-Ouest
            boolean ileSO = (Math.pow((x - 22) / 13.0, 2) + Math.pow((y - 62) / 10.0, 2) < 1);

            // 3. Petite île Sud-Est
            boolean ileSE = (Math.pow(x - 78, 2) + Math.pow(y - 72, 2) < 20);

            // Bordure sable
            boolean bordureSable =
                (Math.pow((x - 52) / 40.5, 2) + Math.pow((y - 55) / 30.5, 2) < 1 && !ilePrincipale) ||
                (Math.pow((x - 22) / 15.5, 2) + Math.pow((y - 62) / 12.5, 2) < 1 && !ileSO) ||
                (Math.pow(x - 78, 2) + Math.pow(y - 72, 2) < 35 && !ileSE);

            TypeSol sol;
            if (ilePrincipale || ileSO || ileSE) sol = TypeSol.HERBE;
            else if (bordureSable)               sol = TypeSol.SABLE;
            else                                  sol = TypeSol.EAU;

            carte.getTile(x, y, 0).ajouter(new Sol(sol, x, y));
        }
    }

    // ============================================================
    // RELIEF (Z=1 à Z=4)
    // ============================================================

    // PIERRE z=1 : Base large de la montagne + colline SO
    for (int x = 0; x < 100; x++) {
        for (int y = 0; y < 100; y++) {
            // Base montagne île principale
            if (Math.pow((x - 38) / 18.0, 2) + Math.pow((y - 35) / 14.0, 2) < 1) {
                carte.getTile(x, y, 1).ajouter(new Sol(TypeSol.ROCHE, x, y));
            }
            // Colline île SO
            if (Math.pow((x - 22) / 7.0, 2) + Math.pow((y - 60) / 6.0, 2) < 1) {
                carte.getTile(x, y, 1).ajouter(new Sol(TypeSol.ROCHE, x, y));
            }
        }
    }

    // ROCHE DURE z=2 & z=3 : Corps de la montagne
    for (int x = 0; x < 100; x++) {
        for (int y = 0; y < 100; y++) {
            if (Math.pow((x - 38) / 13.0, 2) + Math.pow((y - 35) / 10.0, 2) < 1) {
                carte.getTile(x, y, 2).ajouter(new Sol(TypeSol.ROCHE_DURE, x, y));
                if (Math.pow((x - 38) / 8.0, 2) + Math.pow((y - 34) / 6.0, 2) < 1) {
                    carte.getTile(x, y, 3).ajouter(new Sol(TypeSol.ROCHE_DURE, x, y));
                }
            }
        }
    }

    // SOMMET NEIGE z=4
    for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
            carte.getTile(38 + dx, 33 + dy, 4).ajouter(new Sol(TypeSol.NEIGE, 38 + dx, 33 + dy));
        }
    }

    // ============================================================
    // RESSOURCES
    // ============================================================

    // --- FORÊTS ---
    int[][] forets = {
        // Droite île principale
        {65, 45}, {70, 48}, {68, 52}, {72, 55}, {67, 58}, {63, 62},
        {74, 42}, {76, 50}, {70, 60}, {65, 65},
        // Bas centre île principale
        {55, 65}, {50, 68}, {58, 70}, {45, 62},
        // Île SO
        {19, 65}, {23, 67}, {25, 63}
    };
    for (int[] f : forets) {
        carte.getTile(f[0], f[1], 0).ajouter(new LieuDeRessource("Forêt", TypeRessource.BOIS, 1000, f[0], f[1]));
    }

    // --- PÉTROLE : 3 taches centre/bas île principale ---
    int[][] petrole = { {50, 58}, {55, 62}, {48, 65} };
    for (int[] p : petrole) {
        carte.getTile(p[0], p[1], 0).ajouter(new LieuDeRessource("Pétrole", TypeRessource.PETROLE, 800, p[0], p[1]));
    }

    // --- FER (Z=2) : flancs de la montagne ---
    int[][] fer = {
        {33, 36}, {34, 40}, {43, 36}, {44, 40},
        {36, 42}, {40, 42},
        {35, 30}, {41, 30}
    };
    for (int[] p : fer) {
        carte.getTile(p[0], p[1], 2).ajouter(new LieuDeRessource("Fer", TypeRessource.FER, 700, p[0], p[1]));
    }

    // --- SILICIUM (Z=3) : proche du sommet ---
    int[][] silicium = {
        {36, 34}, {40, 34},
        {35, 37}, {41, 37},
        {38, 40}, {38, 31}
    };
    for (int[] p : silicium) {
        carte.getTile(p[0], p[1], 3).ajouter(new LieuDeRessource("Silicium", TypeRessource.SILICIUM, 500, p[0], p[1]));
    }

    // --- PIERRE z=1 : flancs montagne + colline SO ---
    int[][] pierres = {
        {30, 38}, {32, 33}, {44, 38}, {42, 40},
        {20, 60}, {24, 58}, {22, 63}
    };
    for (int[] p : pierres) {
        carte.getTile(p[0], p[1], 1).ajouter(new LieuDeRessource("Pierre", TypeRessource.PIERRE, 900, p[0], p[1]));
    }

    // --- GLACE z=4 ---
    carte.getTile(38, 33, 4).ajouter(new LieuDeRessource("Glace", TypeRessource.GLACE, 400, 38, 33));

    // ============================================================
    // OUVRIERS
    // ============================================================
    int[][] positionsOuvriers = {
        {60, 51}, {61, 51}, {63, 51},
        {60, 53}, {61, 53}, {63, 53}
    };
    String[] nomsOuvriers = {"Alice", "Bob", "Charlie", "Diana", "Emile", "Fanny"};
    for (int i = 0; i < positionsOuvriers.length; i++) {
        int ox = positionsOuvriers[i][0];
        int oy = positionsOuvriers[i][1];
        Ouvrier o = new Ouvrier(nomsOuvriers[i], ox, oy);
        this.joueur.getOuvriers().add(o);
        carte.getTile(ox, oy, 0).ajouter(o);
    }


        // ============================================================
        // BÂTIMENT DE TEST
        // ============================================================
        construireUsine("FONDERIE", 50, 60);
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

        if (partieTerminee) return; //Stoppe le tick si victoire

        // ── 1. Avancer le temps ──────────────────────────────────────── //
        this.temps.augmenterHeure();

        // ── 2. Production (bloquée la nuit) ─────────────────────────── //
        if (!temps.estNuit()) {
            mettreAJourProduction();

            // ── 3 Nuit : récupération de fatigue ───────────────────────── //
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
        verifierVictoire();
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
        
        if (fusee == null) return;
 
        if (fusee.tousModulesAssembles() && fusee.isIngenieurABord()) {
            partieTerminee = true;
            System.out.println("==============================================");
            System.out.println("[VICTOIRE] Tous les modules sont assemblés !");
            System.out.println("           La fusée décolle automatiquement.");
            System.out.println("==============================================");
            // TODO : déclencher l'écran de victoire dans l'interface graphique
        }
    }

    // ------------------------------------------------------------------ //
    //  Getters                                                            //
    // ------------------------------------------------------------------ //

    public Joueur   getJoueur() { return joueur; }
    public Carte    getCarte()  { return carte; }
    public Fusee    getFusee()  { return fusee; }
    //public Age      getAge()    { return age; }
    public Temps    getTemps()  { return temps; }

    public boolean isPartieTerminee() { return partieTerminee; }
}
