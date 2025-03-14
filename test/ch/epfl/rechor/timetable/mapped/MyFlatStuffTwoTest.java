package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MyFlatStuffTwoTest {

    @Test
    public void testBufferedConnections() {
        // Allocate a buffer for 2 connection records.
        ByteBuffer connectionsBuffer = ByteBuffer.allocate(12 * 2);
        // Record 0:
        connectionsBuffer.putShort((short) 10);    // DEP_STOP_ID = 10
        connectionsBuffer.putShort((short) 500);   // DEP_MINUTES = 500
        connectionsBuffer.putShort((short) 20);    // ARR_STOP_ID = 20
        connectionsBuffer.putShort((short) 510);   // ARR_MINUTES = 510
        // Pack trip id = 1 and trip pos = 0 → (1 << 8) | 0 = 256.
        connectionsBuffer.putInt(256);
        // Record 1:
        connectionsBuffer.putShort((short) 30);    // DEP_STOP_ID = 30
        connectionsBuffer.putShort((short) 600);   // DEP_MINUTES = 600
        connectionsBuffer.putShort((short) 40);    // ARR_STOP_ID = 40
        connectionsBuffer.putShort((short) 610);   // ARR_MINUTES = 610
        // Pack trip id = 2 and trip pos = 5 → (2 << 8) | 5 = 517.
        connectionsBuffer.putInt(517);
        connectionsBuffer.flip();

        // Create succBuffer: one int per record.
        ByteBuffer succBufferBB = ByteBuffer.allocate(4 * 2);
        succBufferBB.putInt(1);  // For record 0, next connection = 1.
        succBufferBB.putInt(0);  // For record 1, next connection = 0 (circular linking).
        succBufferBB.flip();

        BufferedConnections bc = new BufferedConnections(connectionsBuffer, succBufferBB);
        assertEquals(2, bc.size(), "Should have 2 connections");

        // Validate record 0.
        assertEquals(10, bc.depStopId(0), "Record 0: depStopId");
        assertEquals(500, bc.depMins(0), "Record 0: depMins");
        assertEquals(20, bc.arrStopId(0), "Record 0: arrStopId");
        assertEquals(510, bc.arrMins(0), "Record 0: arrMins");
        // Trip id is extracted from (256 >>> 8) = 1.
        assertEquals(1, bc.tripId(0), "Record 0: tripId");
        // Trip pos: 256 & 0xFF = 0.
        assertEquals(0, bc.tripPos(0), "Record 0: tripPos");
        // Next connection index from succBuffer.
        assertEquals(1, bc.nextConnectionId(0), "Record 0: nextConnectionId");

        // Validate record 1.
        assertEquals(30, bc.depStopId(1), "Record 1: depStopId");
        assertEquals(600, bc.depMins(1), "Record 1: depMins");
        assertEquals(40, bc.arrStopId(1), "Record 1: arrStopId");
        assertEquals(610, bc.arrMins(1), "Record 1: arrMins");
        // For record 1: (517 >>> 8) = 2.
        assertEquals(2, bc.tripId(1), "Record 1: tripId");
        // Trip pos: 517 & 0xFF = 5.
        assertEquals(5, bc.tripPos(1), "Record 1: tripPos");
        assertEquals(0, bc.nextConnectionId(1), "Record 1: nextConnectionId");

        // Test out-of-bound access.
        assertThrows(IndexOutOfBoundsException.class, () -> bc.depStopId(2));
    }

    /**
     * Test for BufferedTrips.
     *
     * Each trip record is 4 bytes:
     *   - ROUTE_ID (U16, 2 bytes)
     *   - DESTINATION_ID (U16, 2 bytes)
     *
     * The destination method uses a string table.
     */
    @Test
    public void testBufferedTrips() {
        // Create a string table.
        List<String> stringTable = List.of("RouteA", "RouteB", "DestA", "DestB");
        // Allocate a ByteBuffer for 2 trip records (4 bytes each).
        ByteBuffer tripsBuffer = ByteBuffer.allocate(4 * 2);
        // Record 0: ROUTE_ID = 0, DESTINATION_ID = 2 → destination "DestA".
        tripsBuffer.putShort((short) 0);
        tripsBuffer.putShort((short) 2);
        // Record 1: ROUTE_ID = 1, DESTINATION_ID = 3 → destination "DestB".
        tripsBuffer.putShort((short) 1);
        tripsBuffer.putShort((short) 3);
        tripsBuffer.flip();

        BufferedTrips bt = new BufferedTrips(stringTable, tripsBuffer);
        assertEquals(2, bt.size(), "Should have 2 trips");

        // Validate record 0.
        assertEquals(0, bt.routeId(0), "Trip 0: routeId");
        assertEquals("DestA", bt.destination(0), "Trip 0: destination");

        // Validate record 1.
        assertEquals(1, bt.routeId(1), "Trip 1: routeId");
        assertEquals("DestB", bt.destination(1), "Trip 1: destination");

        // Out-of-bound check.
        assertThrows(IndexOutOfBoundsException.class, () -> bt.routeId(2));
    }

    /**
     * Test for BufferedRoutes.
     *
     * Each route record is 3 bytes:
     *   - U16 for the name index (2 bytes)
     *   - U8 for the vehicle kind (1 byte)
     *
     * The name is looked up in a string table.
     * The vehicle method returns Vehicle.ALL.get(kind).
     */
    @Test
    public void testBufferedRoutes() {
        // Create a string table for routes.
        List<String> stringTable = List.of("RouteX", "RouteY");
        // Allocate a ByteBuffer for 2 route records (3 bytes each).
        ByteBuffer routesBuffer = ByteBuffer.allocate(3 * 2);
        // Record 0: NAME_ID = 0, KIND = 2.
        routesBuffer.putShort((short) 0);
        routesBuffer.put((byte) 2);
        // Record 1: NAME_ID = 1, KIND = 3.
        routesBuffer.putShort((short) 1);
        routesBuffer.put((byte) 3);
        routesBuffer.flip();

        BufferedRoutes br = new BufferedRoutes(stringTable, routesBuffer);
        assertEquals(2, br.size(), "Should have 2 routes");

        // Validate record 0.
        assertEquals("RouteX", br.name(0), "Route 0: name");
        // Check vehicle: expecting Vehicle.ALL.get(2).
        assertEquals(Vehicle.ALL.get(2), br.vehicle(0), "Route 0: vehicle");

        // Validate record 1.
        assertEquals("RouteY", br.name(1), "Route 1: name");
        assertEquals(Vehicle.ALL.get(3), br.vehicle(1), "Route 1: vehicle");

        // Out-of-bound access.
        assertThrows(IndexOutOfBoundsException.class, () -> br.name(2));
    }

    /**
     * Helper method to create a ByteBuffer with 4 transfer records.
     * Each record is 5 bytes long:
     *   - DEP_STATION_ID (U16)
     *   - ARR_STATION_ID (U16)
     *   - TRANSFER_MINUTES (U8)
     *
     * The records are arranged in order by ARR_STATION_ID:
     * Record 0: DEP = 10, ARR = 0, minutes = 5
     * Record 1: DEP = 11, ARR = 0, minutes = 7
     * Record 2: DEP = 10, ARR = 1, minutes = 12
     * Record 3: DEP = 12, ARR = 1, minutes = 15
     */
    /**
     * Helper method to create a ByteBuffer with 4 transfer records.
     * Each record is 5 bytes long:
     *   - DEP_STATION_ID (U16)
     *   - ARR_STATION_ID (U16)
     *   - TRANSFER_MINUTES (U8)
     *
     * The records are arranged in order by ARR_STATION_ID:
     * Record 0: DEP = 0, ARR = 0, minutes = 5
     * Record 1: DEP = 1, ARR = 0, minutes = 7
     * Record 2: DEP = 0, ARR = 1, minutes = 12
     * Record 3: DEP = 0, ARR = 1, minutes = 15
     */
    private ByteBuffer createTransfersBuffer() {
        // 4 records * 5 bytes = 20 bytes.
        ByteBuffer buffer = ByteBuffer.allocate(20);
        // Record 0:
        buffer.putShort((short) 0); // DEP = 0
        buffer.putShort((short) 0); // ARR = 0
        buffer.put((byte) 5);       // minutes = 5
        // Record 1:
        buffer.putShort((short) 1); // DEP = 1
        buffer.putShort((short) 0); // ARR = 0
        buffer.put((byte) 7);       // minutes = 7
        // Record 2:
        buffer.putShort((short) 0); // DEP = 0
        buffer.putShort((short) 1); // ARR = 1
        buffer.put((byte) 12);      // minutes = 12
        // Record 3:
        buffer.putShort((short) 0); // DEP = 0
        buffer.putShort((short) 1); // ARR = 1
        buffer.put((byte) 15);      // minutes = 15
        buffer.flip();
        return buffer;
    }

    @Test
    public void testArrivingAt() {
        BufferedTransfers transfers = new BufferedTransfers(createTransfersBuffer());
        // There are 4 records. The maximum ARR station among records is 1,
        // so transfersPerStationTable should have length = max(arrival)+1 = 1+1 = 2.
        // For ARR station 0, records 0 and 1 apply.
        int packed0 = transfers.arrivingAt(0);
        int start0 = PackedRange.startInclusive(packed0);
        int end0 = PackedRange.endExclusive(packed0);
        assertEquals(0, start0, "For ARR station 0, start index should be 0");
        assertEquals(2, end0, "For ARR station 0, end index should be 2");

        // For ARR station 1, records 2 and 3 apply.
        int packed1 = transfers.arrivingAt(1);
        int start1 = PackedRange.startInclusive(packed1);
        int end1 = PackedRange.endExclusive(packed1);
        assertEquals(2, start1, "For ARR station 1, start index should be 2");
        assertEquals(4, end1, "For ARR station 1, end index should be 4");

        // Requesting a station id outside the valid range should throw an exception.
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(2));
    }

    @Test
    public void testMinutesBetween() {
        BufferedTransfers transfers = new BufferedTransfers(createTransfersBuffer());
        // Valid station IDs are now 0 and 1.
        // For ARR station 0:
        //   - Record 0: (dep=0, minutes=5)
        //   - Record 1: (dep=1, minutes=7)
        // For ARR station 1:
        //   - Record 2: (dep=0, minutes=12)
        //   - Record 3: (dep=0, minutes=15)

        // If there is a matching transfer, the first one (in sorted order) is returned.
        assertEquals(5, transfers.minutesBetween(0, 0),
                "minutesBetween(0,0) should return 5");
        assertEquals(7, transfers.minutesBetween(1, 0),
                "minutesBetween(1,0) should return 7");
        assertEquals(12, transfers.minutesBetween(0, 1),
                "minutesBetween(0,1) should return 12");

        // There is no transfer from 1 to 1 (since neither record has DEP == 1 and ARR == 1)
        assertThrows(NoSuchElementException.class,
                () -> transfers.minutesBetween(1, 1),
                "minutesBetween(1,1) should throw NoSuchElementException");

        // Out-of-range station IDs (valid ones are 0 and 1) should throw IndexOutOfBoundsException.
        assertThrows(IndexOutOfBoundsException.class,
                () -> transfers.minutesBetween(2, 0),
                "dep=2 is out of range, should throw IndexOutOfBoundsException");
        assertThrows(IndexOutOfBoundsException.class,
                () -> transfers.minutesBetween(0, 2),
                "arr=2 is out of range, should throw IndexOutOfBoundsException");
    }
}




class FlattenedRailwayData {
    // Constants for data sizes
    public static final int NUM_STATIONS = 2000;
    public static final int NUM_LINES = 500;
    public static final int NUM_TRIPS = 5000;
    public static final int NUM_CONNECTIONS = 20000;
    public static final int NUM_TRANSFERS = 10000;
    private static final int STRING_TABLE_SIZE = 5000; // Number of unique strings

    // Random generator
    private static final Random RANDOM = new Random();

    // Flattened Data Arrays
    public static final byte[] stationData = new byte[NUM_STATIONS * 12];
    public static final byte[] platformData = new byte[NUM_STATIONS * 3 * 8];
    public static final byte[] lineData = new byte[NUM_LINES * 3];
    public static final byte[] tripData = new byte[NUM_TRIPS * 4];
    public static final byte[] connectionData = new byte[NUM_CONNECTIONS * 12];
    // Each transfer record is 5 bytes:
    // Field 0: DEP_STATION_ID (U16)
    // Field 1: ARR_STATION_ID (U16)
    // Field 2: TRANSFER_MINUTES (U8)
    public static final byte[] transferData = new byte[NUM_TRANSFERS * 5];
    public static final String[] stringTable = new String[STRING_TABLE_SIZE];

    static {
        generateStringTable();
        generateStations();
        generatePlatforms();
        generateLines();
        generateTrips();
        generateConnections();
        generateTransfers();
    }

    private static void generateStringTable() {
        for (int i = 0; i < STRING_TABLE_SIZE; i++) {
            stringTable[i] = "Station " + (i + 1);
        }
    }

    private static void generateStations() {
        // Each station record is 12 bytes.
        for (int i = 0; i < NUM_STATIONS; i++) {
            int baseIndex = i * 12;
            putShort(stationData, baseIndex, (short) i); // Station ID
            putShort(stationData, baseIndex + 2, (short) RANDOM.nextInt(STRING_TABLE_SIZE));
            putInt(stationData, baseIndex + 4, RANDOM.nextInt());
            putInt(stationData, baseIndex + 8, RANDOM.nextInt());
        }
    }

    private static void generatePlatforms() {
        // Each station gets between 1 and 3 platforms.
        for (int i = 0; i < NUM_STATIONS; i++) {
            int platformsCount = RANDOM.nextInt(3) + 1; // 1 to 3 platforms
            for (int j = 0; j < platformsCount; j++) {
                int baseIndex = (i * 3 + j) * 8;
                putShort(platformData, baseIndex, (short) (i * 3 + j));       // Platform ID
                putShort(platformData, baseIndex + 2, (short) i);               // Associated station ID
                putShort(platformData, baseIndex + 4, (short) RANDOM.nextInt(STRING_TABLE_SIZE));
                putShort(platformData, baseIndex + 6, (short) RANDOM.nextInt(NUM_LINES));
            }
        }
    }

