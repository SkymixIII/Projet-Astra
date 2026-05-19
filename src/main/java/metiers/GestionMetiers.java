package metiers;

import entites.Role;

/**
 * Fabrique de métiers.
 * Centralise la création des instances de Metier pour éviter de dupliquer
 * le switch dans Jeu ou ailleurs.
 *
 * Usage : Metier m = GestionMetiers.creer(Role.TECHNICIEN);
 */
public class GestionMetiers {

    private GestionMetiers() {} // classe utilitaire, non instanciable

    /**
     * Crée une nouvelle instance du métier correspondant au rôle.
     * Retourne null si le rôle ne correspond à aucun métier jouable (ex : SCIENTIFIQUE
     * n'a pas de classe dédiée en V1).
     */
    public static Metier creer(Role role) {
        switch (role) {
            case MINEUR:     return new Mineur();
            case BUCHERON:   return new Bucheron();
            case MACON:      return new Macon();
            case TECHNICIEN: return new Technicien();
            case INGENIEUR:  return new Ingenieur();
            default:         return null; // TODO V2: SCIENTIFIQUE, OUVRIER_NON_SPECIALISE, etc.
        }
    }

    /**
     * Convertit un nom textuel (insensible à la casse) en Role.
     * Retourne null si le nom ne correspond à aucun rôle connu.
     */
    public static Role roleDepuisNom(String nom) {
        try {
            return Role.valueOf(nom.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
