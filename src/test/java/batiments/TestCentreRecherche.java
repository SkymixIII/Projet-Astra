package batiments.test_batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.CentreRecherche;
import batiments.TypeBatiment;
import entites.Ouvrier;

public class TestCentreRecherche {
    private CentreRecherche cnrs;
    private Ouvrier chercheur1;

    @Before
    public void setUp() {
        // Centre avec une vitesse de recherche de 1.5 pour aller vite
        cnrs = new CentreRecherche("Station Lab", 40, 40, 1.5f);
        chercheur1 = new Ouvrier("ClaraTrèsTrèsSmart", 40, 40);
    }

    @Test
    public void testContributionStandard() {
        cnrs.affecterPersonnel(chercheur1);
        cnrs.contribuerRecherche(10.0f);
        // Progression attendue : 10 * 1.5 (vitesse) * 1 (chercheur) = 15 si je sais compter
        assertEquals("La progression doit être proportionnelle à la vitesse et aux ouvriers", 
                     15.0f, cnrs.getProgressionTechno(), 0.01);
    }

    @Test
    public void testSansOuvrier() {
        // On ne m'affecte pas 
        cnrs.contribuerRecherche(10.0f);
        assertEquals("Sans ouvrier, la recherche ne doit pas progresser", 0.0f, cnrs.getProgressionTechno(), 0.01);
        assertFalse("Le centre ne doit pas être opérationnel sans personnel", cnrs.isOperationnel());
    }

    @Test
    public void testTypeBatiment() {
        assertEquals("Le type doit être CENTRE_RECHERCHE", TypeBatiment.CENTRE_RECHERCHE, cnrs.getType());
    }

    @Test
    public void testContributionNulOuNegative() {
        cnrs.affecterPersonnel(chercheur1);
        cnrs.contribuerRecherche(-5.0f);
        assertEquals("Une contribution négative ne doit avoir aucun effet", 
                     0.0f, cnrs.getProgressionTechno(), 0.01);
    }
}
