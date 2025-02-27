package ch.epfl.rechor;

/**
 * Intervalle empaqueté
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class PackedRange {

    // Pour rendre la classe non instantiable
    private PackedRange() {

    }

    /**
     * Crée un intervalle empaqueté dans un int à partir de ses deux bornes
     * @param startInclusive borne inférieure (incluse)
     * @param endExclusive borne supérieure (éxclue)
     * @return la valeur de type int représentant l'intervalle d'entiers allant de
     * startInclusive (inclus) à endExclusive (exclu)
     */
    public static int pack(int startInclusive, int endExclusive) {

        int intervalleSize = endExclusive - startInclusive;

        // On vérifie que l'interval tient sur 8 bits
        // càd qu'il doit <= 255 donc <= 0xFF
        Preconditions.checkArgument(intervalleSize <= 0xFF && intervalleSize >= 0);

        // Ici on fait le même test mais cette fois ci avec 24 bits
        // pour la borne inférieure
        Preconditions.checkArgument(startInclusive  <= 0xFFFFFF);

        // leur borne inférieure est toujours positive ou nulle, et plus petite que 224,
        Preconditions.checkArgument(startInclusive >= 0);

        // on pack les deux variables dans le même int
        // ce qui est notre résulta final
        int packedInt =  Bits32_24_8.pack(startInclusive, intervalleSize);

        return packedInt;
    }

    public static int length(int interval) {
        // On récupère les 8 bits de poids faible
        // et c'est ici qu'est stocké la longueur
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Retourne la borne inférieur de l'intervalle en paramètre
     * @param interval l'intervalle
     * @return la borne inférieur de l'intervalle en paramètre
     */
    public static int startInclusive(int interval) {

        // On prend les 24 bits de poids fort
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Retourne la borne supérieure de l'intervalle en paramètre
     * @param interval l'intervalle
     * @return la borne supérieure de l'intervalle en paramètre
     */
    public static int endExclusive(int interval) {

        // On utilise les fonctions déjà codées
        int length = length(interval);
        int startInclusive = startInclusive(interval);

        int endExclusif = startInclusive + length;

        return endExclusif;
    }


}
