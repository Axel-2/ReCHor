package ch.epfl.rechor.timetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyStationsTest {

    // Implémentation factice de l’interface Stations
    private final Stations stations = new Stations() {
        private final String[] names = { "Lausanne", "Genève", "Neuchâtel" };
        private final double[] lons  = { 46.5191,   46.2044,  46.991  };
        private final double[] lats  = { 6.6323,    6.1432,   6.9293  };

        @Override
        public int size() {
            return names.length;
        }

        @Override
        public String name(int id) {
            return names[id];
        }

        @Override
        public double longitude(int id) {
            return lons[id];
        }

        @Override
        public double latitude(int id) {
            return lats[id];
        }
    };

    @Test
    void name() {
        assertEquals("Lausanne",  stations.name(0));
        assertEquals("Genève",    stations.name(1));
        assertEquals("Neuchâtel", stations.name(2));
    }

    @Test
    void longitude() {
        assertEquals(46.5191, stations.longitude(0));
        assertEquals(46.2044, stations.longitude(1));
        assertEquals(46.991,  stations.longitude(2));
    }

    @Test
    void latitude() {
        assertEquals(6.6323, stations.latitude(0));
        assertEquals(6.1432, stations.latitude(1));
        assertEquals(6.9293, stations.latitude(2));
    }
}