package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedTripsTest {

    // Création d'un buffer factice contenant des données aplaties
    private static ByteBuffer createTestBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(12); // 3 entrées × (U16 + U16) = 6 octets

        buffer.putShort((short) 10); // Ligne associée à la première course (index 10)
        buffer.putShort((short) 0);  // Destination indexée par 0 ("Gare Centrale")

        buffer.putShort((short) 20); // Ligne associée à la deuxième course (index 20)
        buffer.putShort((short) 1);  // Destination indexée par 1 ("Aéroport")

        buffer.putShort((short) 30); // Ligne associée à la troisième course (index 30)
        buffer.putShort((short) 2);  // Destination indexée par 2 ("Université")

        buffer.rewind();
        return buffer;
    }

    // Table des noms pour tester les destinations
    private final List<String> testStringTable = List.of("Gare Centrale", "Aéroport", "Université");
    private final Trips trips = new BufferedTrips(testStringTable, createTestBuffer());

    /**
     * Teste la méthode routeId(int id) en vérifiant les lignes associées aux courses.
     */
    @Test
    void routeIdReturnsCorrectValues() {
        assertEquals(10, trips.routeId(0)); // Première course → Ligne 10
        assertEquals(20, trips.routeId(1)); // Deuxième course → Ligne 20
        assertEquals(30, trips.routeId(2)); // Troisième course → Ligne 30
    }

    /**
     * Teste la méthode destination(int id) en vérifiant que les destinations sont correctes.
     */
    @Test
    void destinationReturnsCorrectValues() {
        assertEquals("Gare Centrale", trips.destination(0));
        assertEquals("Aéroport", trips.destination(1));
        assertEquals("Université", trips.destination(2));
    }

    /**
     * Teste la méthode size() pour vérifier que la taille est correcte.
     */
    @Test
    void sizeReturnsCorrectNumberOfTrips() {
        assertEquals(3, trips.size());
    }
}