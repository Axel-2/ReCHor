package ch.epfl.rechor.timetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyPlatformsTest {

    // Fausse impl de Platforms pour le test
    private final Platforms platforms = new Platforms() {

        // Définition de quelques plateformes
        private final String[] names = {"70", "", "A", "B"};
        private final int[] stationIds = {0, 0, 1, 1};

        @Override
        public int size() {
            return names.length;
        }

        @Override
        public String name(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            }
            return names[id];
        }

        @Override
        public int stationId(int id) {
            if (id < 0 || id >= size()) {
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            }
            return stationIds[id];
        }
    };

    @Test
    void name() {
        // Tests pour des indices valides
        assertEquals("70", platforms.name(0));
        assertEquals("", platforms.name(1));
        assertEquals("A", platforms.name(2));
        assertEquals("B", platforms.name(3));

        assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(4));
    }

    @Test
    void stationId() {

        assertEquals(0, platforms.stationId(0));
        assertEquals(0, platforms.stationId(1));
        assertEquals(1, platforms.stationId(2));
        assertEquals(1, platforms.stationId(3));

        assertThrows(IndexOutOfBoundsException.class, () -> platforms.stationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> platforms.stationId(4));
    }
}