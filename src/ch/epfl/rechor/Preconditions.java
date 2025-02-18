package ch.epfl.rechor;


public final class Preconditions {
    private Preconditions(){};

    void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
