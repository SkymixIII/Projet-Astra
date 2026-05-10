package ressources;

import static org.junit.Assert.*;
import org.junit.Test;

import batiments.Usine;
import ressources.*;

public class TestRegistreRecettes {

    @Test
    public void testSingleton() {
        RegistreRecettes r1 = RegistreRecettes.getInstance();
        RegistreRecettes r2 = RegistreRecettes.getInstance();

        assertSame(r1, r2);
    }

    @Test
    public void testCreationFonderie() {
        RegistreRecettes registre = RegistreRecettes.getInstance();

        Usine usine = registre.creerUsine(
                "FONDERIE",
                "Fonderie Nord",
                10,
                20
        );

        assertNotNull(usine);

        assertEquals(10, usine.getX());
        assertEquals(20, usine.getY());

        assertEquals(10, usine.getConsommationEnergie());
        assertEquals(0, usine.getProductionEnergie());

        assertFalse(usine.isOperationnel());
    }

    @Test
    public void testCreationCuisine() {
        RegistreRecettes registre = RegistreRecettes.getInstance();

        Usine cuisine = registre.creerUsine(
                "CUISINE",
                "Cuisine Centrale",
                5,
                8
        );

        assertNotNull(cuisine);

        assertEquals(5, cuisine.getX());
        assertEquals(8, cuisine.getY());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypeInconnu() {
        RegistreRecettes registre = RegistreRecettes.getInstance();

        registre.creerUsine(
                "USINE_INCONNUE",
                "Bug Factory",
                0,
                0
        );
    }

    @Test
    public void testDeplacementUsine() {
        RegistreRecettes registre = RegistreRecettes.getInstance();

        Usine usine = registre.creerUsine(
                "SERRE",
                "Serre Sud",
                1,
                1
        );

        usine.deplacer(7, 9);

        assertEquals(7, usine.getX());
        assertEquals(9, usine.getY());
    }

    @Test
    public void testAmeliorationUsine() {
        RegistreRecettes registre = RegistreRecettes.getInstance();

        Usine usine = registre.creerUsine(
                "ATELIER_HACHE",
                "Atelier",
                0,
                0
        );

        assertEquals(1, usine.getNiveau());

        usine.ameliorer();

        assertEquals(2, usine.getNiveau());
        assertEquals(20, usine.getConsommationEnergie());
    }
}
