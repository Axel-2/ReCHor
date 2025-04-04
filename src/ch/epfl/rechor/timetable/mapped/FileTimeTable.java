package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Représente un horaire de transport public dont les données (aplaties) sont stockées dans des fichiers
 *  @author Yoann Salamin (390522)
 *  @author Axel Verga (398787)
 */
public record FileTimeTable(Path directory,
                            List<String> stringTable,
                            Stations stations,
                            StationAliases stationAliases,
                            Platforms platforms,
                            Routes routes,
                            Transfers transfers)
        implements TimeTable {


    /**
     * Charge et mappe en mémoire un fichier binaire en lecture seule.
     *
     * @param directory le répertoire contenant le fichier
     * @param fileName le nom du fichier à charger
     * @return un MappedByteBuffer correspondant au contenu du fichier
     * @throws IOException en cas d'erreur d'accès au fichier
     */
    private static MappedByteBuffer loadMappedBuffer(Path directory, String fileName) throws IOException {
        try (FileChannel channel = FileChannel.open(directory.resolve(fileName))) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }


    /**
     * Méthode qui retourne une nouvelle instance de FileTimeTable dont les données aplaties
     * ont été obtenues à partir des fichiers se trouvant dans le dossier dont le chemin d'accès est donné
     * @param directory chemin d'accès
     * @return une instance de FileTimeTable
     * @throws IOException si le chemin d'accès est invalide
     */
    public static TimeTable in(Path directory) throws IOException {

        // STRINGS : 1) Path / 2) Lecture / 3) Immuabilité
        Path stringsFilePath = directory.resolve("strings.txt");
        List<String> txtFileContent = Files.readAllLines(stringsFilePath, StandardCharsets.ISO_8859_1);
        List<String> stringTable = List.copyOf(txtFileContent);

        // --- Lecture des fichiers -----
        MappedByteBuffer stationsBuffer = loadMappedBuffer(directory, "stations.bin");
        BufferedStations stations = new BufferedStations(stringTable, stationsBuffer);

        MappedByteBuffer stationsAliasesBuffer = loadMappedBuffer(directory, "station-aliases.bin");
        BufferedStationAliases stationAliases = new BufferedStationAliases(stringTable, stationsAliasesBuffer);

        MappedByteBuffer platformsBuffer = loadMappedBuffer(directory, "platforms.bin");
        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, platformsBuffer);

        MappedByteBuffer routesBuffer = loadMappedBuffer(directory, "routes.bin");
        BufferedRoutes routes = new BufferedRoutes(stringTable, routesBuffer);

        MappedByteBuffer transfersBuffer = loadMappedBuffer(directory, "transfers.bin");
        BufferedTransfers transfers = new BufferedTransfers(transfersBuffer);

        return new FileTimeTable(directory, stringTable, stations, stationAliases, platforms, routes, transfers);
    }

    /**
     * Fonction qui retourne les gares indexées de l'horaire
     * @return Les gares indexées de l'horaire
     */
    @Override
    public Stations stations() {
        return stations;
    }

    /**
     * Fonction qui retourne les noms alternatifs indexés des gares de l'horaire
     * @return Les noms alternatifs indexés des gardes de l'horaire
     */
    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    /**
     * Fonction qui retourne les voies/quais indexées de l'horaire
     *
     * @return les voies/quais indexées de l'horaire
     */
    @Override
    public Platforms platforms() {
        return platforms;
    }

    /**
     * Fonction qui retourne les lignes indexées de l'horaire
     *
     * @return les lignes indexées de l'horaire,
     */
    @Override
    public Routes routes() {
        return routes;
    }

    /**
     * Fonction qui retourne les changements indexés de l'horaire
     *
     * @return les changements indexés de l'horaire
     */
    @Override
    public Transfers transfers() {
        return transfers;
    }

    /**
     * Fonction qui retourne les courses indexées de l'horaire actives le jour donné
     *
     * @param date une date qui représente un jour entier
     * @return les courses indexées de l'horaire actives le jour donné
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        // Chemin du dossier contenant le "trips.bin" du jour actuel
        Path tripsDir = directory.resolve(date.toString());
        try {
            MappedByteBuffer tripsBuffer = loadMappedBuffer(tripsDir, "trips.bin");
            return new BufferedTrips(stringTable, tripsBuffer);
        } catch (IOException e) {
            // Dans l'énoncé, on nous demande de gérer les exceptions comme ça
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Fonction qui retourne les liaisons indexées de l'horaire actives le jour donné.
     *
     * @param date une date qui représente un jour entier
     * @return les liaisons indexées de l'horaire actives le jour donné
     */
    @Override
    public Connections connectionsFor(LocalDate date) {

        // Répertoire du jour actuel
        Path dayDirectory = directory.resolve(date.toString());
        try {
            MappedByteBuffer connectionsBuffers = loadMappedBuffer(dayDirectory, "connections.bin");
            MappedByteBuffer connectionsSuccBuffers = loadMappedBuffer(dayDirectory, "connections-succ.bin");
            return new BufferedConnections(connectionsBuffers, connectionsSuccBuffers);
        } catch (IOException e) {
            // Dans l'énoncé, on nous demande de gérer les exceptions comme ça
            throw new UncheckedIOException(e);
        }
    }
}
