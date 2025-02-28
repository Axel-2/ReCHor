package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * Interface qui représente des lignes de transport public indexé
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public interface Routes extends Indexed {


    /**
     * Retourne le type de véhicule desservant la ligne d'index donné
     * @param id index de la ligne donnée
     * @return type de véhicule desservant la ligne d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    Vehicle vehicle(int id);

    /**
     * Retourne le nom de la ligne d'index donné (p. ex. IR 15).
     * @param id index de la ligne donné
     * @return nom de la ligne d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    String name(int id);

}
