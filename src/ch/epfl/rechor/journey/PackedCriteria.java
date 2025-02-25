package ch.epfl.rechor.journey;


import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.Preconditions;

public class PackedCriteria {

    // Pour rendre la classe non instantiable
    private PackedCriteria() {}

    public static long pack(int arrMins, int changes, int payload) {

        // arrMin est exprimé en minutes écoulées depuis minuit
        // On teste si l'heure est valide
        Preconditions.checkArgument(-240 <= arrMins && arrMins < 2880);

        // On translate pour garantir des valeurs positives
        arrMins += 240;

        // TODO changes < 0 nécessaire ou en trop ??
        // On test si les nombres de changements tiennent sur 7 bits
        // 127 est le nombre max sur 7 bits
        Preconditions.checkArgument(changes > 0 && changes <= 127);

        // Long initial
        long resultLong = 0L;

        // ajout de arrMins
        long arrMinShifted = ((long) arrMins) << 39;
        resultLong = resultLong | arrMinShifted;

        // Ajout du changes
        long changesShifted = ((long) changes) << 32;
        resultLong = changesShifted | resultLong;

        // On ajoute le payload
        resultLong = (((long) payload)) | resultLong;

        return resultLong;
    }

    public static boolean hasDepMins(long criteria) {


        // On veut récupérer l'heure de départ qui est
        // dans les bits 51 à 62
        // On shift alors de 51 bits vers la droite puis
        // on masque avec 0xFFF qui représente les 12 premiers bits
        long bits51to62 = (criteria >>> 51) & 0xFFF;

        // retourne vrai si l'heure n'est pas nulle
        return bits51to62 != 0;
    }

    public static int depMins(long criteria){

        // TODO
        Preconditions.checkArgument(true);

        // retourne l'heure de départ (en minutes après minuit) des critères empaquetés donnés,
        // ou lève IllegalArgumentException si ces critères n'incluent pas une heure de départ,

        // Comme dans la fonction précédant, on récupère les bits
        // correspondants à l'heure de départ
        long bits51to62 = (criteria >>> 51) & 0xFFF;

        // On ajoute le complément
        int depMins = (int) bits51to62 + 4095;

        // on enlève l'offset de 4h
        depMins -= 240;

        return depMins;
    }

    public static int arrMins(long criteria){
        return 0;
    }

    public static int changes(long criteria){
        return 0;
    }

    public static int payload(long criteria){
        return 0;
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2){
        return false;
    }

    /**
     * Retourne des critères empaquetés identiques à ceux donnés, mais sans heure de départ
     * @param criteria critères
     * @return critères sans heure de départ
     */
    public static long withoutDepMins(long criteria){
        return criteria & ~(0xFFFL << 51);
    }

    /**
     * Retourne des critères empaquetés identiques à ceux donnés, mais avec l'heure de départ donnée
     * @param criteria critères
     * @param depMins1 heure de départ (son complément) donnée
     * @return les critères avec l'heure de départ donnée.
     */
    public static long withDepMins(long criteria, int depMins1){
        return (criteria | ((long) depMins1 << 51L));
    }

    /**
     * Ajoute un changement à un triplet de critère
     * @param criteria critère
     * @return le triplet de critère avec un changement de plus
     */
    public static long withAdditionalChange(long criteria){
        return criteria + (1L << 32) ;
    }

    /**
     * Insère une charge utile dans un triplet de critère
     * @param criteria critère
     * @param payload1 charge utile
     * @return le critère (long) avec la charge utile.
     */
    public static long withPayload(long criteria, int payload1){
        return (criteria & 0xFFFFFFFF00000000L) | (Integer.toUnsignedLong(payload1));
    }




}
