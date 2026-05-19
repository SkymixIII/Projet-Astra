package batiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import carte.Direction;
import carte.Item;
import entites.Ouvrier;
import exceptions.RessourceInsuffisanteException;
import fusee.ModuleFusee;
import ressources.Stock;
import ressources.TypeRessource;

/**
 * Zone d'assemblage — bâtiment unique qui :
 *   1. Reçoit un ModuleFusee cible via demarrerAssemblage(ModuleFusee, Stock).
 *   2. Consomme les ressources de la recette au démarrage.
 *   3. Avance l'assemblage à chaque tick selon l'efficacité du personnel.
 *
 * Le personnel valide est vérifié lors du démarrage.
 */
public class ZoneAssemblage implements Batiment {

    // Durées d'assemblage par module (§7.1)
    public static final int TICKS_PROPULSEUR   = 3600;
    public static final int TICKS_CHARGE_UTILE = 2400;
    public static final int TICKS_ORDI_BORD    = 3600;

    private final int x, y;
    private final List<Ouvrier> personnel = new ArrayList<>();
    private static final int CAPACITE_MAX = 5;

    // Module en cours d'assemblage (null = aucun)
    private ModuleFusee moduleEnCours;
    // Durée totale requise pour ce module (en ticks efficaces)
    private int dureeCible;
    // Ticks efficaces accumulés sur le cycle courant
    private double ticksAccumules;

    public ZoneAssemblage(int x, int y) {
        this.x = x;
        this.y = y;
        this.moduleEnCours = null;
        this.dureeCible    = 0;
        this.ticksAccumules = 0;
    }


    /**
     * Démarre l'assemblage d'un module.
     * Vérifie que le module n'est pas déjà terminé et que les ressources sont disponibles.
     * Consomme les ressources immédiatement (une seule fois).
     *
     * @return true si le démarrage a réussi, false sinon (module déjà terminé,
     *         ressources insuffisantes, ou un autre module est déjà en cours).
     */
    public boolean demarrerAssemblage(ModuleFusee module, Stock stock) {
        if (module.estTermine()) {
            System.out.println("[ZoneAssemblage] " + module.getNom() + " est déjà terminé.");
            return false;
        }
        if (moduleEnCours != null && !moduleEnCours.estTermine()) {
            System.out.println("[ZoneAssemblage] Assemblage déjà en cours : " + moduleEnCours.getNom());
            return false;
        }
        if (module.ressourcesDejaConsommees()) {
            // Assemblage interrompu puis repris : on n'a pas besoin de reconsommer
            moduleEnCours   = module;
            dureeCible      = resoudureDuree(module);
            ticksAccumules  = module.getPourcentageAssemblage() * dureeCible;
            System.out.println("[ZoneAssemblage] Reprise de l'assemblage : " + module.getNom());
            return true;
        }

        // Vérification + consommation des ressources
        Map<TypeRessource, Integer> recette = module.getRecette();
        if (!stock.contientTout(recette)) {
            System.out.println("[ZoneAssemblage] Ressources insuffisantes pour " + module.getNom());
            afficherManque(recette, stock);
            return false;
        }
        try {
            stock.consommerRecette(recette);
        } catch (RessourceInsuffisanteException e) {
            System.out.println("[ZoneAssemblage] Erreur inattendue : " + e.getMessage());
            return false;
        }

        module.marquerRessourcesConsommees();
        moduleEnCours  = module;
        dureeCible     = resoudureDuree(module);
        ticksAccumules = 0;
        System.out.println("[ZoneAssemblage] Démarrage assemblage : " + module.getNom()
                + " (" + dureeCible + " ticks)");
        return true;
    }

    /** Retourne le module actuellement en cours (peut être null). */
    public ModuleFusee getModuleEnCours() { return moduleEnCours; }

    /**
     * Appelée à chaque tick de travail par Jeu.mettreAJourProduction().
     * Avance l'assemblage proportionnellement à l'efficacité cumulée du personnel.
     */
    @Override
    public void mettreAJour(Stock stock, int tempsEcoule) {
        if (moduleEnCours == null || moduleEnCours.estTermine() || dureeCible <= 0) return;
        if (personnel.isEmpty()) return;

        // Efficacité cumulée de tous les ouvriers affectés
        double efficacite = 0;
        for (Ouvrier o : personnel) {
            efficacite += o.getEfficacite();
        }

        ticksAccumules += tempsEcoule * efficacite;

        // Avancement en fraction [0.0 – 1.0]
        float avancement = (float) (tempsEcoule * efficacite / dureeCible);
        moduleEnCours.assembler(avancement);

        if (moduleEnCours.estTermine()) {
            System.out.println("[ZoneAssemblage] ✓ Module terminé : " + moduleEnCours.getNom());
        }
    }

    @Override
    public boolean isOperationnel() {
        return moduleEnCours != null && !moduleEnCours.estTermine() && !personnel.isEmpty();
    }

    @Override public TypeBatiment getType() { return TypeBatiment.ZONE_ASSEMBLAGE; }
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }

    @Override
    public void affecterPersonnel(Ouvrier o) {
        if (aDeLaPlace() && !personnel.contains(o)) personnel.add(o);
    }

    @Override
    public void retirerPersonnel(Ouvrier o) { personnel.remove(o); }

    @Override
    public boolean aDeLaPlace() { return personnel.size() < CAPACITE_MAX; }

    @Override public void deplacer(int x, int y) { /* Immobile */ }
    @Override public void deplacer(Direction dir) { /* Immobile */ }

    @Override
    public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - x, 2) + Math.pow(autre.getY() - y, 2));
    }

    // -------------------------------------------------------------------------
    // Utilitaires privés
    // -------------------------------------------------------------------------

    /** Résout la durée cible en ticks selon le nom du module. */
    private int resoudureDuree(ModuleFusee module) {
        switch (module.getNom()) {
            case "Propulseur":        return TICKS_PROPULSEUR;
            case "Charge Utile":      return TICKS_CHARGE_UTILE;
            case "Ordinateur de Bord":return TICKS_ORDI_BORD;
            default:                  return TICKS_PROPULSEUR; // valeur de secours
        }
    }

    /** Affiche les ressources manquantes pour aider le joueur. */
    private void afficherManque(Map<TypeRessource, Integer> recette, Stock stock) {
        for (Map.Entry<TypeRessource, Integer> e : recette.entrySet()) {
            int dispo = stock.getQuantite(e.getKey());
            if (dispo < e.getValue()) {
                System.out.printf("  • %s : %d / %d%n", e.getKey(), dispo, e.getValue());
            }
        }
    }
}
