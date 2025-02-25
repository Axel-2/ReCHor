package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void exempleDuSite() {

        // pour un payload vide
        // on ajoute 32 bits à
        long expected = (long) 0b00000000_00000001_10000110_00000010L << 32;

        // 8H00 depuis miniuit
        int a = 8 * 60;

        // 9h depuis minuit
        int b = 9 * 60;

        int changements = 2;

        long actual = PackedCriteria.pack(b, 2, 0);

        assertEquals(expected, actual);
    }

    @Test
    void hasdDepMinsFalse() {
        long packedValue = 0b00000000_00000001_10000110_00000010L << 32;

        boolean value = PackedCriteria.hasDepMins(packedValue);

        // Cela doit être faux car il n y a pas d'heure dans packedValue
        assertFalse(value);
    }

    @Test
    void hadDepsMinsTrue() {

        long packedValue = 0b0_110100101111_001100001100_0000010L << 32;

        boolean value = PackedCriteria.hasDepMins(packedValue);

        assertTrue(value);
    }

}
