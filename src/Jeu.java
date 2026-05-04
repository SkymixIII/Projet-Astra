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
        System.out.println("Lancement du projet Astra...");
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