package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedStationsTest {

    private List<String> exempleStringTable = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");

    private ByteBuffer exempleBuffer = ByteBuffer.wrap(new byte[]{0x00, 0x04, 0x04, (byte)0xB6, (byte)0xCA, 0x14, 0x21, 0x14, 0x1F, (byte)0xA1, 0x00, 0x06, 0x04, (byte)0xDC, (byte)0xCC, 0x12, 0x21, 0x18, (byte)0xDA, 0x03});

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


}