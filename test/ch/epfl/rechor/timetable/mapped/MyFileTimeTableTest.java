package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Trips;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MyFileTimeTableTest {

    private FileTimeTable fileTimeTable;
    private final Path testDirectory = Path.of("timetable"); // Adapte ce chemin selon ton projet
    private final LocalDate testDate = LocalDate.of(2025, 3, 17); // Une date correspondant aux fichiers test

    @BeforeEach
    void setUp() throws IOException {
        fileTimeTable = (FileTimeTable) FileTimeTable.in(testDirectory);
    }

    @Test
    void testStationsNotNull() {
        assertNotNull(fileTimeTable.stations(), "Les stations ne doivent pas être nulles");
    }

    @Test
    void testStationAliasesNotNull() {
        assertNotNull(fileTimeTable.stationAliases(), "Les alias des stations ne doivent pas être nulles");
    }

    @Test
    void testPlatformsNotNull() {
        assertNotNull(fileTimeTable.platforms(), "Les quais ne doivent pas être nulls");
    }

    @Test
    void testRoutesNotNull() {
        assertNotNull(fileTimeTable.routes(), "Les routes ne doivent pas être nulles");
    }

    @Test
    void testTransfersNotNull() {
        assertNotNull(fileTimeTable.transfers(), "Les transferts ne doivent pas être nulls");
    }

    @Test
    void testTripsForValidDate() {
        Trips trips = fileTimeTable.tripsFor(testDate);
        assertNotNull(trips, "Les courses pour la date donnée ne doivent pas être nulles");
    }

    @Test
    void testConnectionsForValidDate() {
        Connections connections = fileTimeTable.connectionsFor(testDate);
        assertNotNull(connections, "Les liaisons pour la date donnée ne doivent pas être nulles");
    }

    @Test
    void testInvalidDirectoryThrowsException() {
        Path invalidPath = Path.of("invalid/directory");
        assertThrows(IOException.class, () -> FileTimeTable.in(invalidPath));
    }


}