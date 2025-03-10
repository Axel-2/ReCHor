package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Classe qui permet d'accéder à une table de lignes représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedRoutes implements Routes {

    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        // TODO A FAIRE
    }

    /**
     * Retourne le type de véhicule desservant la ligne d'index donné
     *
     * @param id index de la ligne donnée
     * @return type de véhicule desservant la ligne d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public Vehicle vehicle(int id) {
        return null;
    }

    /**
     * Retourne le nom de la ligne d'index donné (p. ex. IR 15).
     *
     * @param id index de la ligne donné
     * @return nom de la ligne d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String name(int id) {
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
