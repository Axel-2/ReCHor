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


        // Création des autres variables à retourner
        // qui sont toutes nulles pour l'instant
        Stations stations;
        StationAliases stationAliases;
        Platforms platforms;
        Routes routes;
        Transfers transfers;

        // STATIONS
        try (FileChannel channel = FileChannel.open(directory.resolve("stations.bin"))) {
            MappedByteBuffer stationsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            stations = new BufferedStations(stringTable, stationsBuffer);
        }

        // STATIONS_ALIASES
        try (FileChannel channel = FileChannel.open(directory.resolve("station-aliases.bin"))) {
            MappedByteBuffer stationsAliasesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            stationAliases = new BufferedStationAliases(stringTable, stationsAliasesBuffer);
        }

        // PLATFORMS
        try (FileChannel channel = FileChannel.open(directory.resolve("platforms.bin"))) {
            MappedByteBuffer platformsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            platforms = new BufferedPlatforms(stringTable, platformsBuffer);
        }

        // ROUTES
        try (FileChannel channel = FileChannel.open(directory.resolve("routes.bin"))) {
            MappedByteBuffer routesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            routes = new BufferedRoutes(stringTable, routesBuffer);
        }

        // TRANSFERS
        try (FileChannel channel = FileChannel.open(directory.resolve("transfers.bin"))) {
            MappedByteBuffer transfersBuffers = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            transfers = new BufferedTransfers(transfersBuffers);
        }


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

        // Path du dossier contenant le "trips.bin" du jour actuel
        Path tripsFilePath = directory.resolve(date.toString()).resolve("trips.bin");

        // Même procédé que pour tous, avec le path ajusté
        try(FileChannel channel = FileChannel.open(tripsFilePath)) {
            MappedByteBuffer tripsBuffers = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            Trips trips = new BufferedTrips(stringTable, tripsBuffers);
            return trips;

        // Dans l'énoncé, on nous demande de gérer les exceptions comme ça
        } catch (IOException e) {
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

        // Path du dossier contenant le "trips.bin" du jour actuel
        Path connectionsFilePath = directory.resolve(date.toString()).resolve("connections.bin");
        Path connectionsSuccFilePath = directory.resolve(date.toString()).resolve("connections-succ.bin");

        // Même procédé que pour tous, avec le path ajusté
        try(FileChannel connectionChannel = FileChannel.open(connectionsFilePath);
            FileChannel connectionSuccChannel = FileChannel.open(connectionsSuccFilePath)
        ) {

            MappedByteBuffer connectionsBuffers = connectionChannel.map(FileChannel.MapMode.READ_ONLY,
                    0, connectionChannel.size());
            MappedByteBuffer connectionsSuccBuffers = connectionSuccChannel.map(FileChannel.MapMode.READ_ONLY,
                    0, connectionSuccChannel.size());

            Connections connections = new BufferedConnections(connectionsBuffers, connectionsSuccBuffers);
            return connections;
        }

        // Dans l'énoncé, on nous demande de gérer les exceptions comme ça
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
