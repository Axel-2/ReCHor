package ch.epfl.rechor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyBits32_24_8Test {

    // Teste le cas nominal : bits24 et bits8 valides.
    @Test
    public void testPackValid() {
        int bits24 = 0x123456; // doit tenir sur 24 bits
        int bits8  = 0xAB;     // doit tenir sur 8 bits
        int expected = (bits24 << 8) | bits8;
        int actual = Bits32_24_8.pack(bits24, bits8);
        assertEquals(expected, actual, "Le packing ne correspond pas à l'attendu.");
    }

    // Vérifie qu'une valeur bits24 trop grande déclenche une IllegalArgumentException.
    @Test
    public void testPackInvalidBits24() {
        int bits24 = 0x1000000; // 0xFFFFFF est le maximum sur 24 bits; 0x1000000 dépasse ce seuil
        int bits8  = 0x12;
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32_24_8.pack(bits24, bits8);
        }, "Une valeur bits24 trop grande doit lever une exception.");
    }

    // Vérifie qu'une valeur bits8 trop grande déclenche une IllegalArgumentException.
    @Test
    public void testPackInvalidBits8() {
        int bits24 = 0xABCDEF; // valeur valide pour 24 bits
        int bits8  = 0x100;    // 0xFF est le maximum sur 8 bits; 0x100 dépasse ce seuil
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32_24_8.pack(bits24, bits8);
        }, "Une valeur bits8 trop grande doit lever une exception.");
    }

    // Teste l'extraction des 24 bits de poids fort.
    @Test
    public void testUnpack24() {
        int bits24 = 0xFEDCBA;
        int bits8  = 0x34;
        int packed = Bits32_24_8.pack(bits24, bits8);
        int unpacked = Bits32_24_8.unpack24(packed);
        assertEquals(bits24, unpacked, "Les 24 bits extraits ne correspondent pas à l'original.");
    }

    // Teste l'extraction des 8 bits de poids faible.
    @Test
    public void testUnpack8() {
        int bits24 = 0x13579B;
        int bits8  = 0xCD;
        int packed = Bits32_24_8.pack(bits24, bits8);
        int unpacked = Bits32_24_8.unpack8(packed);
        assertEquals(bits8, unpacked, "Les 8 bits extraits ne correspondent pas à l'original.");
    }

    // Teste plusieurs combinaisons pour vérifier le round-trip (pack suivi d'un unpack).
    @Test
    public void testRoundTrip() {
        int[] testBits24 = {0, 1, 0x123456, 0xFFFFFF};
        int[] testBits8  = {0, 1, 0x12, 0xFF};
        for (int bits24 : testBits24) {
            for (int bits8 : testBits8) {
                int packed = Bits32_24_8.pack(bits24, bits8);
                assertEquals(bits24, Bits32_24_8.unpack24(packed),
                        "Round-trip failed for bits24: " + Integer.toHexString(bits24) +
                                ", bits8: " + Integer.toHexString(bits8));
                assertEquals(bits8, Bits32_24_8.unpack8(packed),
                        "Round-trip failed for bits24: " + Integer.toHexString(bits24) +
                                ", bits8: " + Integer.toHexString(bits8));
            }
        }
    }

}
