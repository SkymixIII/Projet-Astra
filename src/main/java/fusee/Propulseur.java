package fusee;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import entites.Role;
import ressources.TypeRessource;

/**
 * Module Propulseur de la fusée.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class Propulseur extends ModuleFusee {

    /**
     * Construit la recette officielle du Propulseur.
     */
    private static Map<TypeRessource, Integer> creerRecette() {
        // EnumMap : plus performant qu'un HashMap pour des clés enum
        Map<TypeRessource, Integer> r = new EnumMap<>(TypeRessource.class);
        r.put(TypeRessource.POUTRE, 10);
        r.put(TypeRessource.KEROSENE, 20);
        r.put(TypeRessource.ALLIAGE_THERMIQUE, 5);
        return r;
    }

    /**
     * Constructeur du Propulseur. Initialise le nom, la recette et les rôles
     * requis (2 Techniciens + 1 Scientifique).
     */
    public Propulseur() {
        super("Propulseur",
                creerRecette(),
                // 2 TECHNICIEN //TODO V2+ 1 SCIENTIFIQUE(non implémenté dans V1) selon cahier des charges
                Arrays.asList(Role.TECHNICIEN, Role.TECHNICIEN));
    }
}
