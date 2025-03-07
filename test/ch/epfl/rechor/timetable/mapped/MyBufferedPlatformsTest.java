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
}