    private static void generateLines() {
        // Each line record is 3 bytes: U16 for name index and U8 for vehicle type.
        for (int i = 0; i < NUM_LINES; i++) {
            int baseIndex = i * 3;
            putShort(lineData, baseIndex, (short) i);
            lineData[baseIndex + 2] = (byte) RANDOM.nextInt(7);
        }
    }

    private static void generateTrips() {
        // Each trip record is 4 bytes (two U16 fields).
        for (int i = 0; i < NUM_TRIPS; i++) {
            int baseIndex = i * 4;
            putShort(tripData, baseIndex, (short) i);
            putShort(tripData, baseIndex + 2, (short) RANDOM.nextInt(NUM_STATIONS));
        }
    }

    private static void generateConnections() {
        // Each connection record is 12 bytes:
        // U16 for DEP_STOP_ID, U16 for DEP_MINUTES, U16 for ARR_STOP_ID, U16 for ARR_MINUTES,
        // and 4 bytes for TRIP_POS_ID.
        for (int i = 0; i < NUM_CONNECTIONS; i++) {
            int baseIndex = i * 12;
            putShort(connectionData, baseIndex, (short) RANDOM.nextInt(NUM_STATIONS));
            putShort(connectionData, baseIndex + 2, (short) RANDOM.nextInt(1440));
            putShort(connectionData, baseIndex + 4, (short) RANDOM.nextInt(NUM_STATIONS));
            putShort(connectionData, baseIndex + 6, (short) RANDOM.nextInt(1440));
            putInt(connectionData, baseIndex + 8, RANDOM.nextInt());
        }
    }

    private static void generateTransfers() {
        // Each transfer record is 5 bytes:
        // U16 for DEP_STATION_ID, U16 for ARR_STATION_ID, U8 for TRANSFER_MINUTES.
        // We generate station IDs in the range [0, NUM_STATIONS) so that valid IDs are 0 to 1999.
        for (int i = 0; i < NUM_TRANSFERS; i++) {
            int baseIndex = i * 5;
            putShort(transferData, baseIndex, (short) RANDOM.nextInt(NUM_STATIONS));
            putShort(transferData, baseIndex + 2, (short) RANDOM.nextInt(NUM_STATIONS));
            transferData[baseIndex + 4] = (byte) (RANDOM.nextInt(15) + 1);
        }
        // Force at least one record to have ARR_STATION_ID equal to NUM_STATIONS - 1 (i.e. 1999)
        // so that the maximum arrival station ID is 1999.
        putShort(transferData, 2, (short) (NUM_STATIONS - 1));

        // Sort transferData records by ARR_STATION_ID in big‑endian order.
        Integer[] indices = new Integer[NUM_TRANSFERS];
        for (int i = 0; i < NUM_TRANSFERS; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (i1, i2) -> {
            int off1 = i1 * 5;
            int off2 = i2 * 5;
            int arr1 = ((transferData[off1 + 2] & 0xFF) << 8) | (transferData[off1 + 3] & 0xFF);
            int arr2 = ((transferData[off2 + 2] & 0xFF) << 8) | (transferData[off2 + 3] & 0xFF);
            return Integer.compare(arr1, arr2);
        });
        byte[] sorted = new byte[transferData.length];
        for (int i = 0; i < NUM_TRANSFERS; i++) {
            System.arraycopy(transferData, indices[i] * 5, sorted, i * 5, 5);
        }
        System.arraycopy(sorted, 0, transferData, 0, transferData.length);
    }

    // Helper method: write a short (2 bytes) in big‑endian order.
    private static void putShort(byte[] array, int index, short value) {
        array[index] = (byte) ((value >> 8) & 0xFF);
        array[index + 1] = (byte) (value & 0xFF);
    }

    // Helper method: write an int (4 bytes) in big‑endian order.
    private static void putInt(byte[] array, int index, int value) {
        array[index] = (byte) ((value >> 24) & 0xFF);
        array[index + 1] = (byte) ((value >> 16) & 0xFF);
        array[index + 2] = (byte) ((value >> 8) & 0xFF);
        array[index + 3] = (byte) (value & 0xFF);
    }
}



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BufferedTransfersTest {

    private static BufferedTransfers transfers;

    @BeforeAll
    static void setup() {
        try {
            System.out.println("Initializing FlattenedRailwayData...");
            new FlattenedRailwayData();
            if (FlattenedRailwayData.transferData == null || FlattenedRailwayData.transferData.length == 0) {
                throw new IllegalStateException("FlattenedRailwayData.transferData is not initialized correctly.");
            }
            System.out.println("Wrapping transfer data in ByteBuffer...");
            ByteBuffer buffer = ByteBuffer.wrap(FlattenedRailwayData.transferData);
            transfers = new BufferedTransfers(buffer);
            System.out.println("Initialization complete.");
        } catch (Exception e) {
            System.err.println("Error during FlattenedRailwayData initialization: " + e.getMessage());
            e.printStackTrace();
            fail("FlattenedRailwayData failed to initialize correctly.");
        }
    }

    @Test
    void depStationIdWorksForValidIndexes() {
        System.out.println("Running depStationIdWorksForValidIndexes test...");
        int validId = 0;
        // Use 5 instead of 6 as the record size.
        assertTrue(validId * 5 + 1 < FlattenedRailwayData.transferData.length);
        int expected = ((FlattenedRailwayData.transferData[0] & 0xFF) << 8) | (FlattenedRailwayData.transferData[1] & 0xFF);
        assertEquals(expected, transfers.depStationId(validId));
    }

    @Test
    void depStationIdThrowsForInvalidIndexes() {
        System.out.println("Running depStationIdThrowsForInvalidIndexes test...");
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(FlattenedRailwayData.NUM_TRANSFERS));
    }

    @Test
    void minutesWorksForValidIndexes() {
        System.out.println("Running minutesWorksForValidIndexes test...");
        int validId = 0;
        assertTrue(validId * 6 + 4 < FlattenedRailwayData.transferData.length);
        assertEquals(FlattenedRailwayData.transferData[validId * 6 + 4] & 0xFF, transfers.minutes(validId));
    }

    @Test
    void minutesThrowsForInvalidIndexes() {
        System.out.println("Running minutesThrowsForInvalidIndexes test...");
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(FlattenedRailwayData.NUM_TRANSFERS));
    }

    @Test
    void arrivingAtWorksForValidStations() {
        System.out.println("Running arrivingAtWorksForValidStations test...");
        int validStationId = (FlattenedRailwayData.transferData[2] & 0xFF) | ((FlattenedRailwayData.transferData[3] & 0xFF) << 8);
        assertDoesNotThrow(() -> transfers.arrivingAt(validStationId));
    }

    @Test
    void arrivingAtThrowsForInvalidStations() {
        System.out.println("Running arrivingAtThrowsForInvalidStations test...");
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(5000));
    }

    @Test
    void minutesBetweenWorksForValidStations() {
        System.out.println("Running minutesBetweenWorksForValidStations test...");
        // Extract fields using big-endian interpretation.
        int depStationId = ((FlattenedRailwayData.transferData[0] & 0xFF) << 8) | (FlattenedRailwayData.transferData[1] & 0xFF);
        int arrStationId = ((FlattenedRailwayData.transferData[2] & 0xFF) << 8) | (FlattenedRailwayData.transferData[3] & 0xFF);
        int minutes = FlattenedRailwayData.transferData[4] & 0xFF;
        assertEquals(minutes, transfers.minutesBetween(depStationId, arrStationId));
    }

    @Test
    void minutesBetweenThrowsForNoMatchingTransfer() {
        System.out.println("Running minutesBetweenThrowsForNoMatchingTransfer test...");
        // Assuming transfers contains a transfer to station 300 only from departure station 100
        // and that station 300 is within the valid range.
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(123, 300));
    }

    @Test
    void sizeReturnsCorrectValue() {
        System.out.println("Running sizeReturnsCorrectValue test...");
        assertEquals(FlattenedRailwayData.NUM_TRANSFERS, transfers.size());
    }
}
class BufferedTransfersTestAll {

