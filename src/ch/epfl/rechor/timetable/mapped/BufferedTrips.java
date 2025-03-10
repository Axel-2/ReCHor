package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Classe qui permet d'accéder à une table de courses représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedTrips implements Trips {

    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {

    }

    /**
     * Fonction qui retourne l'index de la ligne à laquelle la course d'index donné appartient,
     *
     * @param id index d'une course
     * @return index de la ligne à laquelle la course appartient
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int routeId(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne le nom de la destination finale de la course.
     *
     * @param id index d'une course
     * @return nom de la destination finale de la course
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String destination(int id) {
        return "";
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
