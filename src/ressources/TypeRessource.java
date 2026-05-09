package ressources;

public enum TypeRessource {

    // Ressources primaires (légères)
    //FER(1),
    MINERAI_FER(1),
    SILICIUM(1),
    BOIS(1),
    PIERRE(1),
    //EAU(2),
    //EAU_POTABLE(2),
    //GRAINE(1),
    //NOURRITURE_BRUTE(2),
    //NOURRITURE(2),
    PETROLE(3),

    // Ressources transformées (plus lourdes)
    //PIOCHE_PIERRE(5),
    //HACHE_PIERRE(5),
    //ACIER(4),
    PLAQUE_ACIER (4),
    KEROSENE(5),
    PLASTIQUE(2),
    CABLAGE(3),
    CARTE_MERE(3),
    PROCESSEUR_VOL(4),
    ALLIAGE_THERMIQUE(6),
    POUTRE(3),
    ENERGIE(0); // immatérielle, ne prend pas de place

    // ------------------------------------------------ //
     /** @param poids unités de volume occupées par 1 exemplaire. */
    private final int poids; 

    TypeRessource(int poids) {
        this.poids = poids;
    }

    public int getPoids() {
        return poids;
    }
}