    /**
     * Creates a ByteBuffer with 3 transfer records (each 5 bytes, big-endian):
     * Record 0: DEP = 100, ARR = 200, MIN = 5
     * Record 1: DEP = 150, ARR = 200, MIN = 10
     * Record 2: DEP = 100, ARR = 300, MIN = 7
     */
    private ByteBuffer createCustomTransfersBuffer() {
        byte[] data = new byte[15];
        // Record 0:
        data[0] = (byte) ((100 >> 8) & 0xFF);
        data[1] = (byte) (100 & 0xFF);
        data[2] = (byte) ((200 >> 8) & 0xFF);
        data[3] = (byte) (200 & 0xFF);
        data[4] = (byte) 5;
        // Record 1:
        data[5] = (byte) ((150 >> 8) & 0xFF);
        data[6] = (byte) (150 & 0xFF);
        data[7] = (byte) ((200 >> 8) & 0xFF);
        data[8] = (byte) (200 & 0xFF);
        data[9] = (byte) 10;
        // Record 2:
        data[10] = (byte) ((100 >> 8) & 0xFF);
        data[11] = (byte) (100 & 0xFF);
        data[12] = (byte) ((300 >> 8) & 0xFF);
        data[13] = (byte) (300 & 0xFF);
        data[14] = (byte) 7;
        return ByteBuffer.wrap(data);
    }

