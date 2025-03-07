package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

public final class BufferedPlatforms implements Platforms {
    private final static int NAME_ID = 0;
    private final static int STATION_ID = 1;

    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;


    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {

        this.stringTable = stringTable;

        // Structure d'une plateforme
        Structure platformStructure = new Structure(
                Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(STATION_ID, Structure.FieldType.U16));

        this.structuredBuffer = new StructuredBuffer(platformStructure, buffer);
    }

    /**
     * Fonction qui retourne le nom de la voie (p. ex. 70) ou du quai (p. ex. A), qui peut être vide
     *
     * @param id index de la voie ou du quai
     * @return nom de la voie (p. ex. 70) ou du quai (p. ex. A), qui peut être vide
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String name(int id) {

        // On récupère l'index du nom dans la chaîne de caractère, en cherchant l'info dans notre tableau (buffer)
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);

        // on retourne le nom correspondant
        return stringTable.get(nameIndex);
    }

    /**
     * Fonction qui retourne l'index de la gare à laquelle cette voie ou ce quai appartient.
     *
     * @param id id de la voie ou du quai
     * @return index de la gare à laquelle cette voie ou ce quai appartient
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int stationId(int id) {
        return structuredBuffer.getU16(STATION_ID, id);
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
