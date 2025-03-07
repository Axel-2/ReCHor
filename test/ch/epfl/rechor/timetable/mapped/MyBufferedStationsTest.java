package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedStationsTest {

    // Création du buffer d'exemple
    private final HexFormat hexFormat = HexFormat.ofDelimiter(" ");
    private final String stationHexString = "00 04 04 B6 CA 14 21 14 1F A1 00 06 04 DC CC 12 21 18 DA 03";
    private final byte[] stationBytes = hexFormat.parseHex(stationHexString);
    private final ByteBuffer exempleBuffer = ByteBuffer.wrap(stationBytes);

    private List<String> exempleStringTable = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");


    @Test
    void cs108WebsiteExemples() {
        BufferedStations bufferedStations = new BufferedStations(exempleStringTable, exempleBuffer);

        // Lausanne
        assertEquals("Lausanne", bufferedStations.name(0));
        assertEquals(6.629091985523701, bufferedStations.longitude(0));
        assertEquals(46.5167919639498, bufferedStations.latitude(0));

        // Palézieux
        assertEquals("Palézieux", bufferedStations.name(1));
        assertEquals(6.837874967604876, bufferedStations.longitude(1));
        assertEquals(46.54276396147907, bufferedStations.latitude(1));

        // Nombre d'éléments
        assertEquals(2, bufferedStations.size());
    }

    @Test
    void testEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        BufferedStations bufferedStations = new BufferedStations(exempleStringTable, emptyBuffer);
        assertEquals(0, bufferedStations.size(), "Un buffer vide doit contenir 0 station");
    }


    /**
     * Teste que la construction échoue si le nombre d'octets du buffer
     * n'est pas un multiple de la taille d'une station (10 octets).
     */
    @Test
    void testBufferSizeNotMultipleOfStructure() {
        // On crée un buffer de 11 octets (11 n'est pas un multiple de 10)
        ByteBuffer invalidBuffer = ByteBuffer.allocate(11);
        // La construction doit lever une IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                        new BufferedStations(exempleStringTable, invalidBuffer),
                "Un buffer dont la taille n'est pas un multiple de 10 doit lever une IllegalArgumentException");
    }

    /**
     * Teste que l'accès aux stations avec un indice invalide (négatif ou trop grand)
     * lève bien une IndexOutOfBoundsException.
     */
    @Test
    void testAccessWithInvalidStationIndex() {
        BufferedStations bufferedStations = new BufferedStations(exempleStringTable, exempleBuffer);

        // Accès avec indice négatif
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.longitude(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.latitude(-1));

        // Accès avec indice égal à la taille (inexistant)
        int size = bufferedStations.size();
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.name(size));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.longitude(size));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.latitude(size));
    }

    /**
     * Teste le comportement lorsque l'index de chaîne référencé par une station est invalide.
     * Ici, on crée un buffer pour une station dont le NAME_ID est 7 alors que la table contient 7 éléments (indices 0 à 6).
     */
    @Test
    void testStationNameIndexOutOfBound() {
        byte[] data = new byte[]{
                0x00, 0x07,             // NAME_ID = 7 (invalide)
                0x00, 0x00, 0x00, 0x00,   // LON = 0
                0x00, 0x00, 0x00, 0x00    // LAT = 0
        };
        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations bufferedStations = new BufferedStations(exempleStringTable, buffer);
        // Lors de l'accès au nom, on s'attend à une exception puisque l'index 7 n'existe pas dans la table
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedStations.name(0));
    }

    /**
     * Teste une station avec des coordonnées nulles.
     * On construit une station dont les octets de longitude et latitude valent 0,
     * ce qui devrait donner 0.0 en degrés.
     */
    @Test
    void testStationWithZeroCoordinates() {
        byte[] data = new byte[]{
                0x00, 0x04,             // NAME_ID = 4 -> "Lausanne" dans notre table d'exemple
                0x00, 0x00, 0x00, 0x00,   // LON = 0
                0x00, 0x00, 0x00, 0x00    // LAT = 0
        };
        ByteBuffer buffer = ByteBuffer.wrap(data);
        BufferedStations bufferedStations = new BufferedStations(exempleStringTable, buffer);
        assertEquals("Lausanne", bufferedStations.name(0));
        assertEquals(0.0, bufferedStations.longitude(0));
        assertEquals(0.0, bufferedStations.latitude(0));
    }




    }