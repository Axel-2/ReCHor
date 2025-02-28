package ch.epfl.rechor.timetable;


/**
 * Interface qui représente des liaisons indexées
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface Connections extends Indexed {

    /**
     * Fonction qui retourne l'index de l'arrêt de départ de la liaison d'index donné,
     * @param id index d'une liaison
     * @return index de l'arrêt de départ de la liaison d'index donné,
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int depStopId(int id);

    /**
     * Fonction qui retourne l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit,
     * @param id index d'une liaison
     * @return l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int depMins(int id);

    /**
     * Fonction qui retourne l'index de l'arrêt d'arrivée de la liaison d'index donné
     * @param id index d'une liaison
     * @return  l'index de l'arrêt d'arrivée de la liaison d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int arrStopId(int id);

    /**
     * Fonction qui retourne l'heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit
     * @param id index d'une liaison
     * @return heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int arrMins(int id);

    /**
     * Fonction qui retourne l'index de la course à laquelle appartient la liaison d'index donné
     * @param id index d'une liaison
     * @return index de la course à laquelle appartient la liaison d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int tripId(int id);

    /**
     * Fonction qui retourne la position de la liaison d'index donné dans
     * la course à laquelle elle appartient, la première liaison d'une course ayant l'index 0,
     * @param id index d'une liaison
     * @return position de la liaison d'index donné dans la course à laquelle elle appartient,
     * la première liaison d'une course ayant l'index 0
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int tripPos(int id);


    /**
     * Fonction qui retourne l'index de la liaison suivant celle d'index donné dans la course à
     * laquelle elle appartient, ou l'index de la première liaison de la course si la liaison d'index
     * donné est la dernière de la course.
     * @param id index d'une liaison
     * @return index de la liaison suivant celle d'index donné dans la course à laquelle elle appartient,
     * ou l'index de la première liaison de la course si la liaison d'index donné est la dernière de la course.
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    int nextConnectionId(int id);
}
