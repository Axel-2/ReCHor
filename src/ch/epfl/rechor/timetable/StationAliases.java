package ch.epfl.rechor.timetable;


/**
 *  Interface représentant les noms alternatifs des gares
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface StationAliases extends Indexed {

    /**
     * Fonction qui retourne le nom alternatif d'index donné (p. ex. Losanna),
     * @param id index d'une gare
     * @return Nom alternatif de l'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    String alias(int id);

    /**
     * Fonction qui retourne le nom de la gare à laquelle correspond le nom alternatif d'index donné (p. ex. Lausanne).
     * @param id index d'une gare
     * @return nom de la gare à laquelle correspond le nom alternatif d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    String stationName(int id);
}
