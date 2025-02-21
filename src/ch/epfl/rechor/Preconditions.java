package ch.epfl.rechor;

/**
 * Préconditions
 * @author Yoann Salamin (390522)
 */
public final class Preconditions {
    private Preconditions(){}

    /**
     * Vérifie que le paramètre est true
     * @param shouldBeTrue (paramètre dont on évalue la valeur de vérité)
     * @throws IllegalArgumentException (si le paramètre est false)
     */
    public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
