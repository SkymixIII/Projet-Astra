package carte;

/**
 * Représente un type de sol/terrain sur une case de la carte.
 * Immuable et immobile.
 */
public class Sol implements Item {

    private final TypeSol type;
    private final int x;
    private final int y;

    public Sol(TypeSol type) {
        this.type = type;
        this.x = 0;
        this.y = 0;
    }

    public Sol(TypeSol type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public TypeSol getType() {
        return type;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public double distance(Item autreItem) {
        return Math.sqrt(Math.pow(autreItem.getX() - this.x, 2) + Math.pow(autreItem.getY() - this.y, 2));
    }

    @Override
    public void deplacer(int x, int y) {
        // Le sol est immobile
    }

    @Override
    public void deplacer(Direction direction) {
        // Le sol est immobile
    }

    @Override
    public String toString() {
        return "Sol[" + type + "]";
    }
}
