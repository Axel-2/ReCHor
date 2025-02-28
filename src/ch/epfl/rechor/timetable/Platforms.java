package ch.epfl.rechor.timetable;

/**
 * Interface qui représente des voies/quais indexés
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface Platforms extends Indexed {


    /**
     * Fonction qui retourne le nom de la voie (p. ex. 70) ou du quai (p. ex. A), qui peut être vide
     * @param id index de la voie ou du quai
     * @return nom de la voie (p. ex. 70) ou du quai (p. ex. A), qui peut être vide
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    String name(int id);

    /**
     * Fonction qui retourne l'index de la gare à laquelle cette voie ou ce quai appartient.
     * @param id id de la voie ou du quai
     * @return index de la gare à laquelle cette voie ou ce quai appartient
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int stationId(int id);

}
