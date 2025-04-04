package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Classe qui permet d'accéder à une table de courses représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedTrips implements Trips {

    // Attributs
    // Index de la ligne de la course
    private final static int ROUTE_ID  = 0;
    // Index de chaîne de la destination finale
    private final static int DESTINATION_ID = 1;

    // Tables des noms
    private final List<String> stringTable;

    // Tableau structuré
    private final StructuredBuffer structuredBuffer;

    /**
     * Constructeur public
     * @param stringTable table de string
     * @param buffer buffer utile à la création du structuredBuffer
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {

        // On stocke la table des noms
        this.stringTable = stringTable;

        // Structure d'une course
        Structure tripStructure = new Structure(
                // Index de la ligne de la course
                Structure.field(ROUTE_ID, Structure.FieldType.U16),
                // Index de chaîne de la destination finale
                Structure.field(DESTINATION_ID, Structure.FieldType.U16));

        // création du tableau structuré
        this.structuredBuffer = new StructuredBuffer(tripStructure, buffer);
    }

    /**
     * Fonction qui retourne l'index de la ligne à laquelle la course d'index donné appartient,
     *
     * @param id index d'une course
     * @return index de la ligne à laquelle la course appartient
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int routeId(int id) {

        // on récupère simplement l'id dans
        // le tableau structuré
        return structuredBuffer.getU16(ROUTE_ID, id);
    }

    /**
     * Fonction qui retourne le nom de la destination finale de la course.
     *
     * @param id index d'une course
     * @return nom de la destination finale de la course
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String destination(int id) {

        // on récupère l'index
        int destinationIndex = structuredBuffer.getU16(DESTINATION_ID, id);

        // on retourne la chaine correspondante à
        // l'aide de la table
        return stringTable.get(destinationIndex);
    }

    /**
     * Fonction qui retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {

        // il suffit de retourner la taille du buffer
        return structuredBuffer.size();
    }
}
