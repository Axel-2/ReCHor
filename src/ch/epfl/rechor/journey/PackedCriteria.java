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
