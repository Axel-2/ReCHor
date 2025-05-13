package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * 3 critères empaquetés dans un long
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class PackedCriteria {

    // Nombres magiques
    private static final int MIN_CHANGES = 0;
    private static final int MAX_CHANGES = 127;
    private static final int MIN_ARR_MINS = -240;
    private static final int MAX_ARR_MINS = 2880;
    private static final int OFFSET = 240;

    // Constantes pour les shifts
    private static final int SHIFT_ARR_MINS = 39;
    private static final int SHIFT_CHANGES = 32;
    private static final int SHIFT_DEP_MINS = 51;

    // Constantes pour les masques
    private static final long MASK_7_BITS = 0x7F;
    private static final long MASK_12_BITS = 0xFFFL;
    private static final long MASK_32_BITS = 0xFFFFFFFFL;
    private static final long MASK_UPPER_32_BITS = 0xFFFFFFFF00000000L;

    // Constante de conversion pour les minutes
    private static final int COMPLEMENT_CONSTANT = 4095;

    // Pour rendre la classe non instantiable
    private PackedCriteria() {}

    /**
     * Pack 3 valeurs aux endroits que l'on veut dans un long
     * @param arrMins heure d'arrivées (en minute)
     * @param changes nombre de changements
     * @param payload charge utile
     * @return le long représentant l'empaquetage
     */
    public static long pack(int arrMins, int changes, int payload) {

        // On vérifie que "changes" doit est sur 7 bits
        Preconditions.checkArgument(MIN_CHANGES <= changes && changes <= MAX_CHANGES);

        // arrMin est exprimé en minutes écoulées depuis minuit
        // On teste si l'heure est valide
        Preconditions.checkArgument(MIN_ARR_MINS <= arrMins && arrMins < MAX_ARR_MINS);

        // Le payload non signé pour éviter les erreurs
        // d'extensions de signe
        long unsignedPayload = Integer.toUnsignedLong(payload);

        // On translate l'heure d'arrivée avec un offset prédéfinie
        // pour garantir des valeurs positives dans le nombre
        arrMins += OFFSET;

        // Long avec valeur initiale par défaut
        long resultLong = 0L;

        // ajout de l'heure de départ
        long arrMinShifted = ((long) arrMins) << SHIFT_ARR_MINS;
        resultLong = resultLong | arrMinShifted;

        // Ajout des changements
        long changesShifted = ((long) changes) << SHIFT_CHANGES;
        resultLong = changesShifted | resultLong;

        // On ajoute le payload non signé
        resultLong =  unsignedPayload | resultLong;

        return resultLong;
    }

    /**
     * Fonction qui retourne vrai si les critères donnés en paramètres
     * contiennent une heure de départ
     * @param criteria  un long représentant les critères empaquetés
     * @return vrai si criteria contient une heure de départ, faux sinon
     */
    public static boolean hasDepMins(long criteria) {

        // On veut récupérer l'heure de départ qui est
        // dans les bits 51 à 62
        // On shift alors de 51 bits vers la droite puis
        // on masque avec 0xFFF qui représente les 12 premiers bits
        long bits51to62 = (criteria >>> SHIFT_DEP_MINS) & MASK_12_BITS;

        // retourne vrai si l'heure n'est pas nulle
        return bits51to62 != 0;
    }

    /**
     * Retourne l'heure de départ (en minutes après minuit) des critères empaquetés donnés.
     * @param criteria criteria un long représentant les critères empaquetés
     * @return l'heure de départ (en minutes après minuit)
     */
    public static int depMins(long criteria) {

        Preconditions.checkArgument(hasDepMins(criteria));

        // retourne l'heure de départ (en minutes après minuit) des critères empaquetés donnés,
        // ou lève IllegalArgumentException si ces critères n'incluent pas une heure de départ,

        // Comme dans la fonction précédant, on récupère les bits
        // correspondants à l'heure de départ
        long bits51to62 = (criteria >>> SHIFT_DEP_MINS) & MASK_12_BITS;

        // On reverse le complément
        int depMins = COMPLEMENT_CONSTANT - (int) bits51to62 ;

        // on enlève l'offset de 4h
        depMins -= OFFSET;

        return depMins;
    }

    /**
     * Retourne l'heure d'arrivée (en minutes après minuit) des critères empaquetés donnés.
     * @param criteria un long représentant les critères empaquetés
     * @return l'heure d'arrivée (en minutes après minuit)
     */
    public static int arrMins(long criteria) {
        // retourne l'heure d'arrivée (en minutes après minuit) des critères empaquetés donnés

        // Récupération des bits 39 à 50 avec shift de 39
        // puis masque des 12 premiers bits
        long bits39to50 = (criteria >>> SHIFT_ARR_MINS) & MASK_12_BITS;

        // Il faut convertir en int puis on enlève l'offset de 4h
        // pour avoir des minutes après minuit
        return (int) bits39to50 - OFFSET;
    }

    /**
     * Retourne le nombre de changements des critères empaquetés donnés.
     * @param criteria un long représentant les critères empaquetés
     * @return le nombre de changements
     */
    public static int changes(long criteria) {

        // on shift de 32 bits et on prend seulement
        // les 7 bits de poids faible avec un masque
        // 0x7F ce qui correspond à 127 qui correspond à 7 bits de poids faible
        long bits32to38 = (criteria >>> SHIFT_CHANGES) & MASK_7_BITS;

        // on retourne le résultat converti en int
        return (int) bits32to38;
    }

    /**
     * Retourne la charge utile associée aux critères empaquetés donnés.
     * @param criteria un long représentant les critères empaquetés
     * @return a charge utile
     */
    public static int payload(long criteria){

        // récupération des 32 bits de poids faible avec le masque 0xFFFFFFFFL
        // qui a 32 bits de poids faible
        return (int) (criteria & MASK_32_BITS);
    }

    /**
     * Retourne vrai si et seulement si les premiers critères empaquetés dominent ou sont égaux
     * aux seconds.
     * La domination est définie de sorte que chaque champ des premiers critères
     * est supérieur ou égal au champ correspondant des seconds critères.
     * @param criteria1 un long représentant le premier ensemble de critères empaquetés
     * @param criteria2 un long représentant le second ensemble de critères empaquetés
     * @return vrai si criteria1 domine ou est égal à criteria2, faux sinon
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        // S'assure que, soit les deux ont une heure de départ, soit aucun des deux
        Preconditions.checkArgument((hasDepMins(criteria1) && hasDepMins(criteria2)) ||
                (!hasDepMins(criteria1) && !hasDepMins(criteria2)));

        // Aucun des deux n'a une heure de départ
        if (!hasDepMins(criteria1) && !hasDepMins(criteria2)){
            return arrMins(criteria1) <= arrMins(criteria2) &&
                    changes(criteria1) <= changes(criteria2);
        }

        // Les deux ont une heure de départ
        else{
            // Pour dominer, TOUS les critères doivent être meilleurs ou égaux
            return arrMins(criteria1) <= arrMins(criteria2) &&
                    depMins(criteria1) >= depMins(criteria2) &&
                    changes(criteria1) <= changes(criteria2);
        }
    }

    /**
     * Retourne des critères empaquetés identiques à ceux donnés, mais sans heure de départ
     * @param criteria critères
     * @return critères sans heure de départ
     */
    public static long withoutDepMins(long criteria){
        // On masque notre long avec 1111_0000_0000_0000_1111_1111....... pour supprimer les 12 bits de l'heure de dép.
        return criteria & ~(MASK_12_BITS << SHIFT_DEP_MINS);
    }

    /**
     * Retourne des critères empaquetés identiques à ceux donnés, mais avec l'heure de départ donnée
     * @param criteria critères
     * @param depMins heure de départ (son complément) donnée
     * @return les critères avec l'heure de départ donnée.
     */
    public static long withDepMins(long criteria, int depMins) {

        // depMins1 est exprimé en minutes écoulées depuis minuit
        // On teste si l'heure est valide
        Preconditions.checkArgument(MIN_ARR_MINS <= depMins && depMins < MAX_ARR_MINS);

        depMins += OFFSET;
        depMins = COMPLEMENT_CONSTANT - depMins;


        // Création du masque pour les 12 bits réservés à l'heure de départ.
        // 0xFFF correspond à 12 bits à 1, on décale ce masque de 51 bits vers la gauche
        // pour le positionner correctement dans la structure des critères.
        long mask = MASK_12_BITS << SHIFT_DEP_MINS;


        // Efface la partie des critères correspondant à l'heure de départ.
        // L'opération & avec l'inverse du masque (~mask) met à zéro ces 12 bits.
        long clearedCriteria = criteria & ~mask;

        // Prépare la nouvelle valeur de l'heure de départ en décalant la valeur ajustée
        // de 51 bits vers la gauche pour qu'elle occupe la bonne position.
        long newDepMins = ((long) depMins) << SHIFT_DEP_MINS;

        // Combine les critères nettoyés avec la nouvelle valeur de l'heure de départ.
        return clearedCriteria | newDepMins;
    }


    /**
     * Ajoute un changement à un triplet de critère
     * @param criteria critère
     * @return le triplet de critère avec un changement de plus
     */
    public static long withAdditionalChange(long criteria) {
        // Cette fonction incrément de 1 le changement, en additionnant 1 au bon endroit

        // (1L << 32) place le bit 1 à la position 32.
        long changeIncrement = 1L << SHIFT_CHANGES;

        return criteria + changeIncrement;
    }

    /**
     * Insère une charge utile dans un triplet de critère
     * @param criteria critère
     * @param payload1 charge utile
     * @return le critère (long) avec la charge utile.
     */
    public static long withPayload(long criteria, int payload1){
        // Création du masque pour conserver uniquement les 32 bits de gauche
        // Le masque 0xFFFFFFFF00000000L a des 1 sur les bits 32 à 63 et 0 sur les bits 0 à 31.
        long mask = MASK_UPPER_32_BITS;

        // Efface les 32 bits de droite du critère en appliquant le masque.
        long clearedCriteria = criteria & mask;

        long unsignedPayload = Integer.toUnsignedLong(payload1);

        // Combine le critère nettoyé avec le payload
        return clearedCriteria | unsignedPayload;
    }

}
