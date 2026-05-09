package evenements;

public interface Evenement {
    enum Type {
        GREVE,
        TEMPETE_INHOSPITALIERE,
        DECOUVERTE_GISEMENTS,
        AVANCEE_TECHNOLOGIQUE
    }

    Type getType();
    Object getDonnees(); // payload flexible pour l'ébauche
}