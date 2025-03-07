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

    private int NAME_ID = 0;
    private int LON = 1;
    private int LAT = 2;

    // constante de conversion utilisée
    // pour longitude et latitude
    private final double CONVERSION_CONST = Math.scalb(360, -32);

    private Structure stationStructure = new Structure(
            field(NAME_ID, U16),
            field(LON, S32),
            field(LAT, S32)
    );

    private StructuredBuffer structuredBuffer;


    // TODO IMMUTABILITé ?

    private List<String> stringTable;
    private ByteBuffer buffer;

    // unique constructeur publlique
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {

        this.stringTable = stringTable;

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

        // On récupère l'id du nom dans la zable
        int nameId = structuredBuffer.getU16(NAME_ID, id);

        String name = stringTable.get(nameId);

        return name;

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

        // TODO tests ?

        // on récupère la longitude qui est encodé avec une unité spéciale
        // la longitude est encodée sur 4 octets
        int longitudeCustomUnit = structuredBuffer.getS32(LON, id);

        // on reconverti en degré avec la constante
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

        // on récupère la laltiude qui est encodé avec une unité spéciale
        // la latitude est encodée sur 4 octets
        int latitudeCustomUnit = structuredBuffer.getS32(LAT, id);

        // on reconverti en degré avec la constante
        double latInDegree = CONVERSION_CONST * latitudeCustomUnit;

        // TODO est-ce qu'il faut arrondir ?

        return latInDegree;
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {

        // on peut utiliser la méthode structuredBuffer
        // qui fait exacement ce qu'on veut
        return structuredBuffer.size();
    }
}
