package test_ressources;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestHopital {
    private Hopital hopital;
    private Ouvrier patient;

    @Before
    public void setUp() {
        // Hôpital avec 100% de réussite pour tester si ça kaputt pas 
        hopital = new Hopital("Hôpital Central", 0, 0, 1.0f);
        patient = new Ouvrier("Patient", 0, 0);
    }

    @Test
    public void testSoignerSuccess() {
        patient.setEtat("MALADE"); // On force l'état malade
        hopital.soigner(patient);
        assertEquals("L'ouvrier devrait être guéri (NORMAL)", "NORMAL", patient.getEtat());
    }

    @Test
    public void testSoignerEchecCondition() {
        patient.setEtat("FATIGUE"); // Pas l'état MALADE
        hopital.soigner(patient);
        assertEquals("L'état ne doit pas changer s'il n'est pas MALADE", "FATIGUE", patient.getEtat());
    }

    @Test
    public void testProbabiliteEchec() {
        // Hôpital avec 0% d'efficacité genre caca
        Hopital mauvaisHopital = new Hopital("hopital NUL", 0, 0, 0.0f);
        patient.setEtat("MALADE");
        mauvaisHopital.soigner(patient);
        assertEquals("L'ouvrier doit rester MALADE avec 0% d'efficacité", "MALADE", patient.getEtat());
    }
}