    @Test
    void testSize() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        assertEquals(3, transfers.size(), "Size should be 3");
    }

    @Test
    void testDepStationIdAndMinutesWorks() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        // Because the records are sorted by ARR, the order remains:
        // Record 0: DEP=100, MIN=5; Record 1: DEP=150, MIN=10; Record 2: DEP=100, MIN=7.
        assertEquals(100, transfers.depStationId(0), "depStationId at index 0 should be 100");
        assertEquals(5, transfers.minutes(0), "minutes at index 0 should be 5");

        assertEquals(150, transfers.depStationId(1), "depStationId at index 1 should be 150");
        assertEquals(10, transfers.minutes(1), "minutes at index 1 should be 10");

        assertEquals(100, transfers.depStationId(2), "depStationId at index 2 should be 100");
        assertEquals(7, transfers.minutes(2), "minutes at index 2 should be 7");
    }

    @Test
    void testDepStationIdThrowsForInvalidIndexes() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1), "depStationId(-1) should throw");
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(3), "depStationId(3) should throw (size is 3)");
    }

    @Test
    void testMinutesThrowsForInvalidIndexes() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1), "minutes(-1) should throw");
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(3), "minutes(3) should throw (size is 3)");
    }

    @Test
    void testArrivingAtWorksForValidStations() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        // In our custom data, the ARR values are 200 and 300.
        // After sorting, records with ARR=200 occupy indices 0 and 1,
        // and record with ARR=300 is at index 2.
        // The lookup table is built with length = max(arrival) + 1 = 300 + 1 = 301.
        // For station 200, arrivingAt(200) should return a packed range covering indices [0, 2).
        int packedRange200 = transfers.arrivingAt(200);
        int start200 = PackedRange.startInclusive(packedRange200);
        int end200 = PackedRange.endExclusive(packedRange200);
        assertEquals(0, start200, "For station 200, start index should be 0");
        assertEquals(2, end200, "For station 200, end index should be 2");

        // For station 300, arrivingAt(300) should cover [2, 3).
        int packedRange300 = transfers.arrivingAt(300);
        int start300 = PackedRange.startInclusive(packedRange300);
        int end300 = PackedRange.endExclusive(packedRange300);
        assertEquals(2, start300, "For station 300, start index should be 2");
        assertEquals(3, end300, "For station 300, end index should be 3");
    }

    @Test
    void testArrivingAtThrowsForInvalidStations() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        // The lookup table length is 301, so valid station IDs are 0 to 300.
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(-1), "arrivingAt(-1) should throw");
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(400), "arrivingAt(400) should throw");
    }

    @Test
    void testMinutesBetweenWorksForValidTransfers() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        // There are two transfers arriving at station 200:
        // Record 0: from DEP 100, minutes = 5; Record 1: from DEP 150, minutes = 10.
        // For example, minutesBetween(100, 200) should return 5.
        assertEquals(5, transfers.minutesBetween(100, 200), "minutesBetween(100,200) should be 5");
        // And minutesBetween(150, 200) should return 10.
        assertEquals(10, transfers.minutesBetween(150, 200), "minutesBetween(150,200) should be 10");
        // For station 300, there is only one transfer (from DEP 100, minutes = 7).
        assertEquals(7, transfers.minutesBetween(100, 300), "minutesBetween(100,300) should be 7");
    }

    @Test
    void testMinutesBetweenThrowsNoSuchElement() {
        ByteBuffer buffer = createCustomTransfersBuffer();
        BufferedTransfers transfers = new BufferedTransfers(buffer);
        // For station 300, the only transfer is from DEP 100.
        // Therefore, asking for minutesBetween(150, 300) should throw NoSuchElementException.
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(150, 300), "minutesBetween(150,300) should throw NoSuchElementException");
    }
}
class RandomBufferedTransfersTest {

    private static final int NUM_TRANSFERS = 1000; // number of random transfers
    private static final int MAX_STATION = 2000;     // valid station IDs: 0..1999

    /**
     * Creates a ByteBuffer with 'numTransfers' random transfer records.
     * Each record is 5 bytes long, with:
     *   - DEP_STATION_ID (U16, big-endian)
     *   - ARR_STATION_ID (U16, big-endian)
     *   - TRANSFER_MINUTES (U8)
     * The records are sorted by ARR_STATION_ID (as expected by BufferedTransfers).
     */
    private ByteBuffer createRandomTransfersBuffer(int numTransfers, int maxStation) {
        Random rand = new Random();
        // Create an array to hold records as int[3]: {dep, arr, minutes}
        int[][] records = new int[numTransfers][3];
        for (int i = 0; i < numTransfers; i++){
            int dep = rand.nextInt(maxStation);
            int arr = rand.nextInt(maxStation);
            int minutes = rand.nextInt(60) + 1; // minutes between 1 and 60
            records[i][0] = dep;
            records[i][1] = arr;
            records[i][2] = minutes;
        }
        // Sort records by ARR_STATION_ID (the second element)
        Arrays.sort(records, Comparator.comparingInt(r -> r[1]));
        // Write the sorted records into a byte array in big-endian order.
        byte[] data = new byte[numTransfers * 5];
        for (int i = 0; i < numTransfers; i++){
            int dep = records[i][0];
            int arr = records[i][1];
            int minutes = records[i][2];
            int pos = i * 5;
            data[pos]     = (byte) ((dep >> 8) & 0xFF);
            data[pos + 1] = (byte) (dep & 0xFF);
            data[pos + 2] = (byte) ((arr >> 8) & 0xFF);
            data[pos + 3] = (byte) (arr & 0xFF);
            data[pos + 4] = (byte) (minutes & 0xFF);
        }
        return ByteBuffer.wrap(data);
    }

