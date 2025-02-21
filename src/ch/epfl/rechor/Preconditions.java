package ch.epfl.rechor;

/**
 * Préconditions
 * @author Yoann Salamin (390522)
 */
public final class Preconditions {
    private Preconditions(){}

    public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
