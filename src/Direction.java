/**
 * Représente les six directions cardinales pour les mouvements sur la carte.
 */
public enum Direction {
    NORD(0, -1, 0),
    SUD(0, 1, 0),
    EST(1, 0, 0),
    OUEST(-1, 0, 0),
    HAUT(0, 0, 1),
    BAS(0, 0, -1);

    private final int dx;
    private final int dy;
    private final int dz;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDz() { return dz; }
}