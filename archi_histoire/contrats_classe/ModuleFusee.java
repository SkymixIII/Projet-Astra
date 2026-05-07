// La classe abstraite sert de modèle commun pour tous les modules

public abstract class ModuleFusee {

    // Le nom du module pour pouvoir l'afficher facilement
    String nom;

    // La progression de l'assemblage 
    float pourcentageAssemblage;

    // Le constructeur 
    public ModuleFusee(String nomModule) {
        this.nom = nomModule;
        this.pourcentageAssemblage = 0.0f;
    }

    /**
     * Méthode pour faire avancer l'assemblage du module. Le CentreLancement
     * appellera cette méthode en lui donnant une valeur d'avancement.
     */
    public void assembler(float avancement) {
        // On ajoute la nouvelle progression au pourcentage actuel
        this.pourcentageAssemblage = this.pourcentageAssemblage + avancement;

        // On sécurise le code : le pourcentage ne peut pas dépasser 1.0f (100%)
        if (this.pourcentageAssemblage > 1.0f) {
            this.pourcentageAssemblage = 1.0f;
        }
    }

    /**
     * Méthode pour vérifier si le module est prêt. Dans l'histoire simplifiée,
     * le module est prêt quand il atteint 100%.
     */
    public boolean estTermine() {
        if (this.pourcentageAssemblage >= 1.0f) {
            return true;
        } else {
            return false;
        }
    }
}
