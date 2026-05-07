package code;

/**
 * Fusée — condition de victoire du jeu Astra. Décolle si les 3 modules sont à
 * 100% et qu'un ingénieur est à bord.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class FuseeV3 {

    /**
     * État d'assemblage de chaque module, entre 0.0f et 1.0f.
     */
    private float etatPropulseur;
    private float etatChargeUtile;
    private float etatCommande;

    private final Propulseur propulseur;
    private final ChargeUtile chargeUtile;
    private final OrdiDeBord ordiDeBord;

    /**
     * true si un ingénieur est à bord.
     */
    private boolean ingenieurABord;

    /**
     * Crée une fusée avec ses 3 modules à 0%.
     */
    public Fusee() {
        this.etatPropulseur = 0.0f;
        this.etatChargeUtile = 0.0f;
        this.etatCommande = 0.0f;

        this.propulseur = new Propulseur();
        this.chargeUtile = new ChargeUtile();
        this.ordiDeBord = new OrdiDeBord();

        this.ingenieurABord = false;
    }

    /**
     * Retourne la probabilité de succès : moyenne des 3 états, bornée à [0.0,
     * 1.0].
     */
    public float calculerProbabiliteSucces() {
        float prob = (this.etatPropulseur + this.etatChargeUtile + this.etatCommande) / 3.0f;
        if (prob < 0.0f) {
            prob = 0.0f;
        }
        if (prob > 1.0f) {
            prob = 1.0f;
        }
        return prob;
    }

    /**
     * Tente de lancer la fusée.
     *
     * @return true si les 3 modules sont à 100% et un ingénieur est à bord.
     */
    public boolean lancer() {
        float probabilite = this.calculerProbabiliteSucces();
        boolean modulesPrets = probabilite >= 1.0f;

        if (modulesPrets && this.ingenieurABord) {
            System.out.println("*** Decollage reussi ! VICTOIRE ! ***");
            return true;
        } else {
            System.out.println("Lancement impossible : "
                    + (!modulesPrets ? "modules non termines" : "")
                    + (!modulesPrets && !ingenieurABord ? " ; " : "")
                    + (!ingenieurABord ? "aucun ingenieur a bord" : ""));
            return false;
        }
    }

    /**
     * Variante stricte : lève une exception détaillée si les conditions ne sont
     * pas remplies.
     *
     * @throws LancementImpossibleException si un module ou l'équipage est
     * manquant.
     */
    public void lancerStrict() throws LancementImpossibleException {
        StringBuilder raisons = new StringBuilder();
        if (!propulseur.estTermine()) {
            raisons.append("Propulseur non termine ; ");
        }
        if (!chargeUtile.estTermine()) {
            raisons.append("Charge utile non terminee ; ");
        }
        if (!ordiDeBord.estTermine()) {
            raisons.append("Ordinateur de bord non termine ; ");
        }
        if (!ingenieurABord) {
            raisons.append("Aucun ingenieur a bord ; ");
        }

        if (raisons.length() > 0) {
            throw new LancementImpossibleException(
                    "Conditions de lancement non remplies : " + raisons);
        }

        System.out.println("*** Decollage reussi ! VICTOIRE ! ***");
    }

    /**
     * Synchronise les floats UML avec l'état réel des modules.
     */
    public void synchroniserEtats() {
        this.etatPropulseur = this.propulseur.getPourcentageAssemblage();
        this.etatChargeUtile = this.chargeUtile.getPourcentageAssemblage();
        this.etatCommande = this.ordiDeBord.getPourcentageAssemblage();
    }

    // ── Getters / Setters ──────────────────────────────────────────────
    public float getEtatPropulseur() {
        return etatPropulseur;
    }

    public float getEtatChargeUtile() {
        return etatChargeUtile;
    }

    public float getEtatCommande() {
        return etatCommande;
    }

    public Propulseur getPropulseur() {
        return propulseur;
    }

    public ChargeUtile getChargeUtile() {
        return chargeUtile;
    }

    public OrdiDeBord getOrdiDeBord() {
        return ordiDeBord;
    }

    public boolean isIngenieurABord() {
        return ingenieurABord;
    }

    public void setIngenieurABord(boolean b) {
        this.ingenieurABord = b;
    }

    /**
     * Retourne true si les 3 modules sont à 100%.
     */
    public boolean tousModulesAssembles() {
        return propulseur.estTermine()
                && chargeUtile.estTermine()
                && ordiDeBord.estTermine();
    }

    @Override
    public String toString() {
        return String.format(
                "Fusee[Propulseur=%.0f%% | ChargeUtile=%.0f%% | OrdiDeBord=%.0f%% | Ingenieur=%s]",
                etatPropulseur * 100,
                etatChargeUtile * 100,
                etatCommande * 100,
                ingenieurABord ? "OUI" : "NON"
        );
    }
}
