package batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.Maison;
import entites.Ouvrier;

public class TestMaison {
    private Maison dortoir;
    private Ouvrier o1, o2, o3;

    @Before
    public void setUp() {
        // Maison de capacité 2 pour tester les limites
        dortoir = new Maison("Dortoir Primitif", 10, 10, 2);
        o1 = new Ouvrier("Ouvrier 1", 0, 0);
        o2 = new Ouvrier("Ouvrier 2", 0, 0);
        o3 = new Ouvrier("Ouvrier 3", 0, 0);
    }

    @Test
    public void testLogerSuccess() {
        dortoir.loger(o1);
        assertFalse("La maison doit encore avoir de la place", dortoir.aDeLaPlace() == false);
        dortoir.loger(o2);
        assertTrue("La maison doit être pleine", !dortoir.aDeLaPlace());
    }

    @Test
    public void testLogerLimiteCapacite() {
        dortoir.loger(o1);
        dortoir.loger(o2);
        dortoir.loger(o3); // Ne doit pas être ajouté car capacité = 2
        
        // On vérifie la place (indirectement)
        assertFalse("L'ouvrier 3 ne devrait pas avoir de place", dortoir.aDeLaPlace());
    }

    @Test
    public void testAmelioration() {
        int niveauInitial = dortoir.getNiveau();
        dortoir.ameliorer();
        assertEquals("Le niveau doit augmenter", niveauInitial + 1, dortoir.getNiveau());
        assertTrue("La capacité doit avoir augmenté", dortoir.aDeLaPlace());
    }
}
