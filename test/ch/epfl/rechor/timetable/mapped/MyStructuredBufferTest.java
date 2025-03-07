package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class MyStructuredBufferTest {

    /**
     * Teste le calcul des offsets dans une Structure.
     * La structure testée correspond aux gares, composée de :
     *  - Un champ U16 (2 octets) pour l'index de nom
     *  - Un champ S32 (4 octets) pour la longitude
     *  - Un champ S32 (4 octets) pour la latitude
     * La taille totale attendue est de 10 octets.
     */
    @Test
    public void testStructureOffsets() {
        // Création de la structure
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.S32)
        );

        // Vérification de la taille totale de la structure
        assertEquals(10, structure.totalSize(), "La taille totale doit être 10 octets");

        // On suppose que la Structure calcule en interne les offsets suivants :
        //   offset du champ 0 = 0
        //   offset du champ 1 = 2
        //   offset du champ 2 = 6
        // Pour un élément d'index 1, l'offset global du champ 1 doit être : 1 * 10 + 2 = 12
        assertEquals(10, structure.offset(0, 1), "Offset du champ 0 de l'élément 1 doit être 10");
        assertEquals(12, structure.offset(1, 1), "Offset du champ 1 de l'élément 1 doit être 12");
        assertEquals(16, structure.offset(2, 1), "Offset du champ 2 de l'élément 1 doit être 16");
    }

    /**
     * Teste les méthodes d'accès (getU16, getS32) de StructuredBuffer en
     * utilisant les données d'exemple des gares.
     * La table des gares est codée sur 20 octets (2 éléments de 10 octets chacun) :
     *   Élément 0 : 00 04 | 04 B6 CA 14 | 21 14 1F A1
     *   Élément 1 : 00 06 | 04 DC CC 12 | 21 18 DA 03
     *
     * Les valeurs attendues sont :
     *   - Élément 0, champ 0 (U16) : 0x0004 = 4
     *   - Élément 0, champ 1 (S32) : 0x04B6CA14 = 79 088 148
     *   - Élément 0, champ 2 (S32) : 0x21141FA1 = 554 966 945
     *   - Élément 1, champ 0 (U16) : 0x0006 = 6
     *   - Élément 1, champ 1 (S32) : 0x04DCCCC12 (calculé) = 81 579 026
     *   - Élément 1, champ 2 (S32) : 0x2118DA03 (calculé) = 555 276 803
     */
    @Test
    public void testStructuredBufferGetMethods() {
        byte[] data = new byte[] {
                0x00, 0x04, 0x04, (byte)0xB6, (byte)0xCA, 0x14, 0x21, 0x14, 0x1F, (byte)0xA1,
                0x00, 0x06, 0x04, (byte)0xDC, (byte)0xCC, 0x12, 0x21, 0x18, (byte)0xDA, 0x03
        };
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Définition de la structure pour une gare : [NAME_ID:U16, LON:S32, LAT:S32]
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16), // Nom (index dans la table des chaînes)
                Structure.field(1, Structure.FieldType.S32), // Longitude
                Structure.field(2, Structure.FieldType.S32)  // Latitude
        );

        StructuredBuffer sb = new StructuredBuffer(structure, buffer);

        // Vérifie que le buffer contient 2 éléments (20 octets / 10 octets par élément)
        assertEquals(2, sb.size(), "Le nombre d'éléments doit être 2");

        // Vérifications pour l'élément 0
        assertEquals(4, sb.getU16(0, 0), "Élément 0 - Champ U16 (nom) doit être 4");
        assertEquals(79088148, sb.getS32(1, 0), "Élément 0 - Longitude incorrecte");
        assertEquals(554966945, sb.getS32(2, 0), "Élément 0 - Latitude incorrecte");

        // Vérifications pour l'élément 1
        assertEquals(6, sb.getU16(0, 1), "Élément 1 - Champ U16 (nom) doit être 6");
        assertEquals(81579026, sb.getS32(1, 1), "Élément 1 - Longitude incorrecte");
        assertEquals(555276803, sb.getS32(2, 1), "Élément 1 - Latitude incorrecte");
    }

    /**
     * Teste que les accès avec des indices invalides (champ ou élément) lèvent
     * bien une IndexOutOfBoundsException.
     */

    @Test
    void testInvalidIndices() {
        // Création d'un buffer de 12 octets (permettant de stocker 2 éléments de la structure)
        byte[] data = new byte[12];
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Définition d'une structure avec deux champs
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16), // Champ 1 (2 octets)
                Structure.field(1, Structure.FieldType.S32)  // Champ 2 (4 octets)
        );

        // Création du StructuredBuffer
        StructuredBuffer sb = new StructuredBuffer(structure, buffer);

        // Vérification du nombre d'éléments
        assertEquals(2, sb.size(), "Le buffer doit contenir 2 éléments");

        // Accès à un élément inexistant (indice 2 alors que seuls 0 et 1 existent)
        assertThrows(IndexOutOfBoundsException.class, () -> sb.getU16(0, 2),
                "Accéder à un élément inexistant doit lever une exception");

        // Accès à un champ inexistant (indice 2 alors que seuls 0 et 1 existent)
        assertThrows(IndexOutOfBoundsException.class, () -> sb.getS32(2, 0),
                "Accéder à un champ inexistant doit lever une exception");
    }

}