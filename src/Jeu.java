import java.util.List;

/**
 * Classe orchestratrice principale
 * Gère la boucle de jeu, la production et les besoins vitaux des ouvriers
 */
public class Jeu {
    // Attributs définis dans l'architecture
    private Joueur joueur;
    private Carte carte;
    private Eventbus bus;
    private Fusee fusee;
    private Temps temps;
    private Age age;

    /**
     * Point d'entrée principal du simulateur
     */
    public static void main(String[] args) {
        Jeu projetAstra = new Jeu();
        // Initialisation de l'affichage et du monde ici
        Creation_monde();
        System.out.println("Lancement du projet Astra...");
    }

    /**
     * Méthode pour créer la map
     */
    public void creation_monde() {
        Carte carte = new Carte(100, 100, 5);
    
        // --- Spawn joueur ---
        carte.getTile(50, 50, 0).ajouter(new Joueur());
    
        // ============================================================
        // SOL z=0 : plaine partout avec île au sud ouest
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
    
    /**
     * Méthode appelée à chaque "tick" de l'horloge système
     * Met à jour l'ensemble de la simulation
     */
    public void processTick() {
        // Gestion du temps
        this.temps.augmenterHeure();

        // Logique métier
        this.mettreAJourProduction();
        this.consommerBesoinsVitaux();

        // Commandes utilisateur
        this.recupererCommande();
        this.traiterCommande();
    }

    /**
     * Parcourt tous les bâtiments du joueur pour générer des ressources
     */
    public void mettreAJourProduction() {
        List<Batiment> batiments = this.joueur.getBatiments();

        for (Batiment b : batiments) {
            if (b instanceof Usine) {
                Usine u = (Usine) b;
                if (u.peutProduire()) {
                    try {
                        u.produire(); // Utilise les ressources du stock
                    } catch (Exception e) { // RessourceInsuffisanteException
                        System.err.println("Erreur de production : " + e.getMessage());
                    }
                }
            } else if (b instanceof LieuDeRessource) {
                ((LieuDeRessource) b).extraire(); // Extraction automatique
            }
        }
    }

    /**
     * Réduit les jauges de faim et de soif des ouvriers à chaque intervalle
     */
    public void consommerBesoinsVitaux() {
        List<Ouvrier> equipage = this.joueur.getOuvriers(); // Attribut du joueur

        for (Ouvrier o : equipage) {
            o.mettreAJourBesoins(); // Décrémente faim/soif et met à jour l'EtatOuvrier

            // Si l'ouvrier n'est plus en état (STRESSE ou FATIGUE), cela
            // affectera sa productivité lors du prochain tick
        }
    }

    /**
     * Simule la récupération d'une commande via l'interface ou le bus d'évènements
     */
    public void recupererCommande() {
        // Logique pour écouter les entrées utilisateur (Terminal ou GUI)
    }

    /**
     * Traite les ordres prioritaires (Construction, Déplacement, Recherche)
     */
    public void traiterCommande() {
        // Envoi des ordres aux classes concernées (Joueur, CentreRecherche, etc.)
    }

    // Getters et Setters nécessaires pour l'orchestration
    public Joueur getJoueur() { return joueur; }
    public Carte getCarte() { return carte; }
    public Fusee getFusee() { return fusee; }
    public Age getAge() { return age; }
}
