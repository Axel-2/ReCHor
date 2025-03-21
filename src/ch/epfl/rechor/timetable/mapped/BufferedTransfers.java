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

    // Tableau contenant l'intervalle des changement
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

        // maintenant qu'on la l'id max on peut créer un tableau qui contiendra
        // une entrée pour chaquune des gares (ce sera des intervalles empaquetés)
        this.stationIdTransferInterval = new int[maxStationId +1];

        // 2. la deuxième étape est de déterminer le contenu de notre tableau

        // pour chaque gare on doit stocker l'intervalle des index des changements
        // on a donc besoin de deux variables pour stocker le début et la fin de
        // l'intervalle correspondant à la gare actuelle
        int currentStartIndexToPack = 0;
        int currentEndIndexToPack = 0;

        // on va maintenant itérer sur notre buffer et compter le nombre d'occurrences de chaque
        // gare d'arrivées pour pouvoir construire notre intervalle

        // on initialise une variable avec le premier id de gare qu'on trouve dans le buffer
        int currentStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, 0);

        // on itère donc sur touts les changements de notre buffer
        // on commence la boucle directement à 1 car on a déjà l'élément 0 dans currentStation
        for (int nextTransferIndex = 1; nextTransferIndex < tranferStructuredBuffer.size(); ++nextTransferIndex) {

            // on récupère l'id de la prochaine station dans notre buffer
            int nextStationId = tranferStructuredBuffer.getU16(ARR_STATION_ID, nextTransferIndex);

            // si les ids sont égaux, c'est que l'intervalle n'est pas encore fini
            // sinon l'intervalle est fini et on peut le pack et le mettre dans
            // notre tableau
            if (currentStationId != nextStationId) {

                // on assigne l'index de la prochaine gare (qui est différente)
                // à la variable qui contient la fin de l'intervalle, car
                // l'intervalle est exclusif pour la fin
                currentEndIndexToPack = nextTransferIndex;

                // on peut maintenant créer l'intervalle empaqueté
                int packedInterval = PackedRange.pack(currentStartIndexToPack, currentEndIndexToPack);

                // on n'oublie pas de stocker notre intervalle dans le tableau initial
                stationIdTransferInterval[currentStationId] = packedInterval;

                // S'il y a des IDs intermédiaires qui n'apparaissent pas,
                // on les remplit avec l'intervalle vide correspondant (point d'insertion = currentEndIndexToPack).
                for (int missingId = currentStationId + 1; missingId < nextStationId; missingId++) {
                    stationIdTransferInterval[missingId] = PackedRange.pack(currentEndIndexToPack, currentEndIndexToPack);
                }

                // Il faut maintenant assigner le currentEnd au currentStart pour le prochain
                // tour de boucle (car l'intervalle est inclusif pour le début)
                currentStartIndexToPack = nextTransferIndex;

                // on met à jour aussi currentStationId
                currentStationId = nextStationId;

            }
        }

        // pour le dernier intervalle, on est sorti de la boucle
        // on prend juste la fin du dernier intervalle et la taille du buffer
        stationIdTransferInterval[currentStationId] = PackedRange.pack(currentStartIndexToPack, tranferStructuredBuffer.size());

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
