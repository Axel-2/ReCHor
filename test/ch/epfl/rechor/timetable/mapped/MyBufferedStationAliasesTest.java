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


}