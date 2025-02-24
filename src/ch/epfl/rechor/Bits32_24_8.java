package ch.epfl.rechor;


public final class Bits32_24_8 {

    // Pour rendre la classe non instantiable
    private Bits32_24_8() {}

    public static int pack(int bits24, int bits8){
        // Check si les bits respectent la taille voulue
        Preconditions.checkArgument((bits8 >> 8 == 0));
        Preconditions.checkArgument((bits24 >> 24 == 0));

        return (bits24 << 8) | bits8;
    }

    public static int unpack24(int bits32){
        return (bits32 >>> 8) & 0xFFFFFF;
    }

    public static int unpack8(int bits32){
        return (bits32 & 0xFF);
    }
}
