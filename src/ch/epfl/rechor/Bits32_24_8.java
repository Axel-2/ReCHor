package ch.epfl.rechor;


public final class Bits32_24_8 {

    // Pour rendre la classe non instantiable
    private Bits32_24_8() {}

    public static int pack(int bits24, int bits8) {
        // Check si les bits respectent la taille voulue
        Preconditions.checkArgument((bits8 >> 8 == 0));
        Preconditions.checkArgument((bits24 >> 24 == 0));

        return (bits24 << 8) | bits8;
    }

    public static int unpack24(int bits32) {
        // On décalle les 24 bits de poids fort de 8 bits
        // vers la droite pour écraser les 8 premiers bits
        // et n'avoir plus que 24 bits
        // on utilise >>> pour ne pas préserver le signe
        // TODO masque nécessaire ?
        return (bits32 >>> 8) & 0xFFFFFF;
    }

    public static int unpack8(int bits32) {
        // On récupère les 8 bits de poids faible avec le masque OxFF
        return (bits32 & 0xFF);
    }
}
