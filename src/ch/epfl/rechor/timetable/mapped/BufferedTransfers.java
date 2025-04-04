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

    // Attributs
    // Index de la gare de départ
    private final static int DEP_STATION_ID = 0;
    // 	Index de la gare d'arrivée
    private final static int ARR_STATION_ID = 1;
    // Durée du changement, en minutes
    private final static int TRANSFER_MINUTES = 2;

    // Attributs du tableau structuré
    private final StructuredBuffer transferStructuredBuffer;

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

        // instanciation du tableau structuré
        this.transferStructuredBuffer = new StructuredBuffer(transferStructure, buffer);

        // --------1) Recherche de l'index maximal de la gare pour créer un tableau de bonne taille -----
        maxStationId = 0;
        for (int i = 0; i < transferStructuredBuffer.size(); ++i) {
            int currentStationId = transferStructuredBuffer.getU16(ARR_STATION_ID, i);

            if (currentStationId > maxStationId) {
                maxStationId = currentStationId;
            }
        }

        // Création du tableau de bonne taille
        this.stationIdTransferInterval = new int[maxStationId + 1];

        // --------2) Remplissage du tableau ----------

        int currentStartBufferIndex = 0;

        // on boucle sur tous les changements
        while (currentStartBufferIndex < transferStructuredBuffer.size()) {

            // à chaque tour de boucle, on donne la valeur de l'index de départ
            // à la valeur d'index de fin
            int currentEndBufferIndex = currentStartBufferIndex;

            // On regarde les gares d'arrivées des changements afin de les comparer
            int currentStationId = transferStructuredBuffer.getU16(ARR_STATION_ID, currentStartBufferIndex);
            int nextStationID = transferStructuredBuffer.getU16(ARR_STATION_ID, currentEndBufferIndex);

            while (nextStationID == currentStationId) {

                // Si ce sont les mêmes gares, alors on incrémente,
                // pour augmenter la taille de l'intervalle empaqueté.
                currentEndBufferIndex++;

                // On met à jour l'id de la gare d'arrivée pour la prochaine comparaison,
                // et on fait attention à rester dans la taille du tableau
                if  (currentEndBufferIndex < transferStructuredBuffer.size()) {
                    nextStationID = transferStructuredBuffer.getU16(ARR_STATION_ID, currentEndBufferIndex);
                } else {
                    break;
                }
            }

            // Lorsqu'on a atteint la fin de l'intervalle, c.-à-d. qu'on a trouvé
            // deux transfers avec une gare d'arrivée différente, on crée notre intervalle.
            int packedInterval = PackedRange.pack(currentStartBufferIndex, currentEndBufferIndex);
            stationIdTransferInterval[currentStationId] = packedInterval;

            // On met à jour les index pour le prochain tour de boucle
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
        return transferStructuredBuffer.getU16(DEP_STATION_ID, id);
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
        return transferStructuredBuffer.getU8(TRANSFER_MINUTES, id);
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

        // Puisqu'on a calculé le tableau correspondant dans le constructeur,
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

        // Gestion des index invalides
        if (depStationId > maxStationId || arrStationId > maxStationId || depStationId < 0 || arrStationId < 0) {
            throw new IndexOutOfBoundsException("Index invalide");
        }

        // On récupère l'intervalle empaqueté des changements arrivant à la gare d'arrivée
        int packedInterval = arrivingAt(arrStationId);

        int start = PackedRange.startInclusive(packedInterval);
        int end = PackedRange.endExclusive(packedInterval);

        if (start == end) {
            // Cela signifie que l'intervalle est vide,
            // il n'y a donc aucun changement possible entre ces deux gares
            throw new NoSuchElementException("aucun changement possible entre ces deux gares");
        }

        // On itère sur l'intervalle pour trouver le changement correspondant
        for (int i = start; i < end; i++) {
            if (depStationId == transferStructuredBuffer.getU16(DEP_STATION_ID, i)){
                // Si on a trouvé le changement correspondant, on retourne sa durée
                return transferStructuredBuffer.getU8(TRANSFER_MINUTES, i);
            }
        }

        // Si on arrive jusqu'à là, c'est qu'aucun changement ne correspond, on retourne une exception
        throw new NoSuchElementException("Pas de changements entre " + depStationId + " et " + arrStationId);

    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        // On retourne simplement la taille du buffer
        return transferStructuredBuffer.size();
    }
}
