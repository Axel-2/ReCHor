package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * Classe qui permet d'accéder à une table de changements représentée de manière aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class BufferedTransfers implements Transfers {

    // un changement est un trajet à pied qui peut être effectué
    // soit entre deux gares voisines, soit au sein d'une même gare

    // Attributs
    private final int DEP_STATION_ID = 0;
    private final int ARR_STATION_ID = 1;
    private final int TRANSFER_MINUTES = 2;

    // Attributs du buffer
    private final StructuredBuffer tranferStructuredBuffer;

    // Tableau contenant l'intervalle des changement
    // et qui est indexé par les gares d'arrivées
    private final int[] stationIdTransferInterval;

    private int maxStationId;

    /**
     * Constructeur qui construit une instance donnant accès
     * aux données aplaties disponibles dans le tableau buffer.
     * @param buffer un tableau d'octet qui contient des données aplaties
     */
    public BufferedTransfers(ByteBuffer buffer) {

        // Structure des données aplaties d'un transfert

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

        // Création d'une table associant à toutes les gares
        // l'intervalle des index des changements qui y arrivent

        // 1. la première étape est de déterminer la taille du tableau
        // comme le tableau a comme index toutes les gares, il faut déterminer
        // l'index maximal d'une gare
        // pour se faire on va itérer sur notre buffer et trouver l'index man

        maxStationId = 0;
        for (int i = 0; i < tranferStructuredBuffer.size(); ++i) {
            int currentStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, i);

            if (currentStationId > maxStationId) {
                maxStationId = currentStationId;
            }
        }

        this.stationIdTransferInterval = new int[maxStationId +1];

        // 2. la deuxième étape est de déterminer le contenu de notre tableau
        int currentStart = 0;
        int currentEnd = 0;

        // on initialise la variable  avec le premier id de gare qu'on trouve dans le buffer
        int currentStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, 0);

        // on commence la boucle direct à 1 car on a déjà l'élément 0 dans currentStation
        for (int currentTransferIndex = 1; currentTransferIndex < tranferStructuredBuffer.size(); ++currentTransferIndex) {

            int currentCheckedStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, currentTransferIndex);

            // si les ids sont égaux, c'est que l'intervalle n'est pas encore fini
            // sinon l'intervalle est fini et on peut le pack
            if (currentStationId != currentCheckedStationId) {

                currentEnd = currentTransferIndex;

                int packedInterval = PackedRange.pack(currentStart, currentEnd);

                // on oublie pas de stocker notre intervalle dans le tableau initial
                stationIdTransferInterval[currentStationId] = packedInterval;

                // Si il y a des IDs intermédiaires qui n'apparaissent pas,
                // on les remplit avec l'intervalle vide correspondant (point d'insertion = currentEnd).
                for (int missingId = currentStationId + 1; missingId < currentCheckedStationId; missingId++) {
                    stationIdTransferInterval[missingId] = PackedRange.pack(currentEnd, currentEnd);
                }

                // on peut mtn passer le currentstart au currentEnd pour le prochain check
                currentStart = currentTransferIndex;

                // on updat aussi currentStationId
                currentStationId = currentCheckedStationId;

            }
        }

        // pour le dernier intervalle, on est sorti de la boucle
        // on prend juste la fin du dernier intervalle et la taille du buffer
        stationIdTransferInterval[currentStationId] = PackedRange.pack(currentStart, tranferStructuredBuffer.size());

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

        // comme on a calculé le tableau correspondant dans le constructeur
        // on peut juste l'utiliser avec l'index donné

        return this.stationIdTransferInterval[stationId];
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement entre les deux gares d'index donnés,
     * ou lève NoSuchElementException si aucun changement n'est possible entre ces deux gares.
     *
     * @param depStationId id de la gare de départ
     * @param arrStationId id de la gare d'arrivée
     * @return durée en minutes du changement entre les deux gares d'index donnés
     * @throws java.util.NoSuchElementException    lève NoSuchElementException si aucun changement n'est possible entre ces deux gares
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {


        // on vérifie d'abord les index
        if (depStationId > maxStationId || arrStationId > maxStationId || depStationId < 0 || arrStationId < 0) {
            throw new IndexOutOfBoundsException("Index invalide");
        }
        // On récupère l'intervalle empaqueté des changements arrivant à la gare d'arrivée
        int packedInterval = arrivingAt(arrStationId);

        int start = PackedRange.startInclusive(packedInterval);
        int end = PackedRange.endExclusive(packedInterval);

        if (start == end) {
            // cela signifie que l'intervalle est vide
            // il n y a donc aucun changement possible entre ces deux gares
            throw new NoSuchElementException("aucun changement possible entre ces deux gares");
        }

        for (int i = start; i < end; i++) {
            if (depStationId == tranferStructuredBuffer.getU16(DEP_STATION_ID, i)){
                // Si on a trouvé le changement correspondant, on return sa durée
                return tranferStructuredBuffer.getU8(TRANSFER_MINUTES, i);
            }
        }

        // Si on arrive jusqu'à là, c'est que aucun changement ne correspond, on retourne une exception
        throw new NoSuchElementException("Pas de changements entre " + depStationId + " et " + arrStationId);

    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        return tranferStructuredBuffer.size();
    }
}
