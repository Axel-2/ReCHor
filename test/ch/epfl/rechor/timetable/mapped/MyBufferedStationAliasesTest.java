package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedStationAliasesTest {

    // Création du buffer d'exemple
    private final HexFormat hexFormat = HexFormat.ofDelimiter(" ");
    private final String stationHexString = "00 05 00 04 00 02 00 03";
    private final byte[] stationBytes = hexFormat.parseHex(stationHexString);
    private final ByteBuffer exempleBuffer = ByteBuffer.wrap(stationBytes);

    private List<String> exempleStringTable = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");


    @Test
    void cs108WebsiteExemples() {
        BufferedStationAliases bufferedStationsAliases = new BufferedStationAliases(exempleStringTable, exempleBuffer);

        // Lausanne
        assertEquals("Losanna", bufferedStationsAliases.alias(0));
        assertEquals("Lausanne", bufferedStationsAliases.stationName(0));

        // Ins
        assertEquals("Anet", bufferedStationsAliases.alias(1));
        assertEquals("Ins", bufferedStationsAliases.stationName(1));


    }

    /**
     * Teste le comportement avec un buffer vide.
     */
    @Test
    void testEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        BufferedStationAliases bufferedAliases =
                new BufferedStationAliases(exempleStringTable, emptyBuffer);

        // Un buffer vide ne contient aucun alias
        assertEquals(0, bufferedAliases.size());
    }

    /**
     * Teste que la construction échoue si le nombre d'octets du buffer
     * n'est pas un multiple de 4 (car chaque alias occupe 4 octets).
     */
    @Test
    void testBufferSizeNotMultipleOfStructure() {
        // On crée un buffer de 6 octets (6 n'est pas un multiple de 4)
        ByteBuffer invalidBuffer = ByteBuffer.allocate(6);
        // La construction doit lever une IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                new BufferedStationAliases(exempleStringTable, invalidBuffer));
    }

    /**
     * Teste que l'accès à un alias inexistant (indice < 0 ou >= size)
     * lève une IndexOutOfBoundsException.
     */
    @Test
    void testAccessWithInvalidAliasIndex() {
        BufferedStationAliases bufferedAliases =
                new BufferedStationAliases(exempleStringTable, exempleBuffer);

        assertEquals(2, bufferedAliases.size());

        // Indice négatif
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedAliases.alias(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedAliases.stationName(-1));

        // Indice hors limite (2 est égal à size, donc invalide)
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedAliases.alias(2));
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedAliases.stationName(2));
    }

    /**
     * Teste le cas où l'ALIAS_ID d'un alias pointe hors de la table de chaînes.
     */
    @Test
    void testAliasIndexOutOfBoundInStringTable() {
        // On construit un alias avec ALIAS_ID = 7 (table : indices 0 à 6)
        // Cela va faire 1 alias (4 octets) :
        //  - ALIAS_ID = 7  => 00 07
        //  - STATION_ID = 4 => 00 04
        byte[] data = new byte[] {
                0x00, 0x07, // 7 (invalide, la table a des indices max = 6)
                0x00, 0x04  // 4 ("Lausanne")
        };
        ByteBuffer invalidAliasBuffer = ByteBuffer.wrap(data);
        BufferedStationAliases bufferedAliases =
                new BufferedStationAliases(exempleStringTable, invalidAliasBuffer);

        // size() = 1
        assertEquals(1, bufferedAliases.size());

        // L'accès à alias(0) doit lever IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedAliases.alias(0));
    }

    /**
     * Teste le cas où le STATION_NAME_ID d'un alias pointe hors de la table de chaînes.
     */
    @Test
    void testStationNameIndexOutOfBoundInStringTable() {
        // On construit un alias avec STATION_NAME_ID = 9 (invalide)
        //  - ALIAS_ID = 2  => "Anet"
        //  - STATION_ID = 9 => inexistant
        byte[] data = new byte[] {
                0x00, 0x02, // ALIAS_ID = 2 -> "Anet"
                0x00, 0x09  // STATION_NAME_ID = 9 (inexistant)
        };
        ByteBuffer invalidStationNameBuffer = ByteBuffer.wrap(data);
        BufferedStationAliases bufferedAliases =
                new BufferedStationAliases(exempleStringTable, invalidStationNameBuffer);

        // size() = 1
        assertEquals(1, bufferedAliases.size());

        // L'accès à stationName(0) doit lever IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> bufferedAliases.stationName(0));
    }

    /**
     * Teste un alias qui référence deux fois le même index dans la table.
     * Cela n'est pas forcément invalide, juste pour vérifier le comportement.
     */
    @Test
    void testAliasWithSameIndex() {
        // ALIAS_ID = 2 ("Anet"), STATION_NAME_ID = 2 ("Anet")
        //  => L'alias et le nom de station sont identiques, ce qui est étrange mais pas forcément interdit.
        byte[] data = new byte[] {
                0x00, 0x02,
                0x00, 0x02
        };
        ByteBuffer sameIndexBuffer = ByteBuffer.wrap(data);
        BufferedStationAliases bufferedAliases =
                new BufferedStationAliases(exempleStringTable, sameIndexBuffer);

        // size() = 1
        assertEquals(1, bufferedAliases.size());

        assertEquals("Anet", bufferedAliases.alias(0));
        assertEquals("Anet", bufferedAliases.stationName(0));
    }


}