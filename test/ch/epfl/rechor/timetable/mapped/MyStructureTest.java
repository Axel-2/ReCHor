package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyStructureTest {

    @Test
    void testTotalSizeWithSingleFields() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8)
        );
        assertEquals(1, structure.totalSize(), "Total size should be 1 for U8 field.");
    }

    @Test
    void testTotalSizeWithMultipleFields() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(7, structure.totalSize(), "Total size should be 7 (1 + 2 + 4).");
    }

    @Test
    void testOffsetsForFirstElement() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(0, structure.offset(0, 0), "Offset of field 0 in element 0 should be 0.");
        assertEquals(1, structure.offset(1, 0), "Offset of field 1 in element 0 should be 1.");
        assertEquals(3, structure.offset(2, 0), "Offset of field 2 in element 0 should be 3.");
    }

    @Test
    void testOffsetsForSecondElement() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(7, structure.offset(0, 1), "Offset of field 0 in element 1 should be 7.");
        assertEquals(8, structure.offset(1, 1), "Offset of field 1 in element 1 should be 8.");
        assertEquals(10, structure.offset(2, 1), "Offset of field 2 in element 1 should be 10.");
    }

    @Test
    void testOffsetsForThirdElement() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(14, structure.offset(0, 2), "Offset of field 0 in element 2 should be 14.");
        assertEquals(15, structure.offset(1, 2), "Offset of field 1 in element 2 should be 15.");
        assertEquals(17, structure.offset(2, 2), "Offset of field 2 in element 2 should be 17.");
    }

    @Test
    void testInvalidFieldIndexThrowsException() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );

        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(2, 0),
                "Accessing an out-of-bounds field should throw an exception.");
    }

    @Test
    void testNegativeFieldIndexThrowsException() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );

        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(-1, 0),
                "Accessing a negative field index should throw an exception.");
    }


    @Test
    void testInvalidFieldOrderThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                Structure.field(1, Structure.FieldType.U8),
                Structure.field(0, Structure.FieldType.U16)
        ), "Fields should be provided in ascending order.");
    }


    @Test
    void testNullFieldTypeThrowsException() {
        assertThrows(NullPointerException.class, () -> Structure.field(0, null),
                "Un type de champ null devrait lever une NullPointerException.");
    }

    @Test
    void testDuplicateFieldIndexThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(0, Structure.FieldType.U16)
        ), "Des champs avec le même index devraient lever une IllegalArgumentException.");
    }

    @Test
    void testSkippedFieldIndexThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(2, Structure.FieldType.U16)  // Index 1 manquant
        ), "Sauter un index de champ devrait lever une IllegalArgumentException.");
    }

    @Test
    void testOffsetForLargeElementIndex() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        int elementIndex = 1000;
        assertEquals(3 * elementIndex, structure.offset(0, elementIndex),
                "L'offset pour un grand index d'élément devrait être calculé correctement.");
    }

    @Test
    void testTotalSizeForAllFieldTypes() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32),
                Structure.field(3, Structure.FieldType.U8),
                Structure.field(4, Structure.FieldType.S32)
        );
        assertEquals(12, structure.totalSize(), "La taille totale devrait être 12 (1 + 2 + 4 + 1 + 4).");
    }

//    @Test
//    void testFieldTypeSize() {
//        assertEquals(1, Structure.FieldType.U8.size(), "U8 devrait avoir une taille de 1.");
//        assertEquals(2, Structure.FieldType.U16.size(), "U16 devrait avoir une taille de 2.");
//        assertEquals(4, Structure.FieldType.S32.size(), "S32 devrait avoir une taille de 4.");
//    }

    @Test
    void testStructureWithSingleLargeField() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.S32)
        );
        assertEquals(4, structure.totalSize(), "La taille totale devrait être 4 pour un champ S32.");
        assertEquals(0, structure.offset(0, 0), "L'offset du premier élément devrait être 0.");
        assertEquals(4, structure.offset(0, 1), "L'offset du deuxième élément devrait être 4.");
    }

    @Test
    void testOffsetCalculationForComplexStructure() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),   // 1 byte
                Structure.field(1, Structure.FieldType.U16),  // 2 bytes
                Structure.field(2, Structure.FieldType.S32),  // 4 bytes
                Structure.field(3, Structure.FieldType.U8),   // 1 byte
                Structure.field(4, Structure.FieldType.U16)   // 2 bytes
        );
        // Taille totale: 10 bytes

        // Premier élément
        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
        assertEquals(3, structure.offset(2, 0));
        assertEquals(7, structure.offset(3, 0));
        assertEquals(8, structure.offset(4, 0));

        // Deuxième élément
        assertEquals(10, structure.offset(0, 1));
        assertEquals(11, structure.offset(1, 1));
        assertEquals(13, structure.offset(2, 1));
        assertEquals(17, structure.offset(3, 1));
        assertEquals(18, structure.offset(4, 1));

        // Troisième élément
        assertEquals(20, structure.offset(0, 2));
        assertEquals(21, structure.offset(1, 2));
        assertEquals(23, structure.offset(2, 2));
        assertEquals(27, structure.offset(3, 2));
        assertEquals(28, structure.offset(4, 2));
    }

    @Test
    void testNegativeElementIndexThrowsNoException() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );

        // Vérifie que l'offset peut être calculé avec un index d'élément négatif
        // (le comportement n'est pas spécifié, mais ça devrait fonctionner mathématiquement)
        int negativeOffset = structure.offset(0, -1);
        assertEquals(-3, negativeOffset, "L'offset pour un index d'élément négatif devrait être négatif.");
    }
}