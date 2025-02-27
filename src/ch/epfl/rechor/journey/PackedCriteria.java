package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * 3 critères empaquetés dans un long
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class PackedCriteria {

    // Pour rendre la classe non instantiable
    private PackedCriteria() {}

    // TODO commentaires
    /**
     * Pack 3 valeurs aux endroits que l'on veut dans un long
     * @param arrMins heure d'arrivées (en minute)
     * @param changes nombre de changements
     * @param payload charge utile
     * @return le long représentant l'empaquetage
     */
    public static long pack(int arrMins, int changes, int payload) {

        // arrMin est exprimé en minutes écoulées depuis minuit
        // On teste si l'heure est valide
        Preconditions.checkArgument(-240 <= arrMins && arrMins < 2880);

        // payload non signé pour éviter les erreurs
        // d'extensions de signe
        long unsignedPayload = Integer.toUnsignedLong(payload);

        // On translate pour garantir des valeurs positives
        arrMins += 240;

        // TODO Ici changes peut être nul non ? : je cite mais tu peux supprimer après
        // "nous reste maintenant à considérer le dernier critère d'optimisation,
        // le nombre de changements. Il est clair que celui-ci est un entier positif ou nul,
        // et on peut raisonnablement faire l'hypothèse qu'il est inférieur à 128. Dès lors,
        // 7 bits suffisent à le représenter ".
        // On test si les nombres de changements tiennent sur 7 bits
        // 127 est le nombre max sur 7 bits
        Preconditions.checkArgument(changes > 0 && changes <= 127);

        // Long initial
        long resultLong = 0L;

        // ajout de arrMins
        long arrMinShifted = ((long) arrMins) << 39;
        resultLong = resultLong | arrMinShifted;

        // Ajout du changes
        long changesShifted = ((long) changes) << 32;
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
        long bits51to62 = (criteria >>> 51) & 0xFFF;

        // retourne vrai si l'heure n'est pas nulle
        return bits51to62 != 0;
    }

    /**
     * Retourne l'heure de départ (en minutes après minuit) des critères empaquetés donnés.
     * @param criteria criteria un long représentant les critères empaquetés
     * @return l'heure de départ (en minutes après minuit)
     */
    public static int depMins(long criteria){

        Preconditions.checkArgument(hasDepMins(criteria));

        // retourne l'heure de départ (en minutes après minuit) des critères empaquetés donnés,
        // ou lève IllegalArgumentException si ces critères n'incluent pas une heure de départ,

        // Comme dans la fonction précédant, on récupère les bits
        // correspondants à l'heure de départ
        long bits51to62 = (criteria >>> 51) & 0xFFF;

        // On reverse le complément
        int depMins = 4095 - (int) bits51to62 ;

        // on enlève l'offset de 4h
        depMins -= 240;

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
        long bits39to50 = (criteria >>> 39) & 0xFFFL;

        // on convertis en int puis on enlève l'offset de 4h
        // pour avoir des minutes après minuit
        int arrMins = (int) bits39to50 - 240;

        return arrMins;
    }

    /**
     * Retourne le nombre de changements des critères empaquetés donnés.
     * @param criteria un long représentant les critères empaquetés
     * @return le nombre de changements
     */
    public static int changes(long criteria){
        // on shift de 32 bits et on prend seulement
        // les 7 bits de poids faible avec un masque
        // 0x7F correspond à 127 qui correspond à 7 bits de poids faible
        long bits32to38 = (criteria >>> 32) & 0x7F;

        // on retourne le résultat converti en int
        return (int) bits32to38;
    }

    /**
     * Retourne la «charge utile» associée aux critères empaquetés donnés.
     * @param criteria un long représentant les critères empaquetés
     * @return a charge utile
     */
    public static int payload(long criteria){

        // récupération des 32 bits de poids faible avec le masque 0xFFFFFFFFL
        // qui a 32 bits de poids faible

        return (int) (criteria & 0xFFFFFFFFL);
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

        // S'assure que les deux ont des heures de départ, lève une IEA sinon.
        Preconditions.checkArgument(hasDepMins(criteria1));
        Preconditions.checkArgument(hasDepMins(criteria2));

        // Pour que true soit retourné, il faut que les 3 critères soient supérieurs ou égaux au deuxième long
        // Rappel : on a pris le complément de l'heure de départ de manière à minimiser tous les critères.
        // Il faut donc que les trois critères du premier long soient <= les critères du deuxième.

        // 1) Heure de départ
        if(arrMins(criteria1) <= arrMins(criteria2)){
            // 2) Heure d'arrivée
            if (depMins(criteria1) <= depMins(criteria2)){
                // 3) Changements
                return changes(criteria1) <= changes(criteria2);
            }
        }
        return false;
    }

    /**
     * Retourne des critères empaquetés identiques à ceux donnés, mais sans heure de départ
     * @param criteria critères
     * @return critères sans heure de départ
     */
    public static long withoutDepMins(long criteria){
        // On masque notre long avec 1111_0000_0000_0000_1111_1111....... pour supprimer les 12 bits de l'heure de dép.
        return criteria & ~(0xFFFL << 51);
    }

    /**
     * Retourne des critères empaquetés identiques à ceux donnés, mais avec l'heure de départ donnée
     * @param criteria critères
     * @param depMins1 heure de départ (son complément) donnée
     * @return les critères avec l'heure de départ donnée.
     */
    public static long withDepMins(long criteria, int depMins1){

        depMins1 += 240;
        depMins1 = 4095 - depMins1;

        // On remplace les bits nuls actuels par ceux de l'heure de départ, à la bonne position
        return (criteria | ((long) depMins1 << 51L));
    }

    /**
     * Ajoute un changement à un triplet de critère
     * @param criteria critère
     * @return le triplet de critère avec un changement de plus
     */
    public static long withAdditionalChange(long criteria) {
        // Incrément de 1 le changement, en additionnant 1 au bon endroit
        return criteria + (1L << 32) ;
    }

    /**
     * Insère une charge utile dans un triplet de critère
     * @param criteria critère
     * @param payload1 charge utile
     * @return le critère (long) avec la charge utile.
     */
    public static long withPayload(long criteria, int payload1){

        // On nulifie les 32 bits de droite pour ensuite y mettre la charge utile sans extension de signe.
        return (criteria & 0xFFFFFFFF00000000L) | (Integer.toUnsignedLong(payload1));
    }




}
