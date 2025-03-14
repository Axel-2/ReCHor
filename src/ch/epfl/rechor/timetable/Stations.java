package ch.epfl.rechor.timetable;


/**
 * Interface qui représente les gares indexées
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface Stations extends Indexed {

    // Toutes ces méthodes lèvent une IndexOutOfBoundsException si l'index qu'on leur passe (nommé id ci-dessous) est
    // invalide, c.-à-d. inférieur à 0 ou supérieur ou égal à la taille retournée par size().

    /**
     * Retourne le nom de la gare d'index donné
     * @param id index d'une gare
     * @return le nom de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    String name(int id);

    /**
     * Fonction qui retourne la longitude, en degrés, de la gare d'index donné
     * @param id index d'une gare
     * @return longitude, en degrés, de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    double longitude(int id );

    /**
     * Fonction qui retourne la latitude, en degrés, de la gare d'index donné.
     * @param id index d'une gare
     * @return latitude, en degrés, de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    double latitude(int id);

}
