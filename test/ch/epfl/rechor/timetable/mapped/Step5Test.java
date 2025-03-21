package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.journey.Vehicle;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Classe de tests JUnit pour l'étape 5.
 * Les tests vérifient l'ensemble des fonctions publiques de :
 * - BufferedRoutes
 * - BufferedTrips
 * - BufferedConnections
 * - BufferedTransfers
 *
 * Ces tests se basent sur les structures de données "aplatie" décrites dans le cahier des charges.
 */
public class Step5Test {

    // --- Tests pour BufferedRoutes ---
    @Test
    void testBufferedRoutesValid() {
        // La table aplatie des lignes contient 2 champs :
        // - NAME_ID (U16, 2 octets)
        // - KIND (U8, 1 octet)
        // Chaque enregistrement occupe donc 3 octets.
        // Création de 2 enregistrements :
        // Enregistrement 0 : NAME_ID = 0, KIND = 0 (donc Vehicle.TRAM)
        // Enregistrement 1 : NAME_ID = 1, KIND = 2 (donc Vehicle.TRAIN)
        byte[] routesData = new byte[6];
        // Enregistrement 0
        routesData[0] = 0; routesData[1] = 0; // U16 = 0
        routesData[2] = 0;                   // U8 = 0 => Vehicle.TRAM
        // Enregistrement 1
        routesData[3] = 0; routesData[4] = 1; // U16 = 1
        routesData[5] = 2;                   // U8 = 2 => Vehicle.TRAIN

        ByteBuffer routesBuffer = ByteBuffer.wrap(routesData);
        List<String> stringTable = List.of("LineA", "LineB");

        BufferedRoutes routes = new BufferedRoutes(stringTable, routesBuffer);

        assertEquals(2, routes.size(), "Il devrait y avoir 2 lignes");
        assertEquals("LineA", routes.name(0));
        assertEquals(Vehicle.TRAM, routes.vehicle(0));
        assertEquals("LineB", routes.name(1));
        assertEquals(Vehicle.TRAIN, routes.vehicle(1));

        // Vérification des accès avec indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(2));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
    }

    // --- Tests pour BufferedTrips ---
    @Test
    void testBufferedTripsValid() {
        // La table aplatie des courses contient 2 champs :
        // - ROUTE_ID (U16, 2 octets)
        // - DESTINATION_ID (U16, 2 octets)
        // Chaque enregistrement occupe 4 octets.
        byte[] tripsData = new byte[8];
        // Enregistrement 0 : ROUTE_ID = 1, DESTINATION_ID = 2
        tripsData[0] = 0; tripsData[1] = 1; // 1
        tripsData[2] = 0; tripsData[3] = 2; // 2
        // Enregistrement 1 : ROUTE_ID = 0, DESTINATION_ID = 0
        tripsData[4] = 0; tripsData[5] = 0;
        tripsData[6] = 0; tripsData[7] = 0;

        ByteBuffer tripsBuffer = ByteBuffer.wrap(tripsData);
        // La table des chaînes utilisée pour la destination
        List<String> stringTable = List.of("Dest0", "Dest1", "Dest2");

        BufferedTrips trips = new BufferedTrips(stringTable, tripsBuffer);

        assertEquals(2, trips.size(), "Il devrait y avoir 2 courses");
        assertEquals(1, trips.routeId(0));
        assertEquals("Dest2", trips.destination(0)); // DESTINATION_ID = 2
        assertEquals(0, trips.routeId(1));
        assertEquals("Dest0", trips.destination(1));

        // Vérification des indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(2));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(-1));
    }

    // --- Tests pour BufferedConnections ---
    @Test
    void testBufferedConnectionsValid() {
        // La table aplatie des liaisons contient 5 champs :
        // - DEP_STOP_ID (U16, 2 octets)
        // - DEP_MINUTES (U16, 2 octets)
        // - ARR_STOP_ID (U16, 2 octets)
        // - ARR_MINUTES (U16, 2 octets)
        // - TRIP_POS_ID (S32, 4 octets)
        // Chaque enregistrement occupe 12 octets.
        // Simulons 2 liaisons appartenant à la même course (par exemple, course d'index 5).
        // Pour TRIP_POS_ID, on emballe : les 24 bits de poids fort pour l’index de course et 8 bits pour la position.
        // Pour la première liaison : position 0, donc TRIP_POS_ID = (5 << 8) | 0 = 1280.
        // Pour la seconde liaison : position 1, donc TRIP_POS_ID = (5 << 8) | 1 = 1281.
        byte[] connectionsData = new byte[24];
        ByteBuffer connBuffer = ByteBuffer.wrap(connectionsData);
        // Enregistrement 0
        connBuffer.putShort((short) 100);   // DEP_STOP_ID = 100
        connBuffer.putShort((short) 500);     // DEP_MINUTES = 500
        connBuffer.putShort((short) 101);     // ARR_STOP_ID = 101
        connBuffer.putShort((short) 510);     // ARR_MINUTES = 510
        connBuffer.putInt(1280);              // TRIP_POS_ID = 1280 (course 5, position 0)
        // Enregistrement 1
        connBuffer.putShort((short) 101);     // DEP_STOP_ID = 101
        connBuffer.putShort((short) 520);     // DEP_MINUTES = 520
        connBuffer.putShort((short) 102);     // ARR_STOP_ID = 102
        connBuffer.putShort((short) 530);     // ARR_MINUTES = 530
        connBuffer.putInt(1281);              // TRIP_POS_ID = 1281 (course 5, position 1)
        connBuffer.flip();  // Prépare le buffer pour la lecture

        // La table auxiliaire des liaisons suivantes contient un seul champ (S32, 4 octets) par enregistrement.
        // Pour l'enregistrement 0, la liaison suivante est celle d'index 1.
        // Pour l'enregistrement 1 (la dernière liaison), la liaison suivante est la première de la course (index 0).
        byte[] succData = new byte[8];
        ByteBuffer succBuffer = ByteBuffer.wrap(succData);
        succBuffer.putInt(1); // Pour la liaison 0
        succBuffer.putInt(0); // Pour la liaison 1 (circulaire)
        succBuffer.flip();

        BufferedConnections connections = new BufferedConnections(connBuffer, succBuffer);

        assertEquals(2, connections.size(), "Il devrait y avoir 2 liaisons");

        // Vérification de la première liaison
        assertEquals(100, connections.depStopId(0));
        assertEquals(500, connections.depMins(0));
        assertEquals(101, connections.arrStopId(0));
        assertEquals(510, connections.arrMins(0));
        // Extraction du trip id (24 bits de poids fort)
        assertEquals(5, connections.tripId(0));
        // Position dans la course (8 bits de poids faible)
        assertEquals(0, connections.tripPos(0));
        assertEquals(1, connections.nextConnectionId(0));

        // Vérification de la seconde liaison
        assertEquals(101, connections.depStopId(1));
        assertEquals(520, connections.depMins(1));
        assertEquals(102, connections.arrStopId(1));
        assertEquals(530, connections.arrMins(1));
        assertEquals(5, connections.tripId(1));
        assertEquals(1, connections.tripPos(1));
        // Pour la dernière liaison, nextConnectionId doit retourner l’index de la première liaison de la course (0)
        assertEquals(0, connections.nextConnectionId(1));

        // Vérification des accès avec indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(2));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(-1));
    }

