package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.TimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyFileTimeTableTest2 {

    @TempDir
    Path tempDir;

    private Path testDirectory;
    private TimeTable timeTable;

    @BeforeEach
    void setUp() throws IOException {
        // Créer une structure de répertoire de test ressemblant à timetable
        testDirectory = tempDir.resolve("timetable");
        Files.createDirectories(testDirectory);

        // Créer un fichier strings.txt avec quelques chaînes de caractères
        Path stringsFile = testDirectory.resolve("strings.txt");
        List<String> strings = List.of(
                "Lausanne",
                "Genève",
                "Berne",
                "IC1",
                "S1",
                "Voie 1",
                "Quai 2"
        );
        Files.write(stringsFile, strings, StandardCharsets.ISO_8859_1);

        // Créer les autres fichiers nécessaires (vides pour ce test)
        Files.createFile(testDirectory.resolve("stations.bin"));
        Files.createFile(testDirectory.resolve("station_aliases.bin"));
        Files.createFile(testDirectory.resolve("platforms.bin"));
        Files.createFile(testDirectory.resolve("routes.bin"));
        Files.createFile(testDirectory.resolve("transfers.bin"));

        // Créer les répertoires de dates
        Path datesDir = testDirectory.resolve("dates");
        Files.createDirectories(datesDir);

        // Créer un répertoire pour aujourd'hui
        LocalDate today = LocalDate.now();
        String dateStr = String.format("%04d/%02d/%02d", today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        Path todayDir = datesDir.resolve(dateStr);
        Files.createDirectories(todayDir);

        // Créer les fichiers nécessaires pour les connexions et voyages
        Files.createFile(todayDir.resolve("connections.bin"));
        Files.createFile(todayDir.resolve("trips.bin"));

        // Initialiser TimeTable
        timeTable = FileTimeTable.in(testDirectory);
    }

    @Test
    void testInMethod() throws IOException {
        // Vérifier que la méthode in crée correctement une instance
        TimeTable tt = FileTimeTable.in(testDirectory);
        assertNotNull(tt);
        assertTrue(tt instanceof FileTimeTable);
    }

    @Test
    void testInMethodWithInvalidDirectory() {
        // Vérifier que la méthode in lance une IOException avec un répertoire inexistant
        Path invalidDir = tempDir.resolve("non_existent");
        assertThrows(IOException.class, () -> FileTimeTable.in(invalidDir));
    }

    @Test
    void testStations() {
        // Vérifier que la méthode stations retourne un objet non null
        assertNotNull(timeTable.stations());
    }

    @Test
    void testStationsAliases() {
        // Vérifier que la méthode stationAliases retourne un objet non null
        assertNotNull(timeTable.stationAliases());
    }

    @Test
    void testPlatforms() {
        // Vérifier que la méthode platforms retourne un objet non null
        assertNotNull(timeTable.platforms());
    }

    @Test
    void testRoutes() {
        // Vérifier que la méthode routes retourne un objet non null
        assertNotNull(timeTable.routes());
    }

    @Test
    void testTransfers() {
        // Vérifier que la méthode transfers retourne un objet non null
        assertNotNull(timeTable.transfers());
    }

    @Test
    void testConnectionsFor() {
        // Vérifier que la méthode connectionsFor retourne un objet non null
        LocalDate date = LocalDate.now();
        assertNotNull(timeTable.connectionsFor(date));
    }

    @Test
    void testConnectionsForWithInvalidDate() {
        // Vérifier le comportement avec une date pour laquelle aucun fichier n'existe
        LocalDate futureDate = LocalDate.now().plusYears(100);
        assertThrows(UncheckedIOException.class, () -> timeTable.connectionsFor(futureDate));
    }

    @Test
    void testTripsFor() {
        // Vérifier que la méthode tripsFor retourne un objet non null
        LocalDate date = LocalDate.now();
        assertNotNull(timeTable.tripsFor(date));
    }



}
