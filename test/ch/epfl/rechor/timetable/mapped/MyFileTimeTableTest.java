package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
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

    // On utilise un dossier temporaire pour copier nos fichiers de test.
// Vous devez préparer un dossier de test (par exemple "testdata") contenant :
// • À la racine : strings.txt, stations.bin, station-aliases.bin, platforms.bin, routes.bin, transfers.bin
// • Dans un sous-dossier (par exemple "2025-03-17") : trips.bin, connections.bin, connections-succ.bin
// Ici, on simule cette préparation en copiant (si nécessaire) depuis un dossier de ressources.
    @TempDir
    Path tempDir;

    // Méthode utilitaire pour copier un fichier depuis les ressources vers le dossier temporaire.
    private void copyResource(String resourceName, Path destination) throws IOException {
        // Au lieu d'utiliser getResourceAsStream(...), on construit un chemin relatif
        // "timetable" se trouve au même niveau que "test"
        Path resourcePath = Path.of("timetable", resourceName);
        // Lecture du fichier en tant que flux d'entrée
        try (var is = Files.newInputStream(resourcePath)) {
            Files.copy(is, destination);
        }
    }

    // Prépare le dossier de test en copiant tous les fichiers.
    private Path prepareTestData() throws IOException {
        Path dataDir = tempDir.resolve("timetable");
        Files.createDirectory(dataDir);

        // Copie des fichiers binaires
        copyResource("strings.txt", dataDir.resolve("strings.txt"));
        copyResource("stations.bin", dataDir.resolve("stations.bin"));
        copyResource("station-aliases.bin", dataDir.resolve("station-aliases.bin"));
        copyResource("platforms.bin", dataDir.resolve("platforms.bin"));
        copyResource("routes.bin", dataDir.resolve("routes.bin"));
        copyResource("transfers.bin", dataDir.resolve("transfers.bin"));

        // On crée un sous-dossier "2025-03-17"
        Path dateDir = dataDir.resolve("2025-03-17");
        Files.createDirectory(dateDir);

        copyResource("2025-03-17/trips.bin", dateDir.resolve("trips.bin"));
        copyResource("2025-03-17/connections.bin", dateDir.resolve("connections.bin"));
        copyResource("2025-03-17/connections-succ.bin", dateDir.resolve("connections-succ.bin"));

        return dataDir;
    }


    @Test
    public void testLoadTimetable() throws IOException {
        // Préparation du dossier avec les fichiers réels
        Path dataDir = prepareTestData();

        // Chargement de l'horaire via FileTimeTable.in()
        TimeTable tt = FileTimeTable.in(dataDir);
        assertNotNull(tt, "L'objet TimeTable doit être non null");
        assertNotNull(tt.stations(), "Les stations doivent être chargées");
        assertNotNull(tt.stationAliases(), "Les alias de stations doivent être chargés");
        assertNotNull(tt.platforms(), "Les plateformes doivent être chargées");
        assertNotNull(tt.routes(), "Les routes doivent être chargées");
        assertNotNull(tt.transfers(), "Les transferts doivent être chargés");
    }

    @Test
    public void testTripsForAndConnectionsFor_ExistingDate() throws IOException {
        // Préparation du dossier de test
        Path dataDir = prepareTestData();
        TimeTable tt = FileTimeTable.in(dataDir);
        LocalDate date = LocalDate.of(2025, 3, 17);

        // Test des courses (trips)
        Trips trips = tt.tripsFor(date);
        assertNotNull(trips, "Les courses (trips) pour la date " + date + " doivent être non null");

        // Test des liaisons (connections)
        Connections connections = tt.connectionsFor(date);
        assertNotNull(connections, "Les liaisons (connections) pour la date " + date + " doivent être non null");
    }

    @Test
    public void testTripsFor_MissingDateThrowsUncheckedIOException() throws IOException {
        // Préparation du dossier de test sans créer de sous-dossier pour la date demandée.
        Path dataDir = prepareTestData();
        TimeTable tt = FileTimeTable.in(dataDir);
        LocalDate missingDate = LocalDate.of(1999, 1, 1);

        // Ici, l'absence du dossier "1999-01-01" doit entraîner une exception lors de l'appel à tripsFor.
        assertThrows(UncheckedIOException.class, () -> tt.tripsFor(missingDate),
                "L'appel à tripsFor pour une date manquante doit lever UncheckedIOException");
    }

    @Test
    public void testConnectionsFor_MissingDateThrowsUncheckedIOException() throws IOException {
        // Préparation du dossier de test sans le sous-dossier pour une date donnée.
        Path dataDir = prepareTestData();
        TimeTable tt = FileTimeTable.in(dataDir);
        LocalDate missingDate = LocalDate.of(1999, 1, 1);

        // Vérification que l'absence des fichiers connections.bin et connections-succ.bin entraîne bien une exception.
        assertThrows(UncheckedIOException.class, () -> tt.connectionsFor(missingDate),
                "L'appel à connectionsFor pour une date manquante doit lever UncheckedIOException");
    }


}