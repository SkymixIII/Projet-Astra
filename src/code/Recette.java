package code;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Recette {

    private final Map<TypeRessource, Integer> entrees;
    private final TypeRessource sortieType;
    private final int sortieQuantite;
    private final int dureeTicks;

    public Recette(Map<TypeRessource, Integer> entrees,
                   TypeRessource sortieType,
                   int sortieQuantite,
                   int dureeTicks) {

        if (sortieType == null)   throw new IllegalArgumentException("sortieType requis");
        if (sortieQuantite <= 0)  throw new IllegalArgumentException("sortieQuantite > 0");
        if (dureeTicks <= 0)      throw new IllegalArgumentException("dureeTicks > 0");

        // Copie défensive : on protège la recette contre toute modification extérieure
        this.entrees        = Collections.unmodifiableMap(new EnumMap<>(entrees));
        this.sortieType     = sortieType;
        this.sortieQuantite = sortieQuantite;
        this.dureeTicks     = dureeTicks;
    }

    // Accesseurs — identiques à avant
    public Map<TypeRessource, Integer> getEntrees()  { return entrees; }
    public TypeRessource getSortieType()             { return sortieType; }
    public int getSortieQuantite()                   { return sortieQuantite; }
    public int getDureeTicks()                       { return dureeTicks; }

    @Override
    public String toString() {
        return entrees + " --> " + sortieQuantite + "x " + sortieType
               + " [" + dureeTicks + " ticks]";
    }
}