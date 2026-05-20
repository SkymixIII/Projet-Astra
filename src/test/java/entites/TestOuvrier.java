package entites;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import batiments.Batiment;
import carte.Direction;
import metiers.Metier;
import ressources.Stock;

public class TestOuvrier {

    private Ouvrier ouvrier;

    /** Métier factice neutre. */
    private static Metier metierBidon(Role role) {
        return new Metier() {
            @Override public Role getType()                                          { return role; }
            @Override public boolean peutTravaillerDans(Batiment b)                 { return false; }
            @Override public void travailler(Ouvrier o, Batiment b, Stock s, int t) {}
            @Override public String getNomAffichage()                               { return role.name(); }
        };
    }

    @Before
    public void setUp() {
        ouvrier = new Ouvrier("Bob", 0, 0);
    }

    // ================================================================== //
    //  1. INITIALISATION                                                  //
    // ================================================================== //

    @Test
    public void init_etatNormal() {
        assertEquals(EtatOuvrier.NORMAL, ouvrier.getEtat());
    }

    @Test
    public void init_niveauDebutant() {
        assertEquals(Ouvrier.NiveauExp.DEBUTANT, ouvrier.getNiveau());
    }

    @Test
    public void init_sansMetier() {
        assertNull("L'ouvrier ne doit pas avoir de métier au départ", ouvrier.getMetier());
    }

    @Test
    public void init_sansPoste() {
        assertNull("L'ouvrier ne doit pas avoir de poste au départ", ouvrier.getPosteActuel());
    }

    @Test
    public void init_moralUnite() {
        assertEquals(1.0, ouvrier.getMoral(), 0.001);
    }

    // ================================================================== //
    //  2. EFFICACITÉ                                                      //
    // ================================================================== //

    @Test
    public void efficacite_debutantNormal_attendue() {
        // DEBUTANT(0.7) × NORMAL(1.0)
        assertEquals(0.7, ouvrier.getEfficacite(), 0.001);
    }

    @Test
    public void efficacite_debutantFatigue_reduite() {
        ouvrier.setEtat(EtatOuvrier.FATIGUE);
        // 0.7 × 0.8
        assertEquals(0.56, ouvrier.getEfficacite(), 0.001);
    }

    @Test
    public void efficacite_debutantMotive_augmentee() {
        ouvrier.setEtat(EtatOuvrier.MOTIVE);
        // 0.7 × 1.2
        assertEquals(0.84, ouvrier.getEfficacite(), 0.001);
    }

    // ================================================================== //
    //  3. EXPÉRIENCE — travailler() et montée de niveau                  //
    // ================================================================== //

    @Test
    public void travailler_sansMetier_aucunEffect() {
        ouvrier.travailler(10_000);
        assertEquals(Ouvrier.NiveauExp.DEBUTANT, ouvrier.getNiveau());
    }

    @Test
    public void travailler_avecMetier_progresseAuNiveauSuivant() {
        ouvrier.setMetier(metierBidon(Role.MINEUR));
        // Seuil DEBUTANT = 600 ticks
        ouvrier.travailler(600);
        assertEquals(Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
    }

    @Test
    public void travailler_ticksInferieurSeuil_resteDebutant() {
        ouvrier.setMetier(metierBidon(Role.MINEUR));
        ouvrier.travailler(599);
        assertEquals(Ouvrier.NiveauExp.DEBUTANT, ouvrier.getNiveau());
    }

    @Test
    public void travailler_progressionSequentielle_apprentiPuisConfirme() {
        ouvrier.setMetier(metierBidon(Role.BUCHERON));
        ouvrier.travailler(600);   // → APPRENTI
        assertEquals(Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
        ouvrier.travailler(1200);  // → CONFIRME
        assertEquals(Ouvrier.NiveauExp.CONFIRME, ouvrier.getNiveau());
    }

    @Test
    public void travailler_niveauMAITRE_nePasDepasser() {
        ouvrier.setMetier(metierBidon(Role.INGENIEUR));
        ouvrier.travailler(600);    // APPRENTI
        ouvrier.travailler(1200);   // CONFIRME
        ouvrier.travailler(1800);   // EXPERT
        ouvrier.travailler(3000);   // MAITRE
        ouvrier.travailler(999_999);// doit rester MAITRE
        assertEquals(Ouvrier.NiveauExp.MAITRE, ouvrier.getNiveau());
    }

    // ================================================================== //
    //  4. CHANGEMENT DE MÉTIER                                            //
    // ================================================================== //

    @Test
    public void setMetier_changementDeType_reinitialisExperience() {
        ouvrier.setMetier(metierBidon(Role.MINEUR));
        ouvrier.travailler(600); // → APPRENTI
        ouvrier.setMetier(metierBidon(Role.BUCHERON)); // autre type
        assertEquals("Changement de type de métier : retour à DEBUTANT",
                Ouvrier.NiveauExp.DEBUTANT, ouvrier.getNiveau());
    }

    @Test
    public void setMetier_memetype_conserveNiveau() {
        ouvrier.setMetier(metierBidon(Role.MINEUR));
        ouvrier.travailler(600); // → APPRENTI
        ouvrier.setMetier(metierBidon(Role.MINEUR)); // même type
        assertEquals("Même type de métier : niveau conservé",
                Ouvrier.NiveauExp.APPRENTI, ouvrier.getNiveau());
    }

    @Test
    public void setMetier_null_effaceMetier() {
        ouvrier.setMetier(metierBidon(Role.MINEUR));
        ouvrier.setMetier(null);
        assertNull(ouvrier.getMetier());
    }

    // ================================================================== //
    //  5. MORAL ET ÉTAT                                                   //
    // ================================================================== //

    @Test
    public void modifierMoral_borneMax() {
        ouvrier.modifierMoral(+5.0);
        assertEquals(1.0, ouvrier.getMoral(), 0.001);
    }

    @Test
    public void modifierMoral_borneMin() {
        ouvrier.modifierMoral(-5.0);
        assertEquals(0.0, ouvrier.getMoral(), 0.001);
    }

    @Test
    public void mettreAJourEtat_moralEleve_tresMotive() {
        // Moral initial = 1.0 ≥ 0.9 → TRES_MOTIVE
        ouvrier.mettreAJourEtat();
        assertEquals(EtatOuvrier.TRES_MOTIVE, ouvrier.getEtat());
    }

    @Test
    public void mettreAJourEtat_moralBas_malade() {
        ouvrier.modifierMoral(-1.0); // moral → 0.0 < 0.1
        ouvrier.mettreAJourEtat();
        assertEquals(EtatOuvrier.MALADE, ouvrier.getEtat());
    }

    @Test
    public void mettreAJourEtat_moral05_normal() {
        ouvrier.modifierMoral(-0.5); // moral → 0.5 ∈ [0.5, 0.7[
        ouvrier.mettreAJourEtat();
        assertEquals(EtatOuvrier.NORMAL, ouvrier.getEtat());
    }

    @Test
    public void mettreAJourEtat_moral03_fatigue() {
        ouvrier.modifierMoral(-0.7); // moral → 0.3 ∈ [0.3, 0.5[
        ouvrier.mettreAJourEtat();
        assertEquals(EtatOuvrier.FATIGUE, ouvrier.getEtat());
    }

    // ================================================================== //
    //  6. BESOINS VITAUX                                                  //
    // ================================================================== //

    @Test
    public void signalerManqueNourriture_degradeMoral() {
        double moralAvant = ouvrier.getMoral();
        ouvrier.signalerManqueNourriture();
        assertTrue("Un manque de nourriture doit baisser le moral",
                ouvrier.getMoral() < moralAvant);
    }

    @Test
    public void signalerManqueEau_degradeMoral() {
        double moralAvant = ouvrier.getMoral();
        ouvrier.signalerManqueEau();
        assertTrue("Un manque d'eau doit baisser le moral",
                ouvrier.getMoral() < moralAvant);
    }

    @Test
    public void setAMangeEtBu_reinitialiseFaimEtSoif() {
        ouvrier.signalerManqueNourriture();
        ouvrier.signalerManqueEau();
        ouvrier.setAMangeEtBu(true);
        assertEquals(0.0, ouvrier.getFaim(), 0.001);
        assertEquals(0.0, ouvrier.getSoif(), 0.001);
    }

    @Test
    public void recupererNuit_augmenteMoral() {
        ouvrier.modifierMoral(-0.5); // moral → 0.5
        double moralAvant = ouvrier.getMoral();
        ouvrier.recupererNuit();
        assertTrue("La nuit améliore le moral", ouvrier.getMoral() > moralAvant);
    }

    // ================================================================== //
    //  7. DÉPLACEMENT                                                     //
    // ================================================================== //

    @Test
    public void deplacer_coordonneesAbsolues() {
        ouvrier.deplacer(5, 8);
        assertEquals(5, ouvrier.getX());
        assertEquals(8, ouvrier.getY());
    }

    @Test
    public void deplacer_direction_EST() {
        ouvrier.deplacer(Direction.EST); // dx=+1
        assertEquals(1, ouvrier.getX());
        assertEquals(0, ouvrier.getY());
    }

    @Test
    public void deplacer_direction_SUD() {
        ouvrier.deplacer(Direction.SUD); // dy=+1
        assertEquals(0, ouvrier.getX());
        assertEquals(1, ouvrier.getY());
    }

    // ================================================================== //
    //  8. DISTANCE                                                        //
    // ================================================================== //

    @Test
    public void distance_memePosition_zero() {
        Ouvrier autre = new Ouvrier("Alice", 0, 0);
        assertEquals(0.0, ouvrier.distance(autre), 0.001);
    }

    @Test
    public void distance_pythagoricienne_3_4_5() {
        Ouvrier autre = new Ouvrier("Alice", 3, 4);
        assertEquals(5.0, ouvrier.distance(autre), 0.001);
    }
}
