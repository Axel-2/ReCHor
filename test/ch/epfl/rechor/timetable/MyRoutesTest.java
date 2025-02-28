package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyRoutesTest {

    // Implémentation factice de Routes pour le test via une classe anonyme
    private final Routes routes = new Routes() {
        // Tableaux simulant des données de test
        private final String[] names = {"IR 15", "Metro line 2", "Bus 42"};
        private final Vehicle[] vehicles = {Vehicle.TRAIN, Vehicle.METRO, Vehicle.BUS};

        @Override
        public int size() {
            return names.length;
        }

        @Override
        public Vehicle vehicle(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return vehicles[id];
        }

        @Override
        public String name(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Index invalide: " + id);
            return names[id];
        }
    };

    @Test
    void vehicle() {
        // Vérifie le comportement avec des indices valides
        assertEquals(Vehicle.TRAIN, routes.vehicle(0));
        assertEquals(Vehicle.METRO, routes.vehicle(1));
        assertEquals(Vehicle.BUS, routes.vehicle(2));

        // Vérifie que l'appel avec un indice invalide (négatif ou hors bornes) lève bien une exception
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(3));
    }

    @Test
    void name() {
        // Vérifie le comportement avec des indices valides
        assertEquals("IR 15", routes.name(0));
        assertEquals("Metro line 2", routes.name(1));
        assertEquals("Bus 42", routes.name(2));

        // Vérifie que l'appel avec un indice invalide (négatif ou hors bornes) lève bien une exception
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(3));
    }
}