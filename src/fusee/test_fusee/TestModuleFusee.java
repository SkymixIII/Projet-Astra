package test_fusee;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Map;

import fusee.*;
import entites.Role;
import ressources.TypeRessource;

/**
 * Tests unitaires de {@link ModuleFusee} et de ses sous-classes.
 *
 * @author Binôme 4 (Hadrien & Théo)
 */
public class TestModuleFusee {

    // ================================================================ //
    //  1. INITIALISATION                                                //
    // ================================================================ //
    @Test
    public void propulseur_init_a0() {
        Propulseur p = new Propulseur();
        assertEquals(0.0f, p.getPourcentageAssemblage(), 0.001f);
        assertFalse(p.estTermine());
        assertEquals("Propulseur", p.getNom());
    }

    @Test
    public void chargeUtile_init_a0() {
        ChargeUtile c = new ChargeUtile();
        assertEquals(0.0f, c.getPourcentageAssemblage(), 0.001f);
        assertFalse(c.estTermine());
    }

    @Test
    public void ordiDeBord_init_a0() {
        OrdiDeBord o = new OrdiDeBord();
        assertEquals(0.0f, o.getPourcentageAssemblage(), 0.001f);
        assertFalse(o.estTermine());
    }

    // ================================================================ //
    //  2. ASSEMBLER                                                     //
    // ================================================================ //
    @Test
    public void assembler_progressionLineaire() {
        Propulseur p = new Propulseur();
        p.assembler(0.3f);
        assertEquals(0.3f, p.getPourcentageAssemblage(), 0.001f);
        p.assembler(0.4f);
        assertEquals(0.7f, p.getPourcentageAssemblage(), 0.001f);
    }

    @Test
    public void assembler_plafonneA1() {
        Propulseur p = new Propulseur();
        p.assembler(1.5f);
        assertEquals("Le pourcentage doit etre plafonne a 1.0",
                1.0f, p.getPourcentageAssemblage(), 0.001f);
        assertTrue(p.estTermine());
    }

    @Test
    public void assembler_avancementNegatif_ignore() {
        Propulseur p = new Propulseur();
        p.assembler(0.5f);
        p.assembler(-0.3f);  // Doit etre ignore
        assertEquals(0.5f, p.getPourcentageAssemblage(), 0.001f);
    }

    @Test
    public void estTermine_a100_retourneTrue() {
        Propulseur p = new Propulseur();
        p.assembler(1.0f);
        assertTrue(p.estTermine());
    }

    @Test
    public void estTermine_a99_retourneFalse() {
        Propulseur p = new Propulseur();
        p.assembler(0.99f);
        assertFalse(p.estTermine());
    }

    // ================================================================ //
    //  3. RECETTES                                                      //
    // ================================================================ //
    @Test
    public void propulseur_recetteCorrespondAuCDC() {
        // Recette officielle : 10 poutres acier + 20 kerosene + 5 alliages thermiques
        Propulseur p = new Propulseur();
        Map<TypeRessource, Integer> r = p.getRecette();

        assertEquals(Integer.valueOf(10), r.get(TypeRessource.ACIER));
        assertEquals(Integer.valueOf(20), r.get(TypeRessource.KÉROSENE));
        assertEquals(Integer.valueOf(5), r.get(TypeRessource.ALLIAGE_THERMIQUE));
    }

    @Test
    public void chargeUtile_recetteCorrespondAuCDC() {
        // Recette officielle : 5 poutres acier + 10 cablage + 5 cartes mere
        ChargeUtile c = new ChargeUtile();
        Map<TypeRessource, Integer> r = c.getRecette();

        assertEquals(Integer.valueOf(5), r.get(TypeRessource.ACIER));
        assertEquals(Integer.valueOf(10), r.get(TypeRessource.PLASTIQUE));
        assertEquals(Integer.valueOf(5), r.get(TypeRessource.CARTE_MERE));
    }

    @Test
    public void ordiDeBord_recetteCorrespondAuCDC() {
        // Recette officielle : 2 processeurs vol + 5 cartes mere + 2 alliages thermiques
        OrdiDeBord o = new OrdiDeBord();
        Map<TypeRessource, Integer> r = o.getRecette();

        assertEquals(Integer.valueOf(2), r.get(TypeRessource.PROCESSEUR_VOL));
        assertEquals(Integer.valueOf(5), r.get(TypeRessource.CARTE_MERE));
        assertEquals(Integer.valueOf(2), r.get(TypeRessource.ALLIAGE_THERMIQUE));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void recette_estImmutable() {
        // La recette retournee ne doit pas etre modifiable de l'exterieur
        Propulseur p = new Propulseur();
        p.getRecette().put(TypeRessource.FER, 999);
    }

    // ================================================================ //
    //  4. PERSONNEL REQUIS                                              //
    // ================================================================ //
    @Test
    public void propulseur_rolesRequis() {
        // 2 Techniciens + 1 Scientifique
        Propulseur p = new Propulseur();
        assertEquals(3, p.getRolesRequis().size());
        long techniciens = p.getRolesRequis().stream()
                .filter(r -> r == Role.TECHNICIEN).count();
        long scientifiques = p.getRolesRequis().stream()
                .filter(r -> r == Role.SCIENTIFIQUE).count();
        assertEquals(2, techniciens);
        assertEquals(1, scientifiques);
    }

    @Test
    public void ordiDeBord_rolesRequis() {
        // 2 Ingenieurs + 1 Scientifique
        OrdiDeBord o = new OrdiDeBord();
        long ingenieurs = o.getRolesRequis().stream()
                .filter(r -> r == Role.INGENIEUR).count();
        long scientifiques = o.getRolesRequis().stream()
                .filter(r -> r == Role.SCIENTIFIQUE).count();
        assertEquals(2, ingenieurs);
        assertEquals(1, scientifiques);
    }
}
