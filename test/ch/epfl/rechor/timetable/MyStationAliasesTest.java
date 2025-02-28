package ch.epfl.rechor.timetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyStationAliasesTest {


    // Fausse impl de StationAliases
    private final StationAliases stationAliases = new StationAliases() {

        private final String[] aliases = { "Losanna", "Zürich", "Basle" };
        private final String[] stationNames = { "Lausanne", "Zürich", "Bâle" };

        @Override
        public int size() {
            return aliases.length;
        }

        @Override
        public String alias(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return aliases[id];
        }

        @Override
        public String stationName(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return stationNames[id];
        }
    };

    @Test
    void alias() {
        // indices valides
        assertEquals("Losanna", stationAliases.alias(0));
        assertEquals("Zürich",  stationAliases.alias(1));
        assertEquals("Basle",   stationAliases.alias(2));

        //  indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.alias(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.alias(3));
    }

    @Test
    void stationName() {
        // indices valides
        assertEquals("Lausanne", stationAliases.stationName(0));
        assertEquals("Zürich",   stationAliases.stationName(1));
        assertEquals("Bâle",     stationAliases.stationName(2));

        //  indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.stationName(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.stationName(3));
    }
}