package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedTransfersTest2 {

    /**
     * Crée un ByteBuffer simulant une table de changements.
     * Chaque enregistrement occupe 5 octets suivant la structure :
     *   - DEP_STATION_ID (U16)
     *   - ARR_STATION_ID (U16)
     *   - TRANSFER_MINUTES (U8)
     * Les enregistrements produits sont :
     *   • Record 0 : dep = 1, arr = 10, minutes = 5
     *   • Record 1 : dep = 2, arr = 10, minutes = 3
     *   • Record 2 : dep = 3, arr = 20, minutes = 4
     *   • Record 3 : dep = 4, arr = 20, minutes = 2
     *   • Record 4 : dep = 5, arr = 20, minutes = 6
     *   • Record 5 : dep = 6, arr = 30, minutes = 7
     */
    private BufferedTransfers createBufferedTransfers() {
        byte[] data = new byte[] {
                0x00, 0x01, 0x00, 0x0A, 0x05,  // Record 0: dep=1,  arr=10, minutes=5
                0x00, 0x02, 0x00, 0x0A, 0x03,  // Record 1: dep=2,  arr=10, minutes=3
                0x00, 0x03, 0x00, 0x14, 0x04,  // Record 2: dep=3,  arr=20, minutes=4
                0x00, 0x04, 0x00, 0x14, 0x02,  // Record 3: dep=4,  arr=20, minutes=2
                0x00, 0x05, 0x00, 0x14, 0x06,  // Record 4: dep=5,  arr=20, minutes=6
                0x00, 0x06, 0x00, 0x1E, 0x07   // Record 5: dep=6,  arr=30, minutes=7
        };
        return new BufferedTransfers(ByteBuffer.wrap(data));
    }

    @Test
    public void depStationIdTest() {
        Transfers transfers = createBufferedTransfers();
        // Vérifie que chaque enregistrement renvoie le bon depStationId
        assertEquals(1, transfers.depStationId(0));
        assertEquals(2, transfers.depStationId(1));
        assertEquals(3, transfers.depStationId(2));
        assertEquals(4, transfers.depStationId(3));
        assertEquals(5, transfers.depStationId(4));
        assertEquals(6, transfers.depStationId(5));
    }

    @Test
    public void depStationIdOutOfRangeThrows() {
        Transfers transfers = createBufferedTransfers();
        // Un id négatif ou trop grand doit lever une exception
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(6));
    }

    @Test
    public void minutesTest() {
        Transfers transfers = createBufferedTransfers();
        // Vérifie que chaque enregistrement renvoie la bonne durée en minutes
        assertEquals(5, transfers.minutes(0));
        assertEquals(3, transfers.minutes(1));
        assertEquals(4, transfers.minutes(2));
        assertEquals(2, transfers.minutes(3));
        assertEquals(6, transfers.minutes(4));
        assertEquals(7, transfers.minutes(5));
    }

    @Test
    public void minutesOutOfRangeThrows() {
        Transfers transfers = createBufferedTransfers();
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(6));
    }


    @Test
    public void arrivingAtInvalidStationsThrows() {
        Transfers transfers = createBufferedTransfers();
        // StationId négatif
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(-1));
        // StationId supérieur au maximum présent (max = 30 ici)
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(31));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(100));
    }

    @Test
    public void minutesBetweenTest() {
        Transfers transfers = createBufferedTransfers();
        // Vérifie que minutesBetween renvoie la bonne durée pour chaque changement existant
        assertEquals(5, transfers.minutesBetween(1, 10));
        assertEquals(3, transfers.minutesBetween(2, 10));
        assertEquals(4, transfers.minutesBetween(3, 20));
        assertEquals(2, transfers.minutesBetween(4, 20));
        assertEquals(6, transfers.minutesBetween(5, 20));
        assertEquals(7, transfers.minutesBetween(6, 30));
        // Pour un couple non existant, la méthode doit lever NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(1, 20));
    }

    @Test
    public void minutesBetweenOutOfRangeThrows() {
        Transfers transfers = createBufferedTransfers();
        // StationId invalides doivent lever IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(-1, 10));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(1, -1));
    }

    @Test
    public void sizeTest() {
        Transfers transfers = createBufferedTransfers();
        // Le buffer de test contient 6 enregistrements
        assertEquals(6, transfers.size());
    }
}
