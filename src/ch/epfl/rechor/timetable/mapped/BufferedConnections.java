package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Classe qui permet d'accéder à une table de liaison représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedConnections implements Connections {

    // Champs du connectionsBuffer
    private final static int DEP_STOP_ID  = 0;
    private final static int DEP_MINUTES  = 1;
    private final static int ARR_STOP_ID  = 2;
    private final static int ARR_MINUTES  = 3;
    private final static int TRIP_POS_ID  = 4;

    // Buffers
    private final StructuredBuffer connectionsStructuredBuffer;
    private final IntBuffer succStructuredBuffer;

    /**
     * Constructeur public qui construit une instance donnant accès aux données aplaties réparties entre le
     * tableau buffer, qui contient les liaisons aplaties, et le tableau succBuffer,
     * qui contient les liaisons suivantes.
     * @param buffer tableau qui contient les liaisons aplaties
     * @param succBuffer tableau qui contient les liaisons suivantes
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {

        // Structure d'une liaison
        Structure connectionStructure = new Structure(
                // Index de l'arrêt de départ
                Structure.field(DEP_STOP_ID, Structure.FieldType.U16),
                // Heure de départ, en minutes après minuit
                Structure.field(DEP_MINUTES, Structure.FieldType.U16),
                // Index de l'arrêt d'arrivée
                Structure.field(ARR_STOP_ID, Structure.FieldType.U16),
                // Heure d'arrivée, en minutes après minuit
                Structure.field(ARR_MINUTES, Structure.FieldType.U16),
                // Index de la course et position en son sein
                Structure.field(TRIP_POS_ID, Structure.FieldType.S32));


        // Créations des structured buffers
        this.connectionsStructuredBuffer = new StructuredBuffer(connectionStructure, buffer);

        // ici on construit le tableau structuré plus simplement
        // on transforme un ByteBuffer en IntBuffer
        // voir conseil de prog 3.3.1
        this.succStructuredBuffer = succBuffer.asIntBuffer();
    }

    /**
     * Fonction qui retourne l'index de l'arrêt de départ de la liaison d'index donné,
     *
     * @param id index d'une liaison
     * @return index de l'arrêt de départ de la liaison d'index donné,
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int depStopId(int id) {

        return connectionsStructuredBuffer.getU16(DEP_STOP_ID, id);
    }

    /**
     * Fonction qui retourne l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit,
     *
     * @param id index d'une liaison
     * @return l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int depMins(int id) {

        return connectionsStructuredBuffer.getU16(DEP_MINUTES, id);
    }

    /**
     * Fonction qui retourne l'index de l'arrêt d'arrivée de la liaison d'index donné
     * @param id index d'une liaison
     * @return l'index de l'arrêt d'arrivée de la liaison d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int arrStopId(int id) {

        return connectionsStructuredBuffer.getU16(ARR_STOP_ID, id);
    }

    /**
     * Fonction qui retourne l'heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit
     *
     * @param id index d'une liaison
     * @return heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int arrMins(int id) {

        return connectionsStructuredBuffer.getU16(ARR_MINUTES, id);
    }

    /**
     * Fonction qui retourne l'index de la course à laquelle appartient la liaison d'index donné
     *
     * @param id index d'une liaison
     * @return index de la course à laquelle appartient la liaison d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int tripId(int id) {

        // on récupère la valeur de TRIP_POS_ID puis on shift pour
        // ne que récupérer les 24 bits de poids forts correspondant à l'id
        // et enlever les 8 bits de poids faible
       return connectionsStructuredBuffer.getS32(TRIP_POS_ID, id) >>> 8;
    }

    /**
     * Fonction qui retourne la position de la liaison d'index donné dans
     * la course à laquelle elle appartient, la première liaison d'une course ayant l'index 0,
     *
     * @param id index d'une liaison
     * @return position de la liaison d'index donné dans la course à laquelle elle appartient,
     * la première liaison d'une course ayant l'index 0
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int tripPos(int id) {

        // Masque pour récupérer seulement les 8 bits de poids faible (où se trouve la pos)
        // 0xFF n'a que des 1 sur les premiers 8 bits ensuite que des 0.
        return connectionsStructuredBuffer.getS32(TRIP_POS_ID, id) & 0xFF;
    }

    /**
     * Fonction qui retourne l'index de la liaison suivant celle d'index donné dans la course à
     * laquelle elle appartient, ou l'index de la première liaison de la course si la liaison d'index
     * donné est la dernière de la course.
     *
     * @param id index d'une liaison
     * @return index de la liaison suivant celle d'index donné dans la course à laquelle elle appartient,
     * ou l'index de la première liaison de la course si la liaison d'index donné est la dernière de la course.
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int nextConnectionId(int id) {

        // on utilise simplement le get de IntBuffer
        return succStructuredBuffer.get(id);
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {

        // ici on peut utiliser la méthode size de structuredBuffer
        return connectionsStructuredBuffer.size();
    }
}
