package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void packedWorksOnWebsiteExample() {

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

    @Test
    void withoutDepMinsWorks(){
        long criteria = 0b01011001_10111000_10000110_00000010_11110000_00001111_10101010_01010101L;

        long actual = PackedCriteria.withoutDepMins(criteria);
        long expected = 0b00000000_00000000_10000110_00000010_11110000_00001111_10101010_01010101L;

        assertEquals(actual, expected);

    }

    @Test
    void withDepMinsWorks(){

        long criteria = 0b00000000_00000000_10000110_00000010_11110000_00001111_10101010_01010101L;
        int dep = 0b1011_0011_0111;

        long actual = PackedCriteria.withDepMins(criteria, dep);
        long expected = 0b01011001_10111000_10000110_00000010_11110000_00001111_10101010_01010101L;

        assertEquals(expected, actual);

    }

    @Test
    void withAdditionalChangeWorks() {
        long criteria = 0b00000000_00000001_10000110_00000010_11110000_00001111_10101010_01010101L;
        long expected = 0b00000000_00000001_10000110_00000011_11110000_00001111_10101010_01010101L;

        long actual = PackedCriteria.withAdditionalChange(criteria);

        assertEquals(expected, actual, "Le champ de changement (bits 38-32) doit être incrémenté de 1");
    }

    @Test
    void withPayloadWorks(){
        long packedValue = 0b00000000_00000001_10000110_00000010L << 32;
        int payload = 0b01010101_01010101_01010101_01010101;

        long actual = PackedCriteria.withPayload(packedValue,payload);
        long expected =  0b00000000_00000001_10000110_00000010_01010101_01010101_01010101_01010101L;

        assertEquals(expected, actual);
    }

}