    // --- Tests pour BufferedTransfers ---
    @Test
    void testBufferedTransfersValid() {
        // La table aplatie des changements contient 3 champs :
        // - DEP_STATION_ID (U16, 2 octets)
        // - ARR_STATION_ID (U16, 2 octets)
        // - TRANSFER_MINUTES (U8, 1 octet)
        // Chaque enregistrement occupe 5 octets.
        // Créons 3 enregistrements.
        byte[] transfersData = new byte[15];
        ByteBuffer transfersBuffer = ByteBuffer.wrap(transfersData);
        // Enregistrement 0 : de la gare 10 vers la gare 20, durée 5 minutes
        transfersBuffer.putShort((short) 10);
        transfersBuffer.putShort((short) 20);
        transfersBuffer.put((byte) 5);
        // Enregistrement 1 : de la gare 11 vers la gare 20, durée 3 minutes
        transfersBuffer.putShort((short) 11);
        transfersBuffer.putShort((short) 20);
        transfersBuffer.put((byte) 3);
        // Enregistrement 2 : de la gare 12 vers la gare 21, durée 7 minutes
        transfersBuffer.putShort((short) 12);
        transfersBuffer.putShort((short) 21);
        transfersBuffer.put((byte) 7);
        transfersBuffer.flip();

        BufferedTransfers transfers = new BufferedTransfers(transfersBuffer);

        assertEquals(3, transfers.size(), "Il devrait y avoir 3 changements");

        // Vérification des getters pour chaque enregistrement
        assertEquals(10, transfers.depStationId(0));
        //assertEquals(20, transfers.arrStationId(0));
        assertEquals(5, transfers.minutes(0));

        assertEquals(11, transfers.depStationId(1));
        //assertEquals(20, transfers.arrStationId(1));
        assertEquals(3, transfers.minutes(1));

        assertEquals(12, transfers.depStationId(2));
        //assertEquals(21, transfers.arrStationId(2));
        assertEquals(7, transfers.minutes(2));

        // Test de minutesBetween pour des transferts existants
        assertEquals(5, transfers.minutesBetween(10, 20), "Le transfert de 10 vers 20 doit durer 5 minutes");
        assertEquals(7, transfers.minutesBetween(12, 21), "Le transfert de 12 vers 21 doit durer 7 minutes");
        // Aucun transfert entre la gare 10 et 21, doit lever une exception
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(10, 21));

        // Test de arrivingAt :
        // On suppose que la table est organisée de façon à ce que tous les changements
        // pour une gare donnée soient consécutifs.
        // Ici, pour la gare 20, les enregistrements 0 et 1 ont ARR_STATION_ID = 20.
        // La méthode arrivingAt retourne un entier empaqueté dans lequel, par convention,
        // les 16 bits de poids faible représentent l'indice de départ et les 16 bits de poids fort le nombre d'éléments.
        int packedInterval = transfers.arrivingAt(20);
        int start = PackedRange.startInclusive(packedInterval);
        int count = PackedRange.endExclusive(packedInterval);
        assertEquals(0, start, "L'indice de départ pour les transferts vers la gare 20 devrait être 0");
        assertEquals(2, count, "Il devrait y avoir 2 transferts vers la gare 20");

//        // Pour une gare sans transfert (par exemple, gare 30), on s'attend à un intervalle indiquant aucun élément
//        int packedIntervalNo = transfers.arrivingAt(30);
//        int countNo = (packedIntervalNo >> 16) & 0xFFFF;
//        assertEquals(0, countNo, "Aucun transfert ne doit être trouvé pour la gare 30");

        // Vérification des indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(3));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1));
    }
}
