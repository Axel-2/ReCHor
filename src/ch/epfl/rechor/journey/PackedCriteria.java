package ch.epfl.rechor.journey;


public class PackedCriteria {

    // Pour rendre la classe non instantiable
    private PackedCriteria() {}

    public static long pack(int arrMins, int changes, int payload){
        return 0;
    }

    public static boolean hasDepMins(long criteria){
        return false;
    }

    public static int depMins(long criteria){
        return 0;
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
