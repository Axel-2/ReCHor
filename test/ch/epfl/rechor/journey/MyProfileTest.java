package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class MyProfileTest {

    private FileTimeTable fileTimeTable;
    private final Path testDirectory = Path.of("timetable");
    private final LocalDate testDate = LocalDate.of(2025, 3, 17);

    @BeforeEach
    void setUp() throws IOException {
        // Crée le TimeTable à partir des fichiers contenus dans le dossier "timetable"
        fileTimeTable = (FileTimeTable) FileTimeTable.in(testDirectory);
    }

    @Test
    void testProfileAttributes() {
        // On choisit arbitrairement la gare de destination (arrStationId) = 10
        int destStationId = 10;
        Profile profile = new Profile.Builder(fileTimeTable, testDate, destStationId).build();

        // Vérifie que les attributs du profil correspondent aux valeurs attendues
        assertEquals(fileTimeTable, profile.timeTable(), "Le TimeTable doit correspondre");
        assertEquals(testDate, profile.date(), "La date du profil doit être " + testDate);
        assertEquals(destStationId, profile.arrStationId(), "La gare de destination doit être " + destStationId);
    }

    @Test
    void testDefaultParetoFronts() {
        // Création d'un profil sans affecter de front spécifique pour aucune gare
        int destStationId = 10;
        Profile profile = new Profile.Builder(fileTimeTable, testDate, destStationId).build();
        int nbStations = fileTimeTable.stations().size();

        // Pour chaque station, la méthode forStation() doit renvoyer une frontière vide (typiquement ParetoFront.EMPTY)
        for (int stationId = 0; stationId < nbStations; stationId++) {
            ParetoFront pf = profile.forStation(stationId);
            assertNotNull(pf, "La frontière pour la gare " + stationId + " ne doit pas être nulle");
            assertEquals(ParetoFront.EMPTY.size(), pf.size(),
                    "La frontière pour la gare " + stationId + " doit être vide par défaut");
        }
    }

    @Test
    void testSetAndGetStationFront() {
        int destStationId = 10;
        Profile.Builder builder = new Profile.Builder(fileTimeTable, testDate, destStationId);

        // Choix d'une station pour laquelle on affecte un front non vide
        int stationId = 5;
        // Création d'un ParetoFront.Builder et ajout d'un critère de test (valeur empaquetée fictive)
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        long testCriterion = 0x123456789ABCDEFL;
        frontBuilder.add(testCriterion);

        // Affecte ce front à la station d'index 5
        builder.setForStation(stationId, frontBuilder);
        Profile profile = builder.build();

        // Récupère et vérifie la frontière pour la station 5
        ParetoFront pf = profile.forStation(stationId);
        assertNotNull(pf, "La frontière pour la gare " + stationId + " ne doit pas être nulle");
        assertEquals(1, pf.size(), "La frontière pour la gare " + stationId + " doit contenir 1 critère");

        // Récupération du critère via un forEach (la méthode get(...) peut varier selon votre implémentation)
        final long[] retrievedCriterion = new long[1];
        pf.forEach(value -> retrievedCriterion[0] = value);
        assertEquals(testCriterion, retrievedCriterion[0], "Le critère stocké ne correspond pas à celui attendu");
    }

    @Test
    void testInvalidStationIndex() {
        int destStationId = 10;
        Profile profile = new Profile.Builder(fileTimeTable, testDate, destStationId).build();
        int nbStations = fileTimeTable.stations().size();

        // Un index négatif doit déclencher une exception
        assertThrows(IndexOutOfBoundsException.class, () -> profile.forStation(-1),
                "Un index négatif doit lever une exception");
        // Un index égal ou supérieur au nombre total de gares doit également lever une exception
        assertThrows(IndexOutOfBoundsException.class, () -> profile.forStation(nbStations),
                "Un index supérieur ou égal au nombre de gares doit lever une exception");
    }

    @Test
    void testTripsAndConnections() {
        int destStationId = 10;
        Profile profile = new Profile.Builder(fileTimeTable, testDate, destStationId).build();

        // Vérifie que les méthodes trips() et connections() retournent des objets non nuls
        assertNotNull(profile.trips(), "La liste des courses ne doit pas être nulle");
        assertNotNull(profile.connections(), "La liste des liaisons ne doit pas être nulle");
    }



    @Test
    public void testStationsBuffer() {
        Stations stations = fileTimeTable.stations();
        int nbStations = stations.size();
        assertTrue(nbStations > 0, "Le nombre de gares doit être positif");
        // Vérifie que le nom de la première gare n'est pas vide
        String firstStationName = stations.name(0);
        assertNotNull(firstStationName, "Le nom de la première gare ne doit pas être null");
        assertFalse(firstStationName.isEmpty(), "Le nom de la première gare ne doit pas être vide");
        System.out.println("Première gare : " + firstStationName);
    }

    @Test
    public void testStationAliasesBuffer() {
        StationAliases aliases = fileTimeTable.stationAliases();
        // Si le fichier station-aliases est fourni et non vide
        if (aliases != null && aliases.size() > 0) {
            int nbAliases = aliases.size();
            assertTrue(nbAliases > 0, "Le nombre d'alias doit être positif");
            // Exemple : vérifie l'alias 0 et la station correspondante
            String alias0 = aliases.alias(0);
            String stationName0 = aliases.stationName(0);
            assertNotNull(alias0, "L'alias 0 ne doit pas être null");
            assertNotNull(stationName0, "Le nom de station associé à l'alias 0 ne doit pas être null");
            System.out.println("Alias 0 : " + alias0 + " pour la station " + stationName0);
        }
    }

    @Test
    public void testPlatformsBuffer() {
        Platforms platforms = fileTimeTable.platforms();
        if (platforms != null && platforms.size() > 0) {
            int nbPlatforms = platforms.size();
            assertTrue(nbPlatforms > 0, "Le nombre de plateformes doit être positif");
            String platformName = platforms.name(0);
            int stationId = platforms.stationId(0);
            assertNotNull(platformName, "Le nom de la plateforme ne doit pas être null");
            System.out.println("Première plateforme : " + platformName + " (rattachée à la gare " + stationId + ")");
        }
    }

    @Test
    public void testRoutesBuffer() {
        Routes routes = fileTimeTable.routes();
        int nbRoutes = routes.size();
        assertTrue(nbRoutes > 0, "Le nombre de lignes doit être positif");
        String routeName = routes.name(0);
        assertNotNull(routeName, "Le nom de la première ligne ne doit pas être null");
        assertFalse(routeName.isEmpty(), "Le nom de la première ligne ne doit pas être vide");
        System.out.println("Première ligne : " + routeName + ", véhicule : " + routes.vehicle(0));
    }

    @Test
    public void testTransfersBuffer() {
        Transfers transfers = fileTimeTable.transfers();
        if (transfers != null && transfers.size() > 0) {
            int nbTransfers = transfers.size();
            assertTrue(nbTransfers > 0, "Le nombre de changements doit être positif");
            int depStation = transfers.depStationId(0);
           // int arrStation = transfers.arrStationId(0);
            int duration = transfers.minutes(0);
            assertTrue(duration >= 0, "La durée du changement doit être positive ou nulle");
            System.out.println("Premier transfert : de la gare " + depStation +
                    " vers la gare " + ", durée : " + duration + " min");
        }
    }

    @Test
    public void testTripsBuffer() {
        Trips trips = fileTimeTable.tripsFor(testDate);
        int nbTrips = trips.size();
        assertTrue(nbTrips > 0, "Il doit y avoir des courses pour la date donnée");
        String destination0 = trips.destination(0);
        assertNotNull(destination0, "La destination de la première course ne doit pas être null");
        System.out.println("Première course, destination : " + destination0);
    }

    @Test
    public void testConnectionsBuffer() {
        Connections connections = fileTimeTable.connectionsFor(testDate);
        int nbConnections = connections.size();
        assertTrue(nbConnections > 0, "Il doit y avoir des liaisons pour la date donnée");
        // Vérifie quelques attributs de la première liaison
        int depStopId = connections.depStopId(0);
        int depMinutes = connections.depMins(0);
        int arrStopId = connections.arrStopId(0);
        int arrMinutes = connections.arrMins(0);
        assertTrue(depMinutes >= 0, "Les minutes de départ doivent être positives ou nulles");
        assertTrue(arrMinutes >= depMinutes, "Les minutes d'arrivée doivent être supérieures ou égales à celles de départ");
        System.out.println("Première liaison : depStop " + depStopId + " à " + depMinutes +
                " min, arrStop " + arrStopId + " à " + arrMinutes + " min");
    }


}
