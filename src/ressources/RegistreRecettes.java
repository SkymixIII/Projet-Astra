package ressources;

import java.util.EnumMap;
import java.util.Map;

/**
 * RegistreRecettes — fabrique des Usines préconfigurées.
 * compatible avec le constructeur Usine(nom, x, y, produit, recette).
 */
public class RegistreRecettes {

    private static final RegistreRecettes INSTANCE = new RegistreRecettes();
    public static RegistreRecettes getInstance() { return INSTANCE; }
    private RegistreRecettes() {}

    /**
     * Crée une Usine préconfigurée à partir de son nom et de sa position.
     * 
     * Utilisation :
     *   Usine f = RegistreRecettes.getInstance().creerUsine("FONDERIE", "Fonderie Nord", 10, 5);
     *
     * @param typeNom  Identifiant de l'usine (constantes ci-dessous)
     * @param nom      Nom affiché dans l'interface
     * @param x        Position X sur la carte
     * @param y        Position Y sur la carte
     */
    public Usine creerUsine(String typeNom, String nom, int x, int y) {
        switch (typeNom) {

            // ── ÂGE PRIMITIF ──────────────────────────────────────── //

            case "ATELIER_HACHE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.BOIS, 2);
                e.put(TypeRessource.PIERRE, 1);
                return new Usine(nom, x, y, TypeRessource.HACHE_PIERRE, e);
            }
            case "ATELIER_PIOCHE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.BOIS, 1);
                e.put(TypeRessource.PIERRE, 2);
                return new Usine(nom, x, y, TypeRessource.PIOCHE_PIERRE, e);
            }
            case "SERRE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.EAU, 5);
                e.put(TypeRessource.GRAINE, 5);
                return new Usine(nom, x, y, TypeRessource.NOURRITURE_BRUTE, e);
            }

            // ── ÂGE INDUSTRIEL ────────────────────────────────────── //

            case "FONDERIE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.FER, 3);
                e.put(TypeRessource.ENERGIE, 2);
                return new Usine(nom, x, y, TypeRessource.ACIER, e);
            }
            case "RAFFINERIE_CARBURANT": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.PETROLE, 5);
                e.put(TypeRessource.SILICIUM, 1);
                return new Usine(nom, x, y, TypeRessource.KÉROSENE, e);
            }
            case "RAFFINERIE_PLASTIQUE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.PETROLE, 2);
                return new Usine(nom, x, y, TypeRessource.PLASTIQUE, e);
            }
            case "STATION_POMPAGE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.ENERGIE, 1);
                return new Usine(nom, x, y, TypeRessource.EAU, e);
            }

            // ── ÂGE TECHNOLOGIQUE ─────────────────────────────────── //

            case "USINE_ELECTRONIQUE_CARTE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.SILICIUM, 2);
                e.put(TypeRessource.FER, 1);
                return new Usine(nom, x, y, TypeRessource.CARTE_MERE, e);
            }

            case "USINE_ELECTRONIQUE_CABLAGE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.SILICIUM, 2);
                e.put(TypeRessource.PLASTIQUE, 1);
                return new Usine(nom, x, y, TypeRessource.CABLAGE, e);
            }

            case "USINE_ELECTRONIQUE_PROCESSEUR": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.CARTE_MERE, 2);
                e.put(TypeRessource.ACIER, 1);
                return new Usine(nom, x, y, TypeRessource.PROCESSEUR_VOL, e);
            }

            // ── ÂGE SPATIAL ───────────────────────────────────────── //

            case "FONDERIE_AVANCEE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.ACIER, 2);
                e.put(TypeRessource.SILICIUM, 1);
                return new Usine(nom, x, y, TypeRessource.ALLIAGE_THERMIQUE, e);
            }

            // ── SURVIE ────────────────────────────────────────────── //

            case "CUISINE": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.NOURRITURE_BRUTE, 2);
                e.put(TypeRessource.EAU, 1);
                return new Usine(nom, x, y, TypeRessource.NOURRITURE, e);
            }
            case "CENTRE_TRAITEMENT_EAU": {
                Map<TypeRessource, Integer> e = new EnumMap<>(TypeRessource.class);
                e.put(TypeRessource.EAU, 2);
                e.put(TypeRessource.ENERGIE, 1);
                return new Usine(nom, x, y, TypeRessource.EAU_POTABLE, e);
            }

            default:
                throw new IllegalArgumentException("Type d'usine inconnu : " + typeNom);
        }
    }
}