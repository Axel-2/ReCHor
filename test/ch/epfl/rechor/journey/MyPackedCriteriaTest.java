package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void exempleDuSite() {

        // pour un payload vide
        // on ajoute 32 bits à
        long expected = (long) 0b00000000_00000001_10000110_00000010L << 32;

        // Heure de départ
        // 8H00 depuis miniuit
        int depMins = 8 * 60;

        // 9h depuis minuit
        int arrMins = 9 * 60;

        int changements = 2;

        long actual = PackedCriteria.pack(arrMins, 2, 0);

        assertEquals(expected, actual);
    }

    @Test
    void packValidData() {
        // Test d'un empaquetage avec des valeurs arbitraires valides.
        // Par exemple, 8h30 d'arrivée : 8*60+30 = 510 minutes.
        // On rappelle que la méthode translate l'heure d'arrivée en ajoutant 240,
        // soit 510 + 240 = 750.

        int arrMins = 510;
        int changes = 3;
        int payload = 123456;
        long expected = (((long)(arrMins + 240)) << 39)  // arrMins sur 25 bits (bits 39..63)
                | (((long) changes) << 32)       // changes sur 7 bits (bits 32..38)
                | (((long) payload) & 0xFFFFFFFFL); // payload sur 32 bits (bits 0..31)
        long actual = PackedCriteria.pack(arrMins, changes, payload);
        assertEquals(expected, actual);
    }

    @Test
    void packNegativePayload() {
        // Vérifie qu'un payload négatif est correctement traité (sans extension de signe)
        int arrMins = 510;
        int changes = 3;
        int payload = -1; // en binaire, cela correspond à 0xFFFFFFFF sur 32 bits
        long expected = (((long)(arrMins + 240)) << 39)
                | (((long) changes) << 32)
                | (0xFFFFFFFFL); // Les 32 bits de payload sont tous à 1
        long actual = PackedCriteria.pack(arrMins, changes, payload);
        assertEquals(expected, actual);
    }

    @Test
    void packInvalidArrivalTime() {
        // Teste qu'une heure d'arrivée invalide (inférieure à -240) lève une exception.
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(-300, 3, 0);
        });
        // Teste qu'une heure d'arrivée invalide (>= 2880) lève une exception.
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(2880, 3, 0);
        });
    }

    @Test
    void packInvalidChanges() {
        // changes doit être > 0 et <= 127.
        // Test avec changes == 0
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(540, 0, 0);
        });
        // Test avec changes > 127
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(540, 128, 0);
        });
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
    void depMinsExampleSite() {

        // heure de départ 8h après minuit
        // en minutes
        int expectedDepartValue = 8 * 60;

        long packedValue = 0b0_110100101111_001100001100_0000010L << 32;

        int actualDepMins = PackedCriteria.depMins(packedValue);

        assertEquals(expectedDepartValue, actualDepMins);

    }

    @Test
    void depMinsThrowsError() {

        // doit retourner une erreur si y a pas d'heure de départ

        long packedValue = 0b00000000_00000001_10000110_00000010L << 32;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.depMins(packedValue);
        });

    }

    @Test
    void arrMinsExampleWebsite() {

        // packedValue donné en exemple sur le site
        long criteria = 0b0_110100101111_001100001100_0000010L << 32;

        // L'heure d'arrivée de l'exemple du site
        // est 9h
        int expectedArrMins = 9*60;

        int actualArrMins = PackedCriteria.arrMins(criteria);

        assertEquals(expectedArrMins, actualArrMins);

    }


}
