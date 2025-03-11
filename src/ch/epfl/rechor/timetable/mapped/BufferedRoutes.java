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

    // Attributs
    private final static int NAME_ID = 0;
    private final static int KIND = 1;

    // Tables des noms
    private final List<String> stringTable;

    // Tableau structuré
    private final StructuredBuffer structuredBuffer;


    /**
     * Constructeur public
     * @param stringTable table de string
     * @param buffer buffer utile à la création du structuredBuffer
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {

        this.stringTable = stringTable;

        // Structure d'une plateforme
        Structure platformStructure = new Structure(
                Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(KIND, Structure.FieldType.U8));

        this.structuredBuffer = new StructuredBuffer(platformStructure, buffer);
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
        int vehicleCode = structuredBuffer.getU8(KIND, id);
        return Vehicle.ALL.get(vehicleCode);
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
        // On récupère l'index du nom dans la chaîne de caractère, en cherchant l'info dans notre tableau (buffer)
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);

        // on retourne le nom correspondant
        return stringTable.get(nameIndex);
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
