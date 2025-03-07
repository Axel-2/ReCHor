package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedPlatformsTest {

    // Table des chaînes correspondant aux index de chaînes dans le buffer
    private static final List<String> STRING_TABLE = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");

    // Buffer contenant les données aplaties des quais/voies
    // Format attendu : [NAME_ID (U16), STATION_ID (U16)] pour chaque plateforme
    private static final byte[] PLATFORM_DATA = {
            0x00, 0x00, 0x00, 0x00,  // ID=0, NAME="1", STATION_ID=0 (Lausanne)
            0x00, 0x01, 0x00, 0x00,  // ID=1, NAME="70", STATION_ID=0 (Lausanne)
            0x00, 0x00, 0x00, 0x01   // ID=2, NAME="1", STATION_ID=1 (Palézieux)
    };

    // ByteBuffer à partir des données test
    private static final ByteBuffer BUFFER = ByteBuffer.wrap(PLATFORM_DATA);

    // Création de l'instance de BufferedPlatforms pour les tests
    private final BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(STRING_TABLE, BUFFER);

    /**
     * Vérifie que la méthode size() retourne le bon nombre d'éléments
     */
    @Test
    void sizeReturnsCorrectValue() {
        assertEquals(3, bufferedPlatforms.size(), "La taille devrait être 3 (nombre de plateformes dans le buffer)");
    }

    /**
     * Vérifie que la méthode name(int id) retourne le bon nom pour chaque plateforme
     */
    @Test
    void nameReturnsCorrectValues() {
        assertEquals("1", bufferedPlatforms.name(0), "L'ID 0 devrait correspondre au nom '1'");
        assertEquals("70", bufferedPlatforms.name(1), "L'ID 1 devrait correspondre au nom '70'");
        assertEquals("1", bufferedPlatforms.name(2), "L'ID 2 devrait correspondre au nom '1'"); // Même nom que ID 0
    }

    /**
     * Vérifie que la méthode stationId(int id) retourne le bon index de gare parent
     */
    @Test
    void stationIdReturnsCorrectValues() {
        assertEquals(0, bufferedPlatforms.stationId(0), "L'ID 0 devrait correspondre à la gare d'index 0 (Lausanne)");
        assertEquals(0, bufferedPlatforms.stationId(1), "L'ID 1 devrait correspondre à la gare d'index 0 (Lausanne)");
        assertEquals(1, bufferedPlatforms.stationId(2), "L'ID 2 devrait correspondre à la gare d'index 1 (Palézieux)");
    }

    /**
     * Vérifie que l'accès à une plateforme avec un indice invalide lève IndexOutOfBoundsException.
     */
    @Test
    void testAccessWithInvalidPlatformIndex() {
        // Indice négatif
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedPlatforms.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedPlatforms.stationId(-1));
        // Indice égal à size() (3) est invalide (indices valides : 0,1,2)
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedPlatforms.name(3));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedPlatforms.stationId(3));
    }

    /**
     * Teste le comportement avec un buffer vide.
     */
    @Test
    void testEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        BufferedPlatforms bp = new BufferedPlatforms(STRING_TABLE, emptyBuffer);
        assertEquals(0, bp.size(), "Un buffer vide doit contenir 0 plateforme");
    }

    /**
     * Teste que la construction échoue si la taille du buffer n'est pas un multiple de 4 octets.
     */
    @Test
    void testBufferSizeNotMultipleOfStructure() {
        // 5 octets n'est pas un multiple de 4
        ByteBuffer invalidBuffer = ByteBuffer.allocate(5);
        assertThrows(IllegalArgumentException.class, () -> new BufferedPlatforms(STRING_TABLE, invalidBuffer));
    }

    /**
     * Teste que le nom de plateforme lève une exception lorsque le NAME_ID est invalide (hors bornes de la STRING_TABLE).
     */
    @Test
    void testInvalidNameIndexInStringTable() {
        // On crée une plateforme avec NAME_ID = 7 (valeur 0x00, 0x07), alors que STRING_TABLE contient des indices 0 à 6.
        byte[] data = new byte[] {
                0x00, 0x07,  // NAME_ID = 7, invalide
                0x00, 0x00   // STATION_ID = 0
        };
        ByteBuffer invalidNameBuffer = ByteBuffer.wrap(data);
        BufferedPlatforms bp = new BufferedPlatforms(STRING_TABLE, invalidNameBuffer);
        // L'accès au nom doit lever une exception (IndexOutOfBounds ou similaire)
        assertThrows(IndexOutOfBoundsException.class, () -> bp.name(0));
    }

    /**
     * Teste une plateforme qui référence deux fois le même index pour le nom et la gare parente.
     * Cela n'est pas forcément invalide, mais vérifie que le comportement reste cohérent.
     */
    @Test
    void testPlatformWithSameNameAndStation() {
        // Par exemple, NAME_ID = 0 ("1") et STATION_ID = 0.
        byte[] data = new byte[] {
                0x00, 0x00,  // NAME_ID = 0 ("1")
                0x00, 0x00   // STATION_ID = 0
        };
        ByteBuffer sameIndexBuffer = ByteBuffer.wrap(data);
        BufferedPlatforms bp = new BufferedPlatforms(STRING_TABLE, sameIndexBuffer);
        assertEquals(1, bp.size(), "Le buffer devrait contenir 1 plateforme");
        assertEquals("1", bp.name(0));
        assertEquals(0, bp.stationId(0));
    }
}