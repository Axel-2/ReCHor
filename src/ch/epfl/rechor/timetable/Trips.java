package ch.epfl.rechor.timetable;

/**
 * Interface qui représente des courses de transport public indexées
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface Trips extends Indexed {

    /**
     * Fonction qui retourne l'index de la ligne à laquelle la course d'index donné appartient,
     * @param id index d'une course
     * @return index de la ligne à laquelle la course appartient
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int routeId(int id);

    /**
     * Fonction qui retourne le nom de la destination finale de la course.
     * @param id index d'une course
     * @return nom de la destination finale de la course
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    String destination(int id);
}
