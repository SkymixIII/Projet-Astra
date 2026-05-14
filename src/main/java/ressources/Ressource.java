package ressources;

public class Ressource {
    private TypeRessource type;
    private int quantite;

    public Ressource(TypeRessource type, int quantite) {
        this.type = type;
        this.quantite = quantite;
    }

    public TypeRessource getType() { return this.type; }
    public int getQuantite() { return this.quantite; }
}