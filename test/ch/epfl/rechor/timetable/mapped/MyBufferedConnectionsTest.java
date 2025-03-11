package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;

class MyBufferedConnectionsTest {

    // Création d'un buffer factice pour les liaisons
    private static ByteBuffer createConnectionsBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(24); // 2 connexions × (4 × U16 + S32) = 20 octets

        // Première connexion
        buffer.putShort((short) 100); // depStopId = 100
        buffer.putShort((short) 500); // depMins = 500 min après minuit
        buffer.putShort((short) 200); // arrStopId = 200
        buffer.putShort((short) 550); // arrMins = 550 min après minuit
        buffer.putInt((10 << 8) | 2); // tripId = 10, tripPos = 2 (stocké en 32 bits)

        // Deuxième connexion
        buffer.putShort((short) 200); // depStopId = 200
        buffer.putShort((short) 600); // depMins = 600 min après minuit
        buffer.putShort((short) 300); // arrStopId = 300
        buffer.putShort((short) 650); // arrMins = 650 min après minuit
        buffer.putInt((12 << 8) | 0); // tripId = 12, tripPos = 0 (stocké en 32 bits)

        buffer.rewind();
        return buffer;
    }

    // Création d'un buffer factice pour les connexions suivantes
    private static ByteBuffer createNextBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(8); // 2 connexions × S32 = 8 octets
        IntBuffer intBuffer = buffer.asIntBuffer();

        intBuffer.put(1);  // Connexion 0 → Connexion 1
        intBuffer.put(0);  // Connexion 1 → Connexion 0 (cycle)

        buffer.rewind();
        return buffer;
    }

    private final Connections connections = new BufferedConnections(createConnectionsBuffer(), createNextBuffer());

    /**
     * Teste les méthodes depStopId et arrStopId.
     */
    @Test
    void stopIdsAreCorrect() {
        assertEquals(100, connections.depStopId(0));
        assertEquals(200, connections.arrStopId(0));
        assertEquals(200, connections.depStopId(1));
        assertEquals(300, connections.arrStopId(1));
    }

    /**
     * Teste les heures de départ et d'arrivée.
     */
    @Test
    void minutesAreCorrect() {
        assertEquals(500, connections.depMins(0));
        assertEquals(550, connections.arrMins(0));
        assertEquals(600, connections.depMins(1));
        assertEquals(650, connections.arrMins(1));
    }

    /**
     * Teste les tripId et tripPos.
     */
    @Test
    void tripIdAndPositionAreCorrect() {
        assertEquals(10, connections.tripId(0)); // Stocké dans les 24 bits de poids fort
        assertEquals(2, connections.tripPos(0)); // Stocké dans les 8 bits de poids faible
        assertEquals(12, connections.tripId(1));
        assertEquals(0, connections.tripPos(1));
    }

    /**
     * Teste nextConnectionId.
     */
    @Test
    void nextConnectionIsCorrect() {
        assertEquals(1, connections.nextConnectionId(0)); // Connexion 0 → Connexion 1
        assertEquals(0, connections.nextConnectionId(1)); // Connexion 1 → Connexion 0 (cycle)
    }

    /**
     * Teste la taille des connexions.
     */
    @Test
    void sizeIsCorrect() {
        assertEquals(2, connections.size());
    }
}