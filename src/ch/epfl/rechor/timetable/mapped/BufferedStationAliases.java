package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.StationAliases;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static ch.epfl.rechor.timetable.mapped.Structure.field;

import java.nio.ByteBuffer;
import java.util.List;


/**
 * Classe qui permet d'accéder à une table de noms alternatifs de gares représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedStationAliases implements StationAliases {

    private final int ALIAS_ID = 0;
    private final int STATION_NAME_ID = 1;

    private List<String> stringTable;
    private StructuredBuffer stationAliasesStructuredBuffer;

    private Structure stationAliasesStructure = new Structure(
            field(ALIAS_ID, U16),
            field(STATION_NAME_ID, U16)
    );

    // unique constructeur publique

    /**
     * Constructeur de BufferedStationAliases
     * @param stringTable une table de chaine de charactères qui contient les noms d'alias
     * @param buffer le buffer correspondant aux données aplaties
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {

        this.stringTable = stringTable;
        this.stationAliasesStructuredBuffer = new StructuredBuffer(stationAliasesStructure, buffer);

    }



    /**
     * Fonction qui retourne le nom alternatif d'index donné (p. ex. Losanna),
     *
     * @param id index d'une gare
     * @return Nom alternatif de l'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String alias(int id) {

        // on récupère en premier l'id de l'alias dans la table
        int aliasID =  stationAliasesStructuredBuffer.getU16(ALIAS_ID, id);

        // ensuite on récupère la string dans la table
        String aliasName = stringTable.get(aliasID);

        return aliasName;
    }

    /**
     * Fonction qui retourne le nom de la gare à laquelle correspond le nom alternatif d'index donné (p. ex. Lausanne).
     *
     * @param id index d'une gare
     * @return nom de la gare à laquelle correspond le nom alternatif d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String stationName(int id) {

        // on récupère en premier l'id du nom de la table
        int nameId = stationAliasesStructuredBuffer.getU16(STATION_NAME_ID, id);
        // ensuite on récupère la string dans la table
        String stationNameId = stringTable.get(nameId);

        return stationNameId;
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        return stationAliasesStructuredBuffer.size();
    }
}
