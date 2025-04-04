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
    // Index de chaîne du nom de la ligne
    private final static int NAME_ID = 0;
    // Type de véhicule desservant la ligne
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

        // stockage de la table de chaine de charactères
        this.stringTable = stringTable;

        // Structure d'une route
        Structure routeStructure = new Structure(
                // Index de chaîne du nom de la ligne
                Structure.field(NAME_ID, Structure.FieldType.U16),
                // Type de véhicule desservant la ligne
                // qui est un entier entre 0 et 6
                Structure.field(KIND, Structure.FieldType.U8));

        // ensuite, on crée le tableau structuré à l'aide de notre
        // structure
        this.structuredBuffer = new StructuredBuffer(routeStructure, buffer);
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

        // on récupère l'id dans le tableau structuré
        int vehicleCode = structuredBuffer.getU8(KIND, id);

        // on retourne le véhicule avec l'id correspondant
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

        // On récupère l'index du nom dans la chaîne de caractère,
        // en cherchant l'info dans notre tableau structuré
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);

        // on retourne le nom correspondant grâce
        // à notre table
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

        // on retourne simplement la taille de
        // notre tableau structuré
        return structuredBuffer.size();
    }
}
