package ch.epfl.rechor;

public class PackedRange {

    // Pour rendre la classe non instantiable
    private PackedRange() {

    }

    public static int pack(int startInclusive, int endExclusive) {

        int intervalleSize = endExclusive - startInclusive;

        // On vérifie que l'iinterval tient sur 8 bits
        // càd qu'il doit <= 255 donc <= 0xFF
        Preconditions.checkArgument(intervalleSize <= 0xFF);

        // Ici on fait le même test mais cette fois ci avec 24 bits
        // pour la borne inférieure
        Preconditions.checkArgument(startInclusive  <= 0xFFFFFF);

        // La borne inférieur doit aussi être positive
        Preconditions.checkArgument(startInclusive >= 0);

        // TODO utiliser les fonctions déjà faites de Bits32_24_8

        // On met les 24 bits de la borne supérieure dans les 24 bits
        // de poids fort, donc comme il y a 32 bits dans un int on
        // shift de 8 bits à gauche
        startInclusive = startInclusive << 8;

        // Ensuite on pack les deux variables dans le même int avec un OR
        // ce qui est notre résulta final
        return startInclusive | intervalleSize;
    }

    public static int length(int interval) {
        // On récupère les 8 bits de poids faible
        // et c'est ici qu'est stocké la longueur
        return Bits32_24_8.unpack8(interval);
    }

    public static int startInclusive(int interval) {

        // On prend les 24 bits de poids fort
        return Bits32_24_8.unpack24(interval);
    }


    public static int endExclusive(int interval) {

        // On utilise les fonctions déjà codées
        int length = length(interval);
        int startInclusive = startInclusive(interval);

        int endExclusif = startInclusive + length;

        return endExclusif;
    }


}
