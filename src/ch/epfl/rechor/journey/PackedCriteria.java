package ch.epfl.rechor.journey;


import ch.epfl.rechor.Preconditions;

public class PackedCriteria {

    // Pour rendre la classe non instantiable
    private PackedCriteria() {}

    public static long pack(int arrMins, int changes, int payload) {

        // Il faut en tout premier translater les minutes de -240
        arrMins = arrMins - 240;

        // Ensuite on teste si l'heure est valide
        Preconditions.checkArgument(-240 <= arrMins && arrMins < 2880);

        // TODO changes < 0 nécessaire ou en trop ??
        // On test si les nombres de changements tiennent sur 7 bits
        // 127 est le nombre max sur 7 bits
        Preconditions.checkArgument(changes > 0 && changes <= 127);

        long initialLong = 0;

        initialLong = arrMins << 6;
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

    public static long withoutDepMins(long criteria){
        return 0;
    }

    public static long withDepMins(long criteria, int depMins1){
        return 0;
    }

    public static long withAdditionalChange(long criteria){
        return 0;
    }

    public static long withPayload(long criteria, int payload1){
        return 0;
    }




}
