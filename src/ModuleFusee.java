import java.util.EnumMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ressources.TypeRessource;

/**
 * Modèle commun aux modules de la fusée (Propulseur, ChargeUtile, OrdiDeBord).
 */
public abstract class ModuleFusee {

    protected final String nom; // Nom du module
    protected float pourcentageAssemblage; // Progression de 0.0 (début) à 1.0 (terminé) 
    protected final Map<TypeRessource, Integer> recette; // Ressources nécessaires à l'assemblage 
    protected final List<Role> rolesRequis; // Métiers requis pour assembler ce module 
    protected boolean ressourcesConsommees; // Indique si les ressources ont déjà été prélevées du stock

    /**
     * Initialise un module avec son nom, sa recette et les rôles nécessaires.
     */
    public ModuleFusee(String nomModule,
            Map<TypeRessource, Integer> recette,
            List<Role> rolesRequis) {
        this.nom = nomModule;
        this.pourcentageAssemblage = 0.0f;
        this.recette = recette;
        this.rolesRequis = rolesRequis;
        this.ressourcesConsommees = false;
    }

    /**
     * Ajoute de la progression à l'assemblage (plafonnée à 1.0).
     */
    public void assembler(float avancement) {
        if (avancement <= 0) {
            return;
        }

        this.pourcentageAssemblage += avancement;
        if (this.pourcentageAssemblage > 1.0f) {
            this.pourcentageAssemblage = 1.0f;
        }
    }

    /**
     * Vérifie si le module est fini (100%).
     */
    public boolean estTermine() {
        return this.pourcentageAssemblage >= 1.0f;
    }

    /**
     * Empêche de prélever plusieurs fois les ressources dans le stock.
     */
    public void marquerRessourcesConsommees() {
        this.ressourcesConsommees = true;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public float getPourcentageAssemblage() {
        return pourcentageAssemblage;
    }

    public Map<TypeRessource, Integer> getRecette() {
        return Collections.unmodifiableMap(recette);
    }

    public List<Role> getRolesRequis() {
        return Collections.unmodifiableList(rolesRequis);
    }

    public boolean ressourcesDejaConsommees() {
        return ressourcesConsommees;
    }
}
