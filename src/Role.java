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
    /*SOIGNANT,
    SCIENTIFIQUE,
    FORMATEUR,
    TRANSPORTEUR,
    FERMIER,*///à décommenter pour la V2

    public String getPole() {
        return switch (this) {
            case MINEUR, BUCHERON, MACON -> "Production brute";
            case TECHNICIEN, INGENIEUR  -> "Industriel";
            /*case TRANSPORTEUR-> "Logistique";
            case FERMIER -> "Subsistance"
            case SOIGNANT -> "Sante/social"
            case SCIENTIFIQUE -> "Recherche";
            case FORMATEUR -> "Education";*///à décommenter pour la V2
        };
    }
}
