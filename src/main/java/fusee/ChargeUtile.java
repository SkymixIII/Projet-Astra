package fusee;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import entites.Role;
import ressources.TypeRessource;

/**
 * Module Charge Utile de la fusée.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class ChargeUtile extends ModuleFusee {

    /**
     * Construit la recette officielle de la Charge Utile.
     */
    private static Map<TypeRessource, Integer> creerRecette() {
        Map<TypeRessource, Integer> r = new EnumMap<>(TypeRessource.class);
        r.put(TypeRessource.PLAQUE_ACIER, 5);             // TODO V2 : POUTRE
        r.put(TypeRessource.CABLAGE, 10);
        r.put(TypeRessource.CARTE_MERE, 5);
        return r;
    }

    /**
     * Constructeur de la Charge Utile. Personnel : 1 ouvrier non spécialisé + 1
     * technicien + 1 scientifique.
     */
    public ChargeUtile() {
        super("Charge Utile",
                creerRecette(),
                // 1 Ouvrier (non spécialisé) + 1 Technicien // TODO V2 + 1 Scientifique (non implémenté dans V1)
                Arrays.asList(Role.OUVRIER_NON_SPECIALISE, Role.TECHNICIEN));
    }
}
