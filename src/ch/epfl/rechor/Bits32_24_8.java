package ch.epfl.rechor;

/**
 * Classe maniant des valeurs représentées en 32 bits
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class Bits32_24_8 {


    // Définition des constantes pour les masques et le décalage

    // Masque pour extraire les 24 bits de poids fort
    private static final int MASK_24_BITS = 0xFFFFFF;
    // Masque pour extraire les 8 bits de poids faible
    private static final int MASK_8_BITS  = 0xFF;

    // Pour rendre la classe non instantiable
    private Bits32_24_8() {}

    /**
     * Retourne le vecteur de 32 bits dont les 24 bits de poids fort sont bits24 et les 8 bits
     * de poids faible sont bits8, ou lève une IllegalArgumentException si l'une des deux
     * valeurs nécessite plus de bits qu'elle ne devrait
     * @param bits24 24 bits de poids fort
     * @param bits8 de poids faible
     * @return l'empaquetage de ces 2 valeurs
     */
    public static int pack(int bits24, int bits8) {
        
        // Check si les bits respectent la taille voulue
        Preconditions.checkArgument((bits8 >> 8 == 0));
        Preconditions.checkArgument((bits24 >> 24 == 0));

        return (bits24 << 8) | bits8;
    }

    /**
     *  Retourne les 24 bits de poids fort du vecteur de 32 bits donné.
     * @param bits32 bits à modifier
     * @return l'int modifié
     */
    public static int unpack24(int bits32) {
        // On décale les 24 bits de poids fort de 8 bits
        // vers la droite pour écraser les 8 premiers bits
        // et n'avoir plus que 24 bits,
        // on utilise >>> pour ne pas préserver le signe
        return (bits32 >>> 8) & MASK_24_BITS;
    }

    /**
     * Retourne les 8 bits de poids faible du vecteur de 32 bits donné.
     * @param bits32 à modifier
     * @return l'int modifié.
     */
    public static int unpack8(int bits32) {
        // On récupère les 8 bits de poids faible avec le masque OxFF
        return (bits32 & MASK_8_BITS);
    }
}
