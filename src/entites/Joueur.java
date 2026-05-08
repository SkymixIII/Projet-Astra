package entites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ressources.TypeRessource;
import carte.Direction;
import batiments.*;
import exceptions.RessourceInsuffisanteException;
import carte.Item;

/**
 * Représente l'entité contrôlée par l'utilisateur
 * Le joueur possède un inventaire de ressources et gère ses unités/bâtiments.
 */
public class Joueur implements Item {
    private int x, y, z;
    private Direction orientation;
    private Map<TypeRessource, Integer> ressources;
    private List<Ouvrier> ouvriers;
    private List<Batiment> batiments;

    /**
     * Constructeur pour initialiser un nouveau joueur avec les ressources de base
     */
    public Joueur(int xInitial, int yInitial) {
        this.x = xInitial;
        this.y = yInitial;
        this.z = 0;
        this.orientation = Direction.SUD;
        this.ressources = new HashMap<>();
        this.ouvriers = new ArrayList<>();
        this.batiments = new ArrayList<>();

        // Initialisation de l'inventaire vide pour chaque type de ressource
        for(TypeRessource type : TypeRessource.values()) {
            this.ressources.put(type, 0);
        }
    }

    // --- Implémentation des méthodes de l'interface Item ---

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public double distance(Item autre) {
        return Math.sqrt(Math.pow(autre.getX() - this.x, 2) + Math.pow(autre.getY() - this.y, 2));
    }

    /**
     * Déplacement vers des coordonnées absolues (Téléportation ou placement initial)
     */
    @Override
    public void deplacer(int nouveauX, int nouveauY) {
        this.x = nouveauX;
        this.y = nouveauY;
    }

    /**
     * Déplacement relatif d'une case selon une direction.
     * Met également à jour l'orientation du joueur
     */
    @Override
    public void deplacer(Direction direction) {
        this.orientation = direction;
        this.x += direction.getDx();
        this.y += direction.getDy();
        this.z += direction.getDz();
        System.out.println("Le joueur se déplace vers le " + direction + "(Pos : " + x + "," + y +")");
    }

    // --- Gestion des ressources ---

    public void ajouterRessource(TypeRessource type, int quantite) {
        assert quantite > 0;
        this.ressources.put(type, this.ressources.get(type) + quantite);
    }

    public boolean consommerRessource(TypeRessource type, int quantite) throws RessourceInsuffisanteException {
        assert quantite > 0;
        int stockActuel = this.ressources.get(type);
        if (stockActuel < quantite) {
            throw new RessourceInsuffisanteException("Manque de " + type + " (" + stockActuel + "/" + quantite + ")");
        }
        this.ressources.put(type, stockActuel - quantite);
        return true;
    }

    // --- Getters & Setters

    public List<Batiment> getBatiments() { return batiments; }
    public List<Ouvrier> getOuvriers() { return ouvriers; }
    public Direction getOrientation() { return orientation; }
}
