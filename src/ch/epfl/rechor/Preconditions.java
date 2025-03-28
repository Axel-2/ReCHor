package ch.epfl.rechor;

/**
 * Classe utilitaire qui offre une méthode de validation d'argument
 * @author Yoann Salamin (390522)
 */
public final class Preconditions {

    // pour rendre la classe non instanciable
    private Preconditions(){}

    /**
     * Vérifie que le paramètre est vrai
     * @param shouldBeTrue (paramètre dont on évalue la valeur de vérité)
     * @throws IllegalArgumentException (si le paramètre est false)
     */
    public static void checkArgument(boolean shouldBeTrue){

        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