    /**
     * Helper method to reconstruct a list of records from the ByteBuffer.
     * Each record is an int array of length 3: {dep, arr, minutes}
     */
    private List<int[]> extractRecords(ByteBuffer buffer, int numTransfers) {
        byte[] data = buffer.array();
        List<int[]> records = new ArrayList<>();
        for (int i = 0; i < numTransfers; i++){
            int pos = i * 5;
            int dep = ((data[pos] & 0xFF) << 8) | (data[pos+1] & 0xFF);
            int arr = ((data[pos+2] & 0xFF) << 8) | (data[pos+3] & 0xFF);
            int mins = data[pos+4] & 0xFF;
            records.add(new int[]{dep, arr, mins});
        }
        return records;
    }

    @Test
    public void testRandomBufferedTransfersMethods() {
        // Create a random transfers buffer.
        ByteBuffer buffer = createRandomTransfersBuffer(NUM_TRANSFERS, MAX_STATION);
        // Build a BufferedTransfers instance.
        BufferedTransfers transfers = new BufferedTransfers(buffer);

        // Check size()
        assertEquals(NUM_TRANSFERS, transfers.size(), "Size mismatch.");

        // Extract records from the same buffer.
        List<int[]> records = extractRecords(buffer, NUM_TRANSFERS);

        // Verify that each record's depStationId and minutes are as expected.
        for (int i = 0; i < NUM_TRANSFERS; i++){
            int expectedDep = records.get(i)[0];
            int expectedMinutes = records.get(i)[2];
            assertEquals(expectedDep, transfers.depStationId(i), "depStationId mismatch at index " + i);
            assertEquals(expectedMinutes, transfers.minutes(i), "minutes mismatch at index " + i);
        }

        // Build a naive lookup map for minutesBetween:
        // Map from arrival station -> list of records with that arrival, in sorted order.
        Map<Integer, List<int[]>> lookup = new HashMap<>();
        int maxArrival = -1;
        for (int[] rec : records) {
            int arr = rec[1];
            maxArrival = Math.max(maxArrival, arr);
            lookup.computeIfAbsent(arr, k -> new ArrayList<>()).add(rec);
        }

        // For each random query, decide which exception is expected.
        Random rand = new Random();
        for (int i = 0; i < 100; i++){
            int depQuery = rand.nextInt(MAX_STATION);
            int arrQuery = rand.nextInt(MAX_STATION);
            if (arrQuery > maxArrival) {
                // arrQuery is out of the valid range, so we expect IndexOutOfBoundsException.
                assertThrows(IndexOutOfBoundsException.class, () ->
                                transfers.minutesBetween(depQuery, arrQuery),
                        "Expected IndexOutOfBoundsException for arrQuery = " + arrQuery);
            } else {
                // arrQuery is within the valid range.
                List<int[]> list = lookup.get(arrQuery);
                Optional<int[]> found = (list == null) ? Optional.empty() : list.stream()
                        .filter(r -> r[0] == depQuery)
                        .findFirst();
                if (found.isPresent()){
                    int expectedMinutes = found.get()[2];
                    int actualMinutes = transfers.minutesBetween(depQuery, arrQuery);
                    assertEquals(expectedMinutes, actualMinutes, "Mismatch for minutesBetween(" + depQuery + ", " + arrQuery + ")");
                } else {
                    // No record exists with this departure for the given arrival.
                    assertThrows(NoSuchElementException.class, () ->
                                    transfers.minutesBetween(depQuery, arrQuery),
                            "Expected NoSuchElementException for query (dep=" + depQuery + ", arr=" + arrQuery + ")");
                }
            }
        }
    }
}