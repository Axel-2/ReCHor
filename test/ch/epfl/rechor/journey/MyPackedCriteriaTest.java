package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void packedWorksOnWebsiteExample() {

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
        // changes doit être >= 0 et <= 127.
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
    void dominateOrIsEqualRecognizeDominance(){
        // Trajet pas terrible
        long criteria1 = 0b0_000000000011_00000000001_0000011_00000000000000000000000000000000L;

        // Trajet dominant
        long criteria2 = 0b0_000000000001_000000000111_0000011_00000000000000000000000000000000L;

        boolean actual = PackedCriteria.dominatesOrIsEqual(criteria2, criteria1);
        assertTrue(actual);

    }
    @Test
    void dominateOrIsEqualWorksOnEqualsCriteria(){
        long criteria1 = 0b0_000000000011_00000000001_0000011_00000000000000000000000000000000L;

        // Même données
        long criteria2 = 0b0_000000000011_00000000001_0000011_00000000000000000000000000000000L;

        boolean shouldBeTrue = PackedCriteria.dominatesOrIsEqual(criteria1, criteria2);
        assertTrue(shouldBeTrue);

    }

    @Test
    void dominateOrIsEqualRecognizeNonDominance(){
        // Trajet avec plus de changements, mais une heure de départ plus tard (donc aucun domine)
        long criteria1 = 0b0_000000000101_00000010001_0000011_00000000000000000000000000000000L;

        long criteria2 = 0b0_000000000011_00000010001_0000010_00000000000000000000000000000000L;

        boolean actual = PackedCriteria.dominatesOrIsEqual(criteria1, criteria2);
        assertFalse(actual);

    }
    @Test
    void withoutDepMinsWorks(){
        long criteria = 0b01011001_10111000_10000110_00000010_11110000_00001111_10101010_01010101L;

        long actual = PackedCriteria.withoutDepMins(criteria);
        long expected = 0b00000000_00000000_10000110_00000010_11110000_00001111_10101010_01010101L;

        assertEquals(actual, expected);

    }

//    @Test
//    void withDepMinsWorks(){
//
//        long criteria = 0b00000000_00000000_10000110_00000010_11110000_00001111_10101010_01010101L;
//        int dep = 0b1011_0011_0111;
//
//        long actual = PackedCriteria.withDepMins(criteria, dep);
//        long expected = 0b01011001_10111000_10000110_00000010_11110000_00001111_10101010_01010101L;
//
//        assertEquals(expected, actual);
//
//    }

    @Test
    void withAdditionalChangeWorks() {
        long criteria = 0b00000000_00000001_10000110_00000010_11110000_00001111_10101010_01010101L;
        long expected = 0b00000000_00000001_10000110_00000011_11110000_00001111_10101010_01010101L;

        long actual = PackedCriteria.withAdditionalChange(criteria);

        assertEquals(expected, actual);
    }

    @Test
    void withPayloadWorks(){
        long packedValue = 0b00000000_00000001_10000110_00000010L << 32;
        int payload = 0b01010101_01010101_01010101_01010101;

        long actual = PackedCriteria.withPayload(packedValue,payload);
        long expected =  0b00000000_00000001_10000110_00000010_01010101_01010101_01010101_01010101L;

        assertEquals(expected, actual);
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

    @Test
    void getChangesTest() {

        long criteria = 0b0_110100101111_001100001100_0000010_00000000_00000000_00000000_00000000L;
        long criteria2 = 0b0_110100101111_001100001100_0000010L << 32;

        assertEquals(criteria, criteria2);

        int expectedChanges = 2;

        int actualChanges = PackedCriteria.changes(criteria);

        assertEquals(expectedChanges, actualChanges);
    }

    @Test
    public void testDominatesOrIsEqual() {
        // Create two criteria:
        // Let criteria1: arrMins = 800, departure time = 1000.
        // Let criteria2: arrMins = 850, departure time = 900.
        int arrMins1 = 800, changes1 = 1, payload1 = 0x11111111;
        int arrMins2 = 850, changes2 = 2, payload2 = 0x22222222;
        long crit1 = PackedCriteria.pack(arrMins1, changes1, payload1);
        long crit2 = PackedCriteria.pack(arrMins2, changes2, payload2);
        // Set departure times.
        int dep1 = 650, dep2 = 600;
        crit1 = PackedCriteria.withDepMins(crit1, dep1);
        crit2 = PackedCriteria.withDepMins(crit2, dep2);
        // dominatesOrIsEqual should return true if:
        // depMins(crit1) <= depMins(crit2) because we take the complement and arrMins(crit1) <= arrMins(crit2).
        // Here: 1000 >= 900 and 800 <= 850, so expect true.
        System.out.println("depMins(crit1) = " + PackedCriteria.depMins(crit1));
        System.out.println("depMins(crit2) = " + PackedCriteria.depMins(crit2));
        System.out.println("arrMins(crit1) = " + PackedCriteria.arrMins(crit1));
        System.out.println("arrMins(crit2) = " + PackedCriteria.arrMins(crit2));

        assertTrue(PackedCriteria.dominatesOrIsEqual(crit1, crit2),
                "dominatesOrIsEqual() should return true for criteria1 dominating criteria2");

        // Now, if we modify crit2 to have a higher departure time, then crit1 should not dominate.
        crit2 = PackedCriteria.withDepMins(crit2, 700); // now depMins(crit2) = 1100.
        System.out.println("After modification:");
        System.out.println("depMins(crit1) = " + PackedCriteria.depMins(crit1));
        System.out.println("depMins(crit2) = " + PackedCriteria.depMins(crit2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(crit1, crit2),
                "dominatesOrIsEqual() should return false when criteria1 does not dominate criteria2");
    }

    @Test
    void dominatesOrIsEqual_handlesCriteriaWithoutDepartureTime() {
        // Création de deux critères sans heure de départ
        long criteria1 = PackedCriteria.withoutDepMins(PackedCriteria.pack(600, 2, 0));
        long criteria2 = PackedCriteria.withoutDepMins(PackedCriteria.pack(630, 3, 0));

        // Vérifie que criteria1 domine bien criteria2
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));

        // Vérifie qu'un critère moins bon ne domine pas un critère meilleur
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
    }

    @Test
    void dominatesOrIsEqual_throwsExceptionIfOnlyOneCriteriaHasDepartureTime() {
        // Un critère avec heure de départ
        long criteriaWithDepTime = PackedCriteria.withDepMins(PackedCriteria.pack(600, 2, 0), 300);

        // Un critère sans heure de départ
        long criteriaWithoutDepTime = PackedCriteria.withoutDepMins(PackedCriteria.pack(600, 2, 0));

        // Vérifie que l'exception est bien levée
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.dominatesOrIsEqual(criteriaWithDepTime, criteriaWithoutDepTime);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.dominatesOrIsEqual(criteriaWithoutDepTime, criteriaWithDepTime);
        });
    }
    }
