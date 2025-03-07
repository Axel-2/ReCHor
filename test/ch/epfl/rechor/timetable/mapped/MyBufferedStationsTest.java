package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedStationsTest {

    private List<String> exempleStringTable = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");

    private ByteBuffer exempleBuffer = ByteBuffer.wrap(new byte[]{0x00, 0x04, 0x04, (byte)0xB6, (byte)0xCA, 0x14, 0x21, 0x14, 0x1F, (byte)0xA1, 0x00, 0x06, 0x04, (byte)0xDC, (byte)0xCC, 0x12, 0x21, 0x18, (byte)0xDA, 0x03});


    @Test
    void firstTest() {

        BufferedStations bufferedStations = new BufferedStations(exempleStringTable, exempleBuffer);

        assertEquals("Lausanne", bufferedStations.name(0));
    }


}