package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;

/**
 * Classe qui permet d'accéder à une table de changements représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedTransfers implements Transfers {

    public BufferedTransfers(ByteBuffer buffer) {

    }


    /**
     * Fonction qui retourne l'index de la gare de départ du changement d'index donné,
     *
     * @param id index de changement
     * @return index de la gare de départ du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int depStationId(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement d'index donné,
     *
     * @param id index de changement
     * @return durée en minutes du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutes(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne l'intervalle empaqueté des index des
     * changements dont la gare d'arrivée est celle d'index donné,
     *
     * @param stationId index de la gare d'arrivée
     * @return int représenant un intervalle empaqueté des index des changements de la gare donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int arrivingAt(int stationId) {
        return 0;
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement entre les deux gares d'index donnés,
     * ou lève NoSuchElementException si aucun changement n'est possible entre ces deux gares.
     *
     * @param depStationId id de la gare de départ
     * @param arrStationId id de la gare d'arrivée
     * @return durée en minutes du chanegement entre les deux gares d'index donnés
     * @throws NoSuchElementException    lève NoSuchElementException si aucun changement n'est possible entre ces deux gares
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        return 0;
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        return 0;
    }
}
