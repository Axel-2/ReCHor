package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * Classe qui permet d'accéder à une table de changements représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedTransfers implements Transfers {

    // Attributs
    private final int DEP_STATION_ID = 0;
    private final int ARR_STATION_ID = 1;
    private final int TRANSFER_MINUTES = 2;

    // Attributs du buffer
    private final StructuredBuffer tranferStructuredBuffer;

    /**
     * Constructeur qui construit une instance donnant accès
     * aux données aplaties disponibles dans le tableau buffer.
     * @param buffer un tableau d'octet qui contient des données aplaties
     */
    public BufferedTransfers(ByteBuffer buffer) {

        // Structure d'un transfert
        Structure transferStructure = new Structure(

                // Index de la gare de départ
                Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
                // Index de la gare d'arrivée
                Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
                // Durée du changement, en minutes
                Structure.field(TRANSFER_MINUTES, Structure.FieldType.U8)

        );

        // à l'aide de la structure et du buffer donné en paramètre, on peut maintenant
        // créer le tableau structuré
        this.tranferStructuredBuffer = new StructuredBuffer(transferStructure, buffer);


    }


    /**
     * Fonction qui retourne l'index de la gare de départ du changement d'index donné,
     *
     * @param id index de changement
     * @return index de la gare de départ du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int depStationId(int id) {

        int depStationId = tranferStructuredBuffer.getU16(DEP_STATION_ID, id);
        return depStationId;
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement d'index donné,
     *
     * @param id index de changement
     * @return durée en minutes du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutes(int id) {


        int minutes = tranferStructuredBuffer.getU8(TRANSFER_MINUTES, id);

        return minutes;
    }

    /**
     * Fonction qui retourne l'intervalle empaqueté des index des
     * changements dont la gare d'arrivée est celle d'index donné,
     *
     * @param stationId index de la gare d'arrivée
     * @return int représenant un intervalle empaqueté des index des changements de la gare donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int arrivingAt(int stationId) {
        return tranferStructuredBuffer.;
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement entre les deux gares d'index donnés,
     * ou lève NoSuchElementException si aucun changement n'est possible entre ces deux gares.
     *
     * @param depStationId id de la gare de départ
     * @param arrStationId id de la gare d'arrivée
     * @return durée en minutes du chanegement entre les deux gares d'index donnés
     * @throws NoSuchElementException    lève NoSuchElementException si aucun changement n'est possible entre ces deux gares
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        return 0;
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        return 0;
    }
}
