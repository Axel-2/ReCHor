package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;

/**
 * Classe qui permet d'accéder à une table de liaison représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedConnections implements Connections {

    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {

    }
    /**
     * Fonction qui retourne l'index de l'arrêt de départ de la liaison d'index donné,
     *
     * @param id index d'une liaison
     * @return index de l'arrêt de départ de la liaison d'index donné,
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int depStopId(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit,
     *
     * @param id index d'une liaison
     * @return l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int depMins(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne l'index de l'arrêt d'arrivée de la liaison d'index donné
     *
     * @param id index d'une liaison
     * @return l'index de l'arrêt d'arrivée de la liaison d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int arrStopId(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne l'heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit
     *
     * @param id index d'une liaison
     * @return heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int arrMins(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne l'index de la course à laquelle appartient la liaison d'index donné
     *
     * @param id index d'une liaison
     * @return index de la course à laquelle appartient la liaison d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int tripId(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne la position de la liaison d'index donné dans
     * la course à laquelle elle appartient, la première liaison d'une course ayant l'index 0,
     *
     * @param id index d'une liaison
     * @return position de la liaison d'index donné dans la course à laquelle elle appartient,
     * la première liaison d'une course ayant l'index 0
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int tripPos(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne l'index de la liaison suivant celle d'index donné dans la course à
     * laquelle elle appartient, ou l'index de la première liaison de la course si la liaison d'index
     * donné est la dernière de la course.
     *
     * @param id index d'une liaison
     * @return index de la liaison suivant celle d'index donné dans la course à laquelle elle appartient,
     * ou l'index de la première liaison de la course si la liaison d'index donné est la dernière de la course.
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int nextConnectionId(int id) {
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
