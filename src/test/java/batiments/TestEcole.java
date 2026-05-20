package batiments;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import entites.Ouvrier;
import entites.Role;
import metiers.Metier;
import ressources.Stock;

public class TestEcole {

    private Ecole ecole;
    private Ouvrier chercheur;
    private Ouvrier ouvrierSansMétier;

    /** Métier factice avec nom "Chercheur" (attendu par Ecole.mettreAJour). */
    private static Metier metierChercheur() {
        return new Metier() {
            @Override public Role getType()                                    { return Role.SCIENTIFIQUE; }
            @Override public boolean peutTravaillerDans(Batiment b)            { return b.getType() == TypeBatiment.ECOLE; }
            @Override public void travailler(Ouvrier o, Batiment b, Stock s, int t) {}
            @Override public String getNomAffichage()                          { return "Chercheur"; }
        };
    }

    @Before
    public void setUp() {
        // École avec progressionRecherche = 0.5
        ecole           = new Ecole("École Centrale", 5, 5, 0.5f);
        chercheur       = new Ouvrier("Einstein", 0, 0);
        ouvrierSansMétier = new Ouvrier("Rookie", 0, 0);
        chercheur.setMetier(metierChercheur());
    }

    // ================================================================== //
    //  1. TYPE DE BÂTIMENT                                                //
    // ================================================================== //

    @Test
    public void testTypeBatiment() {
        assertEquals("Le type doit être ECOLE", TypeBatiment.ECOLE, ecole.getType());
    }

    // ================================================================== //
    //  2. GESTION DU PERSONNEL                                            //
    // ================================================================== //

    @Test
    public void testAffecterPersonnel_ajoute() {
        ecole.affecterPersonnel(chercheur);
        assertEquals("L'ouvrier doit être affecté à l'école", ecole, chercheur.getPosteActuel());
    }

    @Test
    public void testAffecterPersonnel_pasDeDoblon() {
        ecole.affecterPersonnel(chercheur);
        ecole.affecterPersonnel(chercheur);
        // Capacité max = 5 ; si doublon ajouté, place < 4 au lieu de 4
        // On ajoute 4 autres ouvriers : si le chercheur compte 2 fois, la 5e place est prise
        for (int i = 0; i < 4; i++) ecole.affecterPersonnel(new Ouvrier("Eleve" + i, 0, 0));
        assertFalse("Avec le chercheur + 4 autres, l'école doit être pleine", ecole.aDeLaPlace());
    }

    @Test
    public void testRetirerPersonnel_liberePlace() {
        ecole.affecterPersonnel(chercheur);
        ecole.retirerPersonnel(chercheur);
        assertNull("Le poste doit être libéré", chercheur.getPosteActuel());
        assertTrue("L'école doit à nouveau avoir de la place", ecole.aDeLaPlace());
    }

    @Test
    public void testADeLaPlace_capaciteMaxAtteinte() {
        for (int i = 0; i < 5; i++) {
            ecole.affecterPersonnel(new Ouvrier("Eleve" + i, 0, 0));
        }
        assertFalse("L'école doit être pleine à 5 élèves (capacité max)", ecole.aDeLaPlace());
    }

    @Test
    public void testADeLaPlace_vide() {
        assertTrue("L'école vide doit avoir de la place", ecole.aDeLaPlace());
    }

    // ================================================================== //
    //  3. OPÉRATIONALITÉ                                                  //
    // ================================================================== //

    @Test
    public void testIsOperationnel_toujours() {
        assertTrue("L'école doit toujours être opérationnelle", ecole.isOperationnel());
    }

    // ================================================================== //
    //  4. MISE À JOUR — expérience chercheur                              //
    // ================================================================== //

    @Test
    public void testMettreAJour_chercheurGagneExperience() {
        ecole.affecterPersonnel(chercheur);
        // 600 ticks * (1.0 + 0.5 progressionRecherche) = 900 ticks → dépasse le seuil DEBUTANT (600)
        ecole.mettreAJour(null, 600);
        assertNotEquals("Le chercheur doit avoir progressé au-delà du niveau DEBUTANT",
                Ouvrier.NiveauExp.DEBUTANT, chercheur.getNiveau());
    }

    @Test
    public void testMettreAJour_ouvrierSansMetier_sansEffet() {
        ecole.affecterPersonnel(ouvrierSansMétier);
        Ouvrier.NiveauExp niveauAvant = ouvrierSansMétier.getNiveau();
        ecole.mettreAJour(null, 10_000);
        assertEquals("Un ouvrier sans métier ne doit pas progresser",
                niveauAvant, ouvrierSansMétier.getNiveau());
    }

    @Test
    public void testMettreAJour_sansEleve_aucunEffet() {
        // Pas d'élève → ne doit pas planter
        ecole.mettreAJour(null, 500);
    }

    // ================================================================== //
    //  5. POSITION & NOM                                                  //
    // ================================================================== //

    @Test
    public void testGetNom() {
        assertEquals("École Centrale", ecole.getNom());
    }

    @Test
    public void testGetPosition() {
        assertEquals(5, ecole.getX());
        assertEquals(5, ecole.getY());
    }

    @Test
    public void testDeplacer() {
        ecole.deplacer(10, 20);
        assertEquals(10, ecole.getX());
        assertEquals(20, ecole.getY());
    }
}
