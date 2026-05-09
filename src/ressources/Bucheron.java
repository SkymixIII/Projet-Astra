package ressources;

/**
 * Métier : BUCHERON
 *
 * Extrait du BOIS en FORET.
 *   Cadence : 1 bois tous les 5 ticks (× efficacité de l'ouvrier)
 *   Niveau minimum : DEBUTANT
 */
public class Bucheron implements Metier {

    private static final int CADENCE_BOIS = 5;

    private double ticksAccumules = 0;

    // -------------------------------------------------------------------------

    @Override
    public Ouvrier.TypeMetier getType() {
        return Ouvrier.TypeMetier.BUCHERON;
    }

    @Override
    public String getNomAffichage() {
        return "Bûcheron";
    }

    @Override
    public boolean peutTravaillerDans(Batiment batiment) {
        return batiment.getType() == TypeBatiment.FORET;
    }

    @Override
    public void travailler(Ouvrier ouvrier, Batiment batiment, Stock stock, int ticks) {

        if (!batiment.isOperationnel()) return;
        if (batiment.getType() != TypeBatiment.FORET) return;

        ticksAccumules += ticks * ouvrier.getEfficacite();

        while (ticksAccumules >= CADENCE_BOIS) {
            ticksAccumules -= CADENCE_BOIS;
            try {
                stock.ajouter(TypeRessource.BOIS, 1);
            } catch (StockException e) {
                System.out.println("[Bûcheron] Stock de BOIS plein — ressource perdue.");
            }
        }

        ouvrier.travailler(ticks);
    }
}