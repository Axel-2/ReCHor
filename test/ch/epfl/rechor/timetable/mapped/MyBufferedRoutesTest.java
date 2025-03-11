package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedRoutesTest {

    // Création d'un buffer factice contenant des données aplaties
    private static ByteBuffer createTestBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(9); // 3 entrées (U16 + U8)
        buffer.putShort((short) 0); // Index du nom de la première ligne
        buffer.put((byte) 1); // Type METRO (Vehicle 1)

        buffer.putShort((short) 1); // Index du nom de la deuxième ligne
        buffer.put((byte) 3); // Type BUS (Vehicle 3)

        buffer.putShort((short) 2); // Index du nom de la troisième ligne
        buffer.put((byte) 5); // Type AERIAL_LIFT (Vehicle 5)

        buffer.rewind();
        return buffer;
    }

    private final List<String> testStringTable = List.of("Ligne 1", "Ligne 2", "Ligne 3");
    private final Routes routes = new BufferedRoutes(testStringTable, createTestBuffer());

    /**
     * Teste la méthode name(int id) en vérifiant les noms attendus.
     */
    @Test
    void nameReturnsCorrectNames() {
        assertEquals("Ligne 1", routes.name(0));
        assertEquals("Ligne 2", routes.name(1));
        assertEquals("Ligne 3", routes.name(2));
    }

    /**
     * Teste la méthode vehicle(int id) en vérifiant que les bons types de véhicules sont retournés.
     */
    @Test
    void vehicleReturnsCorrectVehicle() {
        assertEquals(Vehicle.METRO, routes.vehicle(0));    // Type 1 = METRO
        assertEquals(Vehicle.BUS, routes.vehicle(1));      // Type 3 = BUS
        assertEquals(Vehicle.AERIAL_LIFT, routes.vehicle(2));// Type 5 = AERIAL_LIFT
    }

    /**
     * Teste la méthode size() pour vérifier que la taille est correcte.
     */
    @Test
    void sizeReturnsCorrectNumberOfRoutes() {
        assertEquals(3, routes.size());
    }
}