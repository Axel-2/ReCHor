package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
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
    // Index de la gare de départ
    private final int DEP_STATION_ID = 0;
    // 	Index de la gare d'arrivée
    private final int ARR_STATION_ID = 1;
    // Durée du changement, en minutes
    private final int TRANSFER_MINUTES = 2;

    // Attributs du tableau structuré
    private final StructuredBuffer tranferStructuredBuffer;

    // Tableau contenant l'intervalle des changements
    // et qui est indexé par les gares d'arrivées
    private final int[] stationIdTransferInterval;

    // Id max de la gare donné dans le buffer
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
        // parce que le tableau final a comme index toutes les gares, mais
        // le buffer donné ne contient pas forcément toutes les gares
        // (il peut y avoir des trous, par exemple passer de la gare 5 à la 8 donc on ne peut
        // pas simplement utiliser buffer.size() pour avoir le nombre de gares)
        // il faut donc déterminer l'index maximal de la gare se trouvant dans
        // le buffer donné pour ce faire, on va itérer sur notre
        // buffer et trouver l'index max
        maxStationId = 0;
        for (int i = 0; i < tranferStructuredBuffer.size(); ++i) {
            int currentStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, i);

            if (currentStationId > maxStationId) {
                maxStationId = currentStationId;
            }
        }

        // maintenant qu'on l'id maximal, on peut créer un tableau qui contiendra
        // une entrée pour chaquune des gares (ce sera des intervalles empaquetés)
        this.stationIdTransferInterval = new int[maxStationId +1];

        // 2. la deuxième étape est de déterminer le contenu de notre tableau

        int currentStartBufferIndex = 0;

        // on boucle sur tous les changements
        while (currentStartBufferIndex < tranferStructuredBuffer.size()) {

            int currentEndBufferIndex = currentStartBufferIndex;

            int currentStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, currentStartBufferIndex);

            int currentNextStationID = tranferStructuredBuffer.getU16(ARR_STATION_ID, currentEndBufferIndex);

            while (currentNextStationID == currentStationId) {

                // si c'est identique, on passe à l'index suivant
                currentEndBufferIndex++;


                if  (currentEndBufferIndex < tranferStructuredBuffer.size()) {
                    currentNextStationID = tranferStructuredBuffer.getU16(ARR_STATION_ID, currentEndBufferIndex);
                } else {
                    break;
                }
            }

            // on crée l'intervalle et on le met dans le tableau
            int packedInterval = PackedRange.pack(currentStartBufferIndex, currentEndBufferIndex);
            stationIdTransferInterval[currentStationId] = packedInterval;

            // on met à jour les index pour le prochain tour de boucle
            currentStartBufferIndex = currentEndBufferIndex;

        }

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

        // on récupère l'index de la gare avec le buffer
        return tranferStructuredBuffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement d'index donné,
     * @param id index de changement
     * @return durée en minutes du changement d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutes(int id) {

        // on récupère les minutes avec le buffer
        return tranferStructuredBuffer.getU8(TRANSFER_MINUTES, id);
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

        // comme on a calculé le tableau correspondant dans le constructeur,
        // on peut maintenant simplement l'utiliser avec l'index donné
        return this.stationIdTransferInterval[stationId];
    }

    /**
     * Fonction qui retourne la durée, en minutes, du changement entre les deux gares d'index donnés,
     * ou lève NoSuchElementException si aucun changement n'est possible entre ces deux gares.
     *
     * @param depStationId id de la gare de départ
     * @param arrStationId id de la gare d'arrivée
     * @return durée en minutes du changement entre les deux gares d'index donnés
     * @throws java.util.NoSuchElementException lève NoSuchElementException si aucun changement n'est possible entre ces deux gares
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {

        // on vérifie d'abord les index grâce à la variable calculée dans
        // le constructeur
        if (depStationId > maxStationId || arrStationId > maxStationId || depStationId < 0 || arrStationId < 0) {
            throw new IndexOutOfBoundsException("Index invalide");
        }

        // On récupère l'intervalle empaqueté des changements arrivant à la gare d'arrivée
        int packedInterval = arrivingAt(arrStationId);

        int start = PackedRange.startInclusive(packedInterval);
        int end = PackedRange.endExclusive(packedInterval);

        if (start == end) {
            // cela signifie que l'intervalle est vide,
            // il n'y a donc aucun changement possible entre ces deux gares
            throw new NoSuchElementException("aucun changement possible entre ces deux gares");
        }

        // on itère sur l'intervalle pour trouver le changement correspondant
        for (int i = start; i < end; i++) {
            if (depStationId == tranferStructuredBuffer.getU16(DEP_STATION_ID, i)){
                // Si on a trouvé le changement correspondant, on retourne sa durée
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
        // on retourne simplement la taille du buffer
        return tranferStructuredBuffer.size();
    }
}
