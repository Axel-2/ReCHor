package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static ch.epfl.rechor.timetable.mapped.Structure.field;


/**
 * Classe qui implémente l'interface Stations et permet d'accéder
 * à une table de gares représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedStations implements Stations {

    // Constante pour la structure
    private final static int NAME_ID = 0;
    private final static int LON = 1;
    private final static int LAT = 2;

    // constante de conversion utilisée
    // pour longitude et latitude
    private final static double CONVERSION_CONST = Math.scalb(360, -32);

    // Variable pour stocker le tableau structuré
    private final  StructuredBuffer structuredBuffer;

    // table de conversion qui contient les
    // chaines de charactère
    private final  List<String> stringTable;

    /**
     * Constructeur de BufferedStations
     * @param stringTable une table de chaine de char qui correspond aux nom des stations
     * @param buffer un buffer qui correspond aux données aplaties
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {

        // on stock notre table de conversion
        this.stringTable = stringTable;

        // on crée notre structure
        Structure stationStructure = new Structure(
                // Index de chaîne du nom de la gare
                field(NAME_ID, U16),
                // Longitude de la gare
                field(LON, S32),
                // Latitude de la gare
                field(LAT, S32)
        );

        // création du buffer structuré
        this.structuredBuffer = new StructuredBuffer(stationStructure, buffer);

    }


    /**
     * Retourne le nom de la gare d'index donné
     * @param id index d'une gare
     * @return le nom de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String name(int id) {

        // On récupère l'id du nom dans la table structurée
        int nameId = structuredBuffer.getU16(NAME_ID, id);

        // on récupère ensuite la chaine dans la table à l'aide de l'id
        return stringTable.get(nameId);
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

        // on récupère la longitude qui est encodée avec une unité spéciale
        // la longitude est encodée sur 4 octets
        int longitudeCustomUnit = structuredBuffer.getS32(LON, id);

        // on remet en degré avec la constante
        double longInDegree = CONVERSION_CONST * longitudeCustomUnit;

        return longInDegree;
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

        // on récupère la latitude qui est encodée avec une unité spéciale
        // la latitude est encodée sur 4 octets
        int latitudeCustomUnit = structuredBuffer.getS32(LAT, id);

        // on remet en degré avec la constante
        double latInDegree = CONVERSION_CONST * latitudeCustomUnit;

        return latInDegree;
    }

    /**
     * Fonction qui retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {

        // on peut utiliser la méthode structuredBuffer
        // qui fait exactement ce qu'on veut
        return structuredBuffer.size();
    }
}
