package ch.epfl.rechor.timetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyTripsTest {


    // Fausse impl de Trips pour les tests
    private final Trips trips = new Trips() {

        private final int[] routeIds = {0, 1, 2};
        private final String[] destinations = {"Paris", "Lyon", "Marseille"};

        @Override
        public int size() {
            return destinations.length;
        }

        @Override
        public int routeId(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException("Indice invalide: " + id);
            }
            return routeIds[id];
        }

        @Override
        public String destination(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException("Indice invalide: " + id);
            }
            return destinations[id];
        }
    };

    @Test
    void routeId() {
        assertEquals(0, trips.routeId(0));
        assertEquals(1, trips.routeId(1));
        assertEquals(2, trips.routeId(2));

        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(3));

    }

    @Test
    void destination() {
        assertEquals("Paris", trips.destination(0));
        assertEquals("Lyon", trips.destination(1));
        assertEquals("Marseille", trips.destination(2));

        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(3));
    }
}