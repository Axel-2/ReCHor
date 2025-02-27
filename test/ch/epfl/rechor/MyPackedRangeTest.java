package ch.epfl.rechor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyPackedRangeTest {

    @Test
    void websiteExample() {

        // Exemple du site directement

        int expectedPacked = 0b00000000000001001101001000101100;

        int actualPacked = PackedRange.pack(1234, 1278);
        System.out.printf("s");

        assertEquals(expectedPacked, actualPacked);

    }

    @Test
    void packValidRange() {
        int start = 5;
        int end   = 10;
        int packed = PackedRange.pack(start, end);

        // Vérifier que les méthodes de dépackaging renvoient bien les valeurs attendues
        assertEquals(start, PackedRange.startInclusive(packed),
                "startInclusive ne correspond pas à la valeur de départ.");
        assertEquals(end, PackedRange.endExclusive(packed),
                "endExclusive ne correspond pas à la valeur de fin.");
        assertEquals(end - start, PackedRange.length(packed),
                "length ne correspond pas à end - start.");
    }

    @Test
    void packEmptyRange() {
        // Cas d'un intervalle vide : startInclusive == endExclusive
        int start = 10;
        int end   = 10;
        int packed = PackedRange.pack(start, end);

        assertEquals(start, PackedRange.startInclusive(packed));
        assertEquals(end, PackedRange.endExclusive(packed));
        assertEquals(0, PackedRange.length(packed),
                "Un intervalle [10,10) devrait avoir une longueur de 0.");
    }

    @Test
    void packInvalidRangeReverseOrder() {
        // Cas où endExclusive < startInclusive
        assertThrows(IllegalArgumentException.class, () ->
                        PackedRange.pack(10, 5),
                "Un intervalle où end < start devrait lever une IllegalArgumentException.");
    }

    @Test
    void packInvalidRangeTooLarge() {
        // Exemple si la représentation interne ne peut pas dépasser un certain écart
        // (par ex. si l'on stocke sur 16 bits, la longueur max est 65535)
        // Ici on prend 70000 pour forcer l'erreur.
        assertThrows(IllegalArgumentException.class, () ->
                        PackedRange.pack(0, 70000),
                "Un intervalle trop large devrait lever une IllegalArgumentException.");
    }

    @Test
    void lengthWorksIndependently() {
        int start = 100;
        int end   = 200;
        int packed = PackedRange.pack(start, end);

        // Vérifie simplement la longueur
        assertEquals(100, PackedRange.length(packed),
                "La longueur devrait être end - start = 100.");
    }

    @Test
    void startInclusiveWorksIndependently() {
        int start = 42;
        int end   = 100;
        int packed = PackedRange.pack(start, end);

        // Vérifie simplement la valeur de startInclusive
        assertEquals(start, PackedRange.startInclusive(packed),
                "startInclusive ne renvoie pas la bonne valeur.");
    }

    @Test
    void endExclusiveWorksIndependently() {
        int start = 0;
        int end   = 234;
        int packed = PackedRange.pack(start, end);

        // Vérifie simplement la valeur de endExclusive
        assertEquals(end, PackedRange.endExclusive(packed),
                "endExclusive ne renvoie pas la bonne valeur.");
    }

    @Test
    void roundTripOnMultipleRanges() {
        // Vérifie un ensemble de valeurs pour s'assurer que le "pack → unpack" fonctionne
        int[][] testRanges = {
                {0, 0},       // intervalle vide
                {0, 1},
                {1, 2},
                {10, 10},     // vide aussi
                {100, 105},
                {9999, 10000} // test plus large
        };

        for (int[] range : testRanges) {
            int start = range[0];
            int end   = range[1];

            int packed = PackedRange.pack(start, end);

            assertEquals(start, PackedRange.startInclusive(packed),
                    "startInclusive incorrect pour le range [" + start + "," + end + ")");
            assertEquals(end, PackedRange.endExclusive(packed),
                    "endExclusive incorrect pour le range [" + start + "," + end + ")");
            assertEquals(end - start, PackedRange.length(packed),
                    "length incorrect pour le range [" + start + "," + end + ")");
        }
    }
}
