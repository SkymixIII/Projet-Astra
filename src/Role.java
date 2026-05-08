/**
 * Représente les roles des ouvriers
 */
public enum Role {
    OUVRIER_NON_SPECIALISE,
    MINEUR,
    MACON,
    BUCHERON,
    TECHNICIEN,
    INGENIEUR,
    SCIENTIFIQUE;
    /*SOIGNANT,
    FORMATEUR,
    TRANSPORTEUR,
    FERMIER,*///à décommenter pour la V2

    public String getPole() {
        return switch (this) {
            case OUVRIER_NON_SPECIALISE -> "Général";
            case MINEUR, BUCHERON, MACON -> "Production brute";
            case TECHNICIEN, INGENIEUR  -> "Industriel";
            case SCIENTIFIQUE -> "Recherche";
            /*case TRANSPORTEUR-> "Logistique";
            case FERMIER -> "Subsistance"
            case SOIGNANT -> "Sante/social"
            case FORMATEUR -> "Education";*///à décommenter pour la V2
        };
    }
}
