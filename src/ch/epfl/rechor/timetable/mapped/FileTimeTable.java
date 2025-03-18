package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Représente un horaire de transport public dont les données (aplaties) sont stockées dans des fichiers
 *  * @author Yoann Salamin (390522)
 *  * @author Axel Verga (398787)
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
     * @return
     * @throws IOException
     */
    public TimeTable in(Path directory) throws IOException {
        //TODO
        Path strings = directory.resolve("strings.txt");
        List<String> txt = Files.readAllLines(directory, StandardCharsets.ISO_8859_1);
        List<String> immutableTxt = List.copyOf(txt);
        return null;
    }

    /**
     * Fonction qui retourne les gares indexées de l'horaire
     *
     * @return Les gares indexées de l'horaire
     */
    @Override
    public Stations stations() {
        return null;
    }

    /**
     * Fonction qui retourne les noms alternatifs indexés des gares de l'horaire
     *
     * @return Les noms alternatifs indexés des gardes de l'horaire
     */
    @Override
    public StationAliases stationAliases() {
        return null;
    }

    /**
     * Fonction qui retourne les voies/quais indexées de l'horaire
     *
     * @return les voies/quais indexées de l'horaire
     */
    @Override
    public Platforms platforms() {
        return null;
    }

    /**
     * Fonction qui retourne les lignes indexées de l'horaire
     *
     * @return les lignes indexées de l'horaire,
     */
    @Override
    public Routes routes() {
        return null;
    }

    /**
     * Fonction qui retourne les changements indexés de l'horaire
     *
     * @return les changements indexés de l'horaire
     */
    @Override
    public Transfers transfers() {
        return null;
    }

    /**
     * Fonction qui retourne les courses indexées de l'horaire actives le jour donné
     *
     * @param date une date qui représente un jour entier
     * @return les courses indexées de l'horaire actives le jour donné
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        return null;
    }

    /**
     * Fonction qui retourne les liaisons indexées de l'horaire actives le jour donné.
     *
     * @param date une date qui représente un jour entier
     * @return les liaisons indexées de l'horaire actives le jour donné
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        return null;
    }
}
