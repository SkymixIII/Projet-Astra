package fusee;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import ressources.TypeRessource;
import entites.Role;

/**
 * Module Ordinateur de Bord de la fusée.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class OrdiDeBord extends ModuleFusee {

    /**
     * Construit la recette officielle de l'Ordinateur de Bord.
     */
    private static Map<TypeRessource, Integer> creerRecette() {
        Map<TypeRessource, Integer> r = new EnumMap<>(TypeRessource.class);
        r.put(TypeRessource.PROCESSEUR_VOL, 2);
        r.put(TypeRessource.CARTE_MERE, 5);
        r.put(TypeRessource.ALLIAGE_THERMIQUE, 2);
        return r;
    }

    /**
     * Constructeur de l'Ordinateur de Bord. Personnel : 2 ingénieurs + 1
     * scientifique.
     */
    public OrdiDeBord() {
        super("Ordinateur de Bord",
                creerRecette(),
                // 2 INGENIEUR + 1 SCIENTIFIQUE selon cahier des charges
                Arrays.asList(Role.INGENIEUR, Role.INGENIEUR, Role.SCIENTIFIQUE));
    }
}
