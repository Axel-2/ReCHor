package ch.epfl.rechor.timetable;

/**
 * Interface représentant des données indexées.
 * Elle est destinée à être étendue par toutes les
 * interfaces représentant des données indexées
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */

public interface Indexed {

    /**
     * Fonction qu retourne le nombre d'éléments des données
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    int size();
}
