package ch.epfl.rechor.timetable;


import java.util.NoSuchElementException;

/**
 * Interface qui représente des changements indexés
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface Transfers extends Indexed {

    /**
     * Fonction qui retourne l'index de la gare de départ du changement d'index donné,
     * @param id index de changement
     * @return index de la gare de départ du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int depStationId(int id);

    /**
     * Fonction qui retourne la durée, en minutes, du changement d'index donné,
     * @param id index de changement
     * @return durée en minutes du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int minutes(int id);

    /**
     * Fonction qui retourne l'intervalle empaqueté des index des
     * changements dont la gare d'arrivée est celle d'index donné,
     * @param stationId index de la gare d'arrivée
     * @return int représenant un intervalle empaqueté des index des changements de la gare donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int arrivingAt(int stationId);

    /**
     * Fonction qui retourne la durée, en minutes, du changement entre les deux gares d'index donnés,
     * ou lève NoSuchElementException si aucun changement n'est possible entre ces deux gares.
     * @param depStationId id de la gare de départ
     * @param arrStationId id de la gare d'arrivée
     * @return durée en minutes du chanegement entre les deux gares d'index donnés
     * @throws NoSuchElementException lève NoSuchElementException si aucun changement n'est possible entre ces deux gares
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int minutesBetween(int depStationId, int arrStationId);

}
