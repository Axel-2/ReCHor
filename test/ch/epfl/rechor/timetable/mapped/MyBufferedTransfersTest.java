package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedTransfersTest {

    /**
     * Crée un ByteBuffer de test avec 3 changements :
     *   - Changement #0 : depStationId = 10, arrStationId = 20, minutes = 5
     *   - Changement #1 : depStationId = 12, arrStationId = 20, minutes = 9
     *   - Changement #2 : depStationId = 15, arrStationId = 16, minutes = 2
     * Format : U16 (2 octets) + U16 (2 octets) + U8 (1 octet) = 5 octets par changement.
     */

    private static ByteBuffer createTestTransfersBuffer() {
        byte[] bytes = new byte[] {
                // Changement #0 : depStationId = 10, arrStationId = 20, minutes = 5
                0x00, 0x0A, 0x00, 0x14, 0x05,
                // Changement #1 : depStationId = 12, arrStationId = 20, minutes = 9
                0x00, 0x0C, 0x00, 0x14, 0x09,
                // Changement #2 : depStationId = 15, arrStationId = 16, minutes = 2
                0x00, 0x0F, 0x00, 0x10, 0x02
        };
        return ByteBuffer.wrap(bytes);
    }

    @Test
    public void depStationIdTest() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        // Vérifie que chaque changement a la bonne gare de départ
        assertEquals(10, transfers.depStationId(0));
        assertEquals(12, transfers.depStationId(1));
        assertEquals(15, transfers.depStationId(2));
    }

    @Test
    public void depStationIdOutOfRangeThrows() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        // Id négatif
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.depStationId(-1);
        });
        // Id trop grand
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.depStationId(3); // On n'a que 3 changements, indices 0..2
        });
    }

    @Test
    public void minutesTest() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        // Vérifie la durée de chaque changement
        assertEquals(5, transfers.minutes(0));
        assertEquals(9, transfers.minutes(1));
        assertEquals(2, transfers.minutes(2));
    }

    @Test
    public void minutesOutOfRangeThrows() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.minutes(-1);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.minutes(3);
        });
    }


    @Test
    public void arrivingAtTest() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        /*
         * Attentes :
         *  - La gare 20 doit avoir deux changements arrivant (indices 0 et 1)
         *  - La gare 16 doit avoir un changement arrivant (indice 2)
         *  - Toute autre gare (ex. 999) ne doit avoir aucun changement
         *
         * La méthode arrivingAt(stationId) renvoie un intervalle empaqueté via PackedRange.
         */
        int interval20 = transfers.arrivingAt(20);
        int start20 = PackedRange.startInclusive(interval20);
        int end20 = PackedRange.endExclusive(interval20);
        // Pour la gare 20, on attend l'intervalle [0, 2) (indices 0 et 1)
        assertEquals(0, start20, "Station 20 : début d'intervalle incorrect");
        assertEquals(2, end20, "Station 20 : fin d'intervalle incorrect");

        int interval16 = transfers.arrivingAt(16);
        int start16 = PackedRange.startInclusive(interval16);
        int end16 = PackedRange.endExclusive(interval16);
        // Pour la gare 16, on attend l'intervalle [2, 3) (seul l'indice 2)
        assertEquals(2, start16, "Station 16 : début d'intervalle incorrect");
        assertEquals(3, end16, "Station 16 : fin d'intervalle incorrect");
    }

    @Test
    public void arrivingAtOutOfRangeThrows() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        // Si stationId négatif ou stationId >= nbGaresMax géré en interne,
        // on peut s'attendre à une IndexOutOfBoundsException (à adapter selon votre code).
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.arrivingAt(-1);
        });
        // Exemple d'un stationId très grand
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.arrivingAt(1000000);
        });
    }

    @Test
    public void minutesBetweenTest() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        /*
         * minutesBetween(depStationId, arrStationId) :
         *  - 10 -> 20 = 5 minutes
         *  - 12 -> 20 = 9 minutes
         *  - 15 -> 16 = 2 minutes
         *  - Tout autre couple non présent => -1
         */
        assertEquals(5, transfers.minutesBetween(10, 20));
        assertEquals(9, transfers.minutesBetween(12, 20));
        assertEquals(2, transfers.minutesBetween(15, 16));
        // Pas de changement direct 10->16
        assertThrows(NoSuchElementException.class, () -> {
            transfers.minutesBetween(10, 16);
        });    }

    @Test
    public void minutesBetweenOutOfRangeThrows() {
        ByteBuffer buffer = createTestTransfersBuffer();
        Transfers transfers = new BufferedTransfers(buffer);

        // StationId invalides
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.minutesBetween(-1, 20);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            transfers.minutesBetween(10, -1);
        });
    }
}