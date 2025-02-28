package ch.epfl.rechor.timetable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyConnectionsTest {

    // Fausse impl  de Connections pour les tests.

    // Nous créeons ici 5 liaisons (indices de 0 à 4).
    private final Connections connections = new Connections() {

        // Données
        private final int[] depStopIds   = {10, 20, 1100, 15, 25};
        private final int[] depMinsArr   = {600, 550, 500, 450, 400};
        private final int[] arrStopIds   = {11, 21, 1101, 16, 26};
        private final int[] arrMinsArr   = {620, 570, 520, 470, 420};
        private final int[] tripIds      = {0, 0, 1, 2, 2};
        private final int[] tripPositions= {0, 1, 0, 0, 1};
        private final int[] nextConn     = {1, 0, 2, 4, 3};

        @Override
        public int size() {
            return depStopIds.length;
        }

        @Override
        public int depStopId(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return depStopIds[id];
        }

        @Override
        public int depMins(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return depMinsArr[id];
        }

        @Override
        public int arrStopId(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return arrStopIds[id];
        }

        @Override
        public int arrMins(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return arrMinsArr[id];
        }

        @Override
        public int tripId(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return tripIds[id];
        }

        @Override
        public int tripPos(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return tripPositions[id];
        }

        @Override
        public int nextConnectionId(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return nextConn[id];
        }
    };

    @Test
    void depStopId() {
        // Tests pour des indices valides.
        assertEquals(10, connections.depStopId(0));
        assertEquals(20, connections.depStopId(1));
        assertEquals(1100, connections.depStopId(2));
        assertEquals(15, connections.depStopId(3));
        assertEquals(25, connections.depStopId(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(5));
    }

    @Test
    void depMins() {
        assertEquals(600, connections.depMins(0));
        assertEquals(550, connections.depMins(1));
        assertEquals(500, connections.depMins(2));
        assertEquals(450, connections.depMins(3));
        assertEquals(400, connections.depMins(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.depMins(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depMins(5));
    }

    @Test
    void arrStopId() {
        assertEquals(11, connections.arrStopId(0));
        assertEquals(21, connections.arrStopId(1));
        assertEquals(1101, connections.arrStopId(2));
        assertEquals(16, connections.arrStopId(3));
        assertEquals(26, connections.arrStopId(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrStopId(5));
    }

    @Test
    void arrMins() {
        assertEquals(620, connections.arrMins(0));
        assertEquals(570, connections.arrMins(1));
        assertEquals(520, connections.arrMins(2));
        assertEquals(470, connections.arrMins(3));
        assertEquals(420, connections.arrMins(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrMins(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrMins(5));
    }

    @Test
    void tripId() {
        assertEquals(0, connections.tripId(0));
        assertEquals(0, connections.tripId(1));
        assertEquals(1, connections.tripId(2));
        assertEquals(2, connections.tripId(3));
        assertEquals(2, connections.tripId(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripId(5));
    }

    @Test
    void tripPos() {
        assertEquals(0, connections.tripPos(0));
        assertEquals(1, connections.tripPos(1));
        assertEquals(0, connections.tripPos(2));
        assertEquals(0, connections.tripPos(3));
        assertEquals(1, connections.tripPos(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripPos(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripPos(5));
    }

    @Test
    void nextConnectionId() {
        assertEquals(1, connections.nextConnectionId(0));
        assertEquals(0, connections.nextConnectionId(1));
        assertEquals(2, connections.nextConnectionId(2)); // Pour la course d'une seule liaison.
        assertEquals(4, connections.nextConnectionId(3));
        assertEquals(3, connections.nextConnectionId(4));

        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(5));
    }
}
