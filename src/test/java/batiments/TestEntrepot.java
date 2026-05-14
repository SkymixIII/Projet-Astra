package batiments;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import ressources.TypeRessource;

public class TestEntrepot {
    private Entrepot hangar;

    @Before
    public void setUp() {
        // Entrepôt avec une capacité de 100 avec x=20, y=20
        hangar = new Entrepot("Hangar de Stockage", 20, 20, 100);
    }

    @Test
    public void testStockerStandard() {
        hangar.stocker(TypeRessource.FER, 40);
        assertEquals("La quantité de fer stockée doit être de 40", 40, hangar.getQuantite(TypeRessource.FER));
        assertEquals("Le volume total occupé doit être de 40", 40, hangar.getVolumeActuel());
    }

    @Test
    public void testDepassementCapacite() {
        hangar.stocker(TypeRessource.BOIS, 80);
        // Test d'ajouter plus de ressource que l'entrepot peut supporter
        hangar.stocker(TypeRessource.FER, 30);
        
        assertEquals("Le fer ne doit pas être ajouté !!", 0, hangar.getQuantite(TypeRessource.FER));
        assertEquals("Le volume occupé doit rester à 80 !", 80, hangar.getVolumeActuel());
    }

    @Test
    public void testTypeBatiment() {
        assertEquals("Le type de bâtiment doit être un ENTREPOT", TypeBatiment.ENTREPOT, hangar.getType());
    }

    @Test
    public void testStockerNegatif() {
        hangar.stocker(TypeRessource.PIERRE, -50);
        assertEquals("On peu pas avoir -50 pls", 0, hangar.getQuantite(TypeRessource.PIERRE));
        assertEquals("Le volume doit être à 0", 0, hangar.getVolumeActuel());
    }
}
