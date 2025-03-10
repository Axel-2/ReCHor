package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Interface qui représente un horaire de transport public
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface TimeTable {

    /**
     * Fonction qui retourne les gares indexées de l'horaire
     * @return Les gares indexées de l'horaire
     */
    Stations stations();

    /**
     * Fonction qui retourne les noms alternatifs indexés des gares de l'horaire
     * @return Les noms alternatifs indexés des gardes de l'horaire
     */
    StationAliases stationAliases();

    /**
     * Fonction qui retourne les voies/quais indexées de l'horaire
     * @return les voies/quais indexées de l'horaire
     */
    Platforms platforms();

    /**
     * Fonction qui retourne les lignes indexées de l'horaire
     * @return les lignes indexées de l'horaire,
     */
    Routes routes();

    /**
     * Fonction qui retourne les changements indexés de l'horaire
     * @return les changements indexés de l'horaire
     */
    Transfers transfers();

    /**
     * Fonction qui retourne les courses indexées de l'horaire actives le jour donné
     * @param date une date qui représente un jour entier
     * @return les courses indexées de l'horaire actives le jour donné
     */
    Trips tripsFor(LocalDate date);

    /**
     * Fonction qui retourne les liaisons indexées de l'horaire actives le jour donné.
     * @param date une date qui représente un jour entier
     * @return les liaisons indexées de l'horaire actives le jour donné
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Fonction qui retourne vrai si et seulement si l'index d'arrêt donné
     * est un index de gare (et pas un index de voie ou de quai),
     * @param stopId un index d'arrêt
     * @return  vrai si et seulement si l'index d'arrêt donné
     * est un index de gare (et pas un index de voie ou de quai)
     */
    default boolean isStationId(int stopId) {

        // Si l'index est inférieur au nombre de gares, c'est une gare.
        return stopId < stations().size();
    }

    /**
     * Fonction qui retourne vrai si et seulement si l'index d'arrêt donné
     * est un index de voie ou de quai (et pas un index de gare),
     * @param stopId un index d'arrêt
     * @return vrai si et seulement si l'index d'arrêt donné
     * est un index de voie ou de quai (et pas un index de gare)
     */
    default boolean isPlatformId(int stopId) {
        // Sinon, c'est une voie ou un quai.
        return stopId >= stations().size();
    }

    /**
     * Fonction qui retourne l'index de la gare de l'arrêt d'index donné
     * (qui peut être identique si l'arrêt en question est une gare),
     * @param stopId un index d'arrêt
     * @return l'index de la gare de l'arrêt d'index donné
     * (qui peut être identique si l'arrêt en question est une gare)
     */
    default int stationId(int stopId) {
        if (isStationId(stopId)) {
            return stopId;
        } else {
            // Pour une plateforme, déduire l'index dans platforms et retourner l'index de la gare associée.
            return platforms().stationId(stopId - stations().size());
        }
    }

    /**
     * Fonction qui retourne le nom de voie ou de quai de l'arrêt d'index donné, ou null si cet arrêt est une gare.
     * @param stopId un index d'arrêt
     * @return le nom de voie ou de quai de l'arrêt d'index donné, ou null si cet arrêt est une gare.
     */
    default String platformName(int stopId) {
        if (isPlatformId(stopId)) {
            // Pour une plateforme, obtenir son nom (voies/quais).
            return platforms().name(stopId - stations().size());
        } else {
            // Pour une gare, il n'y a pas de nom de voie/quai.
            return null;
        }
    }

}
