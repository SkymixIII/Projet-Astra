package evenements;

//TODO, ceci est une classe provisoire à refaire si nécéssaire
public class Tempete implements Evenement {
    private final int duree; // en tours
    private final Type type = Type.TEMPETE_INHOSPITALIERE;

    public Tempete(int duree) {
        this.duree = duree;
    }

    @Override
    public Type getType() { return type; }

    @Override
    public Object getDonnees() { return duree; }

    public int getDuree() { return duree; }
}