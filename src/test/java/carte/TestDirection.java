package carte;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestDirection {

    // ================================================================== //
    //  1. DELTAS CARDINAUX                                                //
    // ================================================================== //

    @Test
    public void nord_dx0_dyMoins1_dz0() {
        assertEquals( 0, Direction.NORD.getDx());
        assertEquals(-1, Direction.NORD.getDy());
        assertEquals( 0, Direction.NORD.getDz());
    }

    @Test
    public void sud_dx0_dyPlus1_dz0() {
        assertEquals( 0, Direction.SUD.getDx());
        assertEquals( 1, Direction.SUD.getDy());
        assertEquals( 0, Direction.SUD.getDz());
    }

    @Test
    public void est_dxPlus1_dy0_dz0() {
        assertEquals( 1, Direction.EST.getDx());
        assertEquals( 0, Direction.EST.getDy());
        assertEquals( 0, Direction.EST.getDz());
    }

    @Test
    public void ouest_dxMoins1_dy0_dz0() {
        assertEquals(-1, Direction.OUEST.getDx());
        assertEquals( 0, Direction.OUEST.getDy());
        assertEquals( 0, Direction.OUEST.getDz());
    }

    @Test
    public void haut_dx0_dy0_dzPlus1() {
        assertEquals( 0, Direction.HAUT.getDx());
        assertEquals( 0, Direction.HAUT.getDy());
        assertEquals( 1, Direction.HAUT.getDz());
    }

    @Test
    public void bas_dx0_dy0_dzMoins1() {
        assertEquals( 0, Direction.BAS.getDx());
        assertEquals( 0, Direction.BAS.getDy());
        assertEquals(-1, Direction.BAS.getDz());
    }

    // ================================================================== //
    //  2. OPPOSÉS COHÉRENTS                                               //
    // ================================================================== //

    @Test
    public void nord_et_sud_opposes() {
        assertEquals(0, Direction.NORD.getDx() + Direction.SUD.getDx());
        assertEquals(0, Direction.NORD.getDy() + Direction.SUD.getDy());
    }

    @Test
    public void est_et_ouest_opposes() {
        assertEquals(0, Direction.EST.getDx() + Direction.OUEST.getDx());
        assertEquals(0, Direction.EST.getDy() + Direction.OUEST.getDy());
    }

    @Test
    public void haut_et_bas_opposes() {
        assertEquals(0, Direction.HAUT.getDz() + Direction.BAS.getDz());
    }

    // ================================================================== //
    //  3. NOMBRE DE DIRECTIONS                                            //
    // ================================================================== //

    @Test
    public void sixDirections() {
        assertEquals("Il doit y avoir 6 directions", 6, Direction.values().length);
    }
}
