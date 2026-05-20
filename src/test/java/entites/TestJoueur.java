package entites;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.Maison;
import carte.Direction;
import exceptions.RessourceInsuffisanteException;
import ressources.TypeRessource;

public class TestJoueur {

    private Joueur joueur;

    @Before
    public void setUp() {
        joueur = new Joueur(0, 0);
    }

    // ================================================================== //
    //  1. INITIALISATION                                                  //
    // ================================================================== //

    @Test
    public void init_position() {
        assertEquals(0, joueur.getX());
        assertEquals(0, joueur.getY());
    }

    @Test
    public void init_orientationSud() {
        assertEquals(Direction.SUD, joueur.getOrientation());
    }

    @Test
    public void init_stockNonNull() {
        assertNotNull("Le stock du joueur ne doit pas être null", joueur.getStock());
    }

    @Test
    public void init_ouvrierListeVide() {
        assertTrue("La liste des ouvriers doit être vide au départ",
                joueur.getOuvriers().isEmpty());
    }

    @Test
    public void init_batimentListeVide() {
        assertTrue("La liste des bâtiments doit être vide au départ",
                joueur.getBatiments().isEmpty());
    }

    // ================================================================== //
    //  2. RESSOURCES — ajouterRessource / consommerRessource             //
    // ================================================================== //

    @Test
    public void ajouterRessource_augmenteStock() throws Exception {
        joueur.ajouterRessource(TypeRessource.FER, 10);
        // On valide via consommerRessource (indirectement)
        assertTrue(joueur.consommerRessource(TypeRessource.FER, 10));
    }

    @Test
    public void ajouterRessource_deuxFoisCumulatif() throws Exception {
        joueur.ajouterRessource(TypeRessource.BOIS, 5);
        joueur.ajouterRessource(TypeRessource.BOIS, 3);
        // 8 au total, on consomme 8
        assertTrue(joueur.consommerRessource(TypeRessource.BOIS, 8));
    }

    @Test
    public void consommerRessource_stockSuffisant_retourneTrue() throws Exception {
        joueur.ajouterRessource(TypeRessource.PIERRE, 10);
        assertTrue(joueur.consommerRessource(TypeRessource.PIERRE, 5));
    }

    @Test(expected = RessourceInsuffisanteException.class)
    public void consommerRessource_stockInsuffisant_leveException() throws Exception {
        joueur.consommerRessource(TypeRessource.FER, 1);
    }

    @Test(expected = RessourceInsuffisanteException.class)
    public void consommerRessource_tropGrand_leveException() throws Exception {
        joueur.ajouterRessource(TypeRessource.FER, 3);
        joueur.consommerRessource(TypeRessource.FER, 5);
    }

    // ================================================================== //
    //  3. DÉPLACEMENT                                                     //
    // ================================================================== //

    @Test
    public void deplacer_coordonneesAbsolues() {
        joueur.deplacer(7, 3);
        assertEquals(7, joueur.getX());
        assertEquals(3, joueur.getY());
    }

    @Test
    public void deplacer_direction_NORD_decYmoins1() {
        joueur.deplacer(Direction.NORD); // dy = -1
        assertEquals(0, joueur.getX());
        assertEquals(-1, joueur.getY());
    }

    @Test
    public void deplacer_direction_EST_incXplus1() {
        joueur.deplacer(Direction.EST); // dx = +1
        assertEquals(1, joueur.getX());
        assertEquals(0, joueur.getY());
    }

    @Test
    public void deplacer_direction_metAJourOrientation() {
        joueur.deplacer(Direction.NORD);
        assertEquals(Direction.NORD, joueur.getOrientation());
    }

    // ================================================================== //
    //  4. DISTANCE                                                        //
    // ================================================================== //

    @Test
    public void distance_autreJoueur_calculCorrect() {
        Joueur autre = new Joueur(3, 4);
        assertEquals(5.0, joueur.distance(autre), 0.001);
    }

    @Test
    public void distance_memePosition_zero() {
        Joueur autre = new Joueur(0, 0);
        assertEquals(0.0, joueur.distance(autre), 0.001);
    }

    // ================================================================== //
    //  5. LITS DISPONIBLES                                                //
    // ================================================================== //

    @Test
    public void getNombreLitsDisponibles_sansMaison_zero() {
        assertEquals(0, joueur.getNombreLitsDisponibles());
    }

    @Test
    public void getNombreLitsDisponibles_avecMaison_comptePlaces() {
        Maison maison = new Maison("Dortoir", 0, 0, 3);
        joueur.getBatiments().add(maison);
        assertEquals("Une maison vide de capacité 3 doit offrir 3 lits",
                3, joueur.getNombreLitsDisponibles());
    }

    @Test
    public void getNombreLitsDisponibles_maisonPleine_zeroLit() {
        Maison maison = new Maison("Dortoir", 0, 0, 2);
        maison.loger(new Ouvrier("A", 0, 0));
        maison.loger(new Ouvrier("B", 0, 0));
        joueur.getBatiments().add(maison);
        assertEquals("Une maison pleine ne doit offrir aucun lit",
                0, joueur.getNombreLitsDisponibles());
    }
}
