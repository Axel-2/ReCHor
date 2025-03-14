package ch.epfl.rechor.timetable.mapped;

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
        ByteBuffer buffer = ByteBuffer.allocate(3 * 5);

        // Changement #0
        buffer.putShort((short)10);  // depStationId
        buffer.putShort((short)20);  // arrStationId
        buffer.put((byte)5);         // transferMinutes

        // Changement #1
        buffer.putShort((short)12);
        buffer.putShort((short)20);
        buffer.put((byte)9);

        // Changement #2
        buffer.putShort((short)15);
        buffer.putShort((short)16);
        buffer.put((byte)2);

        buffer.flip(); // Repositionne la position à 0 pour lecture
        return buffer;
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
         * On s’attend à ce que :
         *  - La gare 20 ait deux changements arrivant (#0 et #1)
         *  - La gare 16 ait un changement arrivant (#2)
         *  - Toute autre gare (ex. 999) n’ait aucun changement arrivant
         *
         * La méthode arrivingAt(stationId) renvoie un intervalle empaqueté (start<<16 | end)
         * qu’on peut décompacter pour vérifier le nombre d’éléments attendus.
         */

        int interval20 = transfers.arrivingAt(20);
        int start20 = interval20 >>> 16;
        int end20   = interval20 & 0xFFFF;
        // L’intervalle devrait contenir les indices [0..2) => 0 et 1
        assertEquals(0, start20);
        assertEquals(2, end20);

        int interval16 = transfers.arrivingAt(16);
        int start16 = interval16 >>> 16;
        int end16   = interval16 & 0xFFFF;
        // L’intervalle devrait contenir [2..3) => juste l’indice 2
        assertEquals(2, start16);
        assertEquals(3, end16);

        int interval999 = transfers.arrivingAt(999);
        int start999 = interval999 >>> 16;
        int end999   = interval999 & 0xFFFF;
        // Pas de changement arrivant à 999 => intervalle vide [0..0)
        assertEquals(0, start999);
        assertEquals(0, end999);
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