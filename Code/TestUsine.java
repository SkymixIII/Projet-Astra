package code;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

public class TestUsine {
    private Stock stock;
    private Usine fonderie;

    @Before
    public void setUp() throws Exception {
        stock = new Stock(false); // Mode facile
        stock.ajouter(TypeRessource.FER, 20);
        
        Map<TypeRessource, Integer> recette = new HashMap<>();
        recette.put(TypeRessource.FER, 10);
        fonderie = new Usine("Fonderie Alpha", 10, 10, TypeRessource.ACIER, recette);
    }

    @Test(expected = RessourceInsuffisanteException.class)
    public void testProductionSansPersonnel() throws RessourceInsuffisanteException {
        fonderie.produire(TypeRessource.ACIER, 1, stock);
    }

    @Test
    public void testProductionAvecPersonnel() throws RessourceInsuffisanteException {
        Ouvrier jean = new Ouvrier("Jean", 10, 10);
        fonderie.affecterPersonnel(jean);
        
        // Simule 600 secondes pour être sûr de finir (600 * 0.7 = 420 points de progrès)
        fonderie.mettreAJour(stock, 600); 
        
        assertEquals(1, stock.getQuantite(TypeRessource.ACIER));
        assertEquals(10, stock.getQuantite(TypeRessource.FER));
    }
}