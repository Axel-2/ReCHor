package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;


/**
 * Classe qui implémente l'interface Stations et permet d'accéder
 * à une table de gares représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedStations implements Stations {

    private int NAME_ID = 0;
    private int LON = 1;
    private int LAT = 2;

    Structure stationStructure = new Structure(
            field(NAME_ID, U16),
            field(LON, S32),
            field(LAT, S32)
    );




    // unique constructeur publlique
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {


    }


    /**
     * Retourne le nom de la gare d'index donné
     * @param id index d'une gare
     * @return le nom de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String name(int id) {
        return "";
    }

    /**
     * Fonction qui retourne la longitude, en degrés, de la gare d'index donné
     *
     * @param id index d'une gare
     * @return longitude, en degrés, de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public double longitude(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne la latitude, en degrés, de la gare d'index donné.
     *
     * @param id index d'une gare
     * @return latitude, en degrés, de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public double latitude(int id) {
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
