package ch.epfl.rechor.journey;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * Classe qui représente une frontière de Pareto de critères d'optimisation
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class ParetoFront {

    // tuples de la frontière stockée sous forme empaquetée
    private final long[] packed_criterias;

    /**
     * Attribut qui contient une frontière de pareto vide
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    // le constructeur doit être privé, car les instances sont constitutes par le bâtisseur
    private ParetoFront(long[] packed_criterias){

        // il ne faut pas copier les critères
        this.packed_criterias = packed_criterias;
        // TODO La méthode build du builder  doit garantir l'immuabilité...
    }

    /**
     * Retourne la taille de la frontière de Pareto
     * @return la taille (int)
     */
    public int size() {

        return packed_criterias.length;
    }

    /**
     * Retourne les critères d'optimisation empaquetés dont l'heure d'arrivée et le
     * nombre de changements sont ceux donnés
     * @param arrMins entier représentant le nombre de minutes passées depuis minuit
     * @param changes entier représentant le nombre de changements
     * @return le critère correspondant (long)
     */
    public long get(int arrMins, int changes){

        // On itère sur tous les critères de notre liste
        for (long pc : packed_criterias){
            // si l'un est identique aux params, on le retourne
            if (PackedCriteria.arrMins(pc) == arrMins && PackedCriteria.changes(pc) == changes) {
                return pc;
            }
        }

        // si on sort de la boucle sans retourner une valeur on lance une erreure
        throw new NoSuchElementException("Aucun long ne contient ces données");
    }

    /**
     * Appelle la méthode accept de l'action de type LongConsumer donnée avec chacun des critères de la frontière
     * @param action action
     */
    public void forEach(LongConsumer action){

        // on itère sur tous les critères de notre liste
        for (long packed_criteria : packed_criterias) {

            // on appelle la méthode accept de LongConsumer
            action.accept(packed_criteria);
        }
    }

    /**
     * Redefinition de la méthode toString sur ParetoFront qui
     * retourne une representation textuelle de la frontière de Pareto
     * @return la chaîne de caractère décrivant au mieux la classe
     */
    public String toString(){

        // Il n'y a pas de spécification, mais il faut que ce soit
        // aussi lisible que possible

        StringBuilder s = new StringBuilder();

        for (long pc : packed_criterias){

            // Montrer l'heure de départ si elle est présente
            if (PackedCriteria.hasDepMins(pc)) {
                s
                        .append("Heure de départ : ")
                        .append(PackedCriteria.depMins(pc))
                        .append("\n ")
                ;
            }

            // Montrer les autres infos dans tous les cas
            s
                    .append("Heure d'arrivée : ")
                    .append(PackedCriteria.arrMins(pc))
                    .append("\n ")
                    .append("Changements : ")
                    .append(PackedCriteria.changes(pc));
        }

        return s.toString();
    }

    /**
     * Classe qui représente un bâtisseur de frontière de Pareto
     */
    public final static class Builder {

        // TODO à la fin voir ce qu'on peut evtl mettre en final

        // Tableau de type long qui contient les tuples
        // en cours de construction
        private long[] arrayInConstruction;
        private int effectiveSize;

        // capacité initiale du tableau de pareto
        private final int INITIAL_CAPACITY = 2;

        // TODO voir si cette variable peut rester local
        private int capacity;

        /**
         * Constructeur par défaut qui retourne un bâtisseur
         * dont la frontière en cours de construction est vide
         */
        public Builder() {

            // on remet la capacité initiale
            capacity = INITIAL_CAPACITY;

            // on crée juste un tableau vide
            this.arrayInConstruction = new long[capacity];

            // la taille effective est nulle par défaut
            this.effectiveSize = 0;
        }

        /**
         * Constructeur de copie qui retourne un nouveau bâtisseur
         * avec les mêmes attributs que celui reçu en argument
         * @param that bâtisseur à copier
         */
        public Builder(Builder that) {
            this.arrayInConstruction = that.arrayInConstruction.clone();
            this.effectiveSize = that.effectiveSize;
        }

        /**
         * Fonction qui retourne true si le tableau en cours de construction est vide, false sinon
         * @return vrai si le tableau en cours de construction est vide
         */
        public boolean isEmpty() {

            // On contrôle cela avec la valeur d' effectiveSize
            // Attention de pas faire ce test avec tableau.length car cela ne
            // représente pas la taille effective
            return effectiveSize == 0;
        }

        /**
         * Fonction qui vide la frontière en cours de construction en supprimant tous ses éléments
         * @return l'instance courante du bâtisseur
         */
        public Builder clear() {

            // TODO on répète le code du constructeur voir si il y a une autre solution

            // on remet la capacité initiale
            capacity = INITIAL_CAPACITY;

            // on réinitialise les instances de classe
            // donc on refait comme dans le constructeur
            this.arrayInConstruction = new long[capacity];
            this.effectiveSize = 0;

            // on renvoi l'instance nettoyée
            return this;
        }


        /**
         * Ajoute à la frontière le tuple de critères empaquetés donné
         * @param packedTuple tuple de critère empaqueté
         * @return le builder mis à jour
         */
        public Builder add(long packedTuple) {

            // variable pour stocker la position d'insertion que l'on va trouver
            // on l'initialise à -1 pour montrer que la variable n'a pas encore une valeur
            // correcte
            int insertionPosition = -1;

            for (int i = 0; i < effectiveSize; i++) {

                // on enlève le payload avec une variable temporaire pour éviter les problèmes
                // de comparaison
                long packedTupleWithoutPayload = PackedCriteria.withPayload(packedTuple, 0);
                long elementToCompareWithoutPayload = PackedCriteria.withPayload(arrayInConstruction[i], 0);

                // on vérifie si le tuple à ajouter se fait dominer
                if (PackedCriteria.dominatesOrIsEqual(elementToCompareWithoutPayload, packedTupleWithoutPayload)) {
                    // si c'est le cas on ne change rien à la fontière
                    // actuelle et on sort de la fonction
                    return this;
                }

                // on cherche le premier élément supérieur (dans l'ordre lexicographique) à celui à insérer
                if (packedTupleWithoutPayload < elementToCompareWithoutPayload) {

                    // si la condition est validée, on stock l'index trouvé dans la
                    // variable de position d'insertion
                    insertionPosition = i;

                    // on sort de la boucle
                    break;
                }
            }

            // si aucune valeur n'a été assignée cela veut dire que
            // la position dd'insertion doit être tout à la fin après le dernier
            // élément
            if (insertionPosition == -1) {
                insertionPosition = effectiveSize;
            }



            int dst = insertionPosition;
            for (int src = insertionPosition; src < arrayInConstruction.length; src += 1) {
                if (PackedCriteria.dominatesOrIsEqual(packedTuple, arrayInConstruction[src])) {
                    continue;
                }
                if (dst != src) {
                    arrayInConstruction[dst] = arrayInConstruction[src];
                }

                dst += 1;
            }

            // Vérifier qu'il y a de la place, et augmenter la taille sinon
            if (effectiveSize == arrayInConstruction.length){
                this.capacity *= 2;
                long[] newArrayInConstruction = new long[capacity];
                System.arraycopy(arrayInConstruction, 0,newArrayInConstruction, 0, effectiveSize);
                arrayInConstruction = newArrayInConstruction;
            }
            // On insère enfin le tuple à la bonne position
            arrayInConstruction[insertionPosition] = packedTuple;

            // On oublie pas de mettre à jour la taille occupée
            effectiveSize ++;
            return this;
        }

        /**
         * Surcharge de add
         * @param arrMins heure d'arrivée en minute
         * @param changes nombre de changement (int)
         * @param payload charge utile
         * @return le builder
         */
        public Builder add(int arrMins, int changes, int payload) {
            long packedTuple = PackedCriteria.pack(arrMins, changes, payload);
            return add(packedTuple);
        }

        /**
         * Ajoute à la frontière tous les tuples
         * présents dans la frontière en cours de construction par le bâtisseur donné
         * @param that autre builder
         * @return builder actuel
         */
        public Builder addAll(Builder that) {

            // TODO essaie de ne pas build si on arrive à résoudre le problème de forEach
            // il faut d'abord build avant d'appliquer le forEach sinon
            // on va itérer sur des valeurs nulles du tableau qu'on ne veut pas
            that.forEach(value -> this.add(value));
            return this;
        }

        /**
         * Retourne vrai si et seulement si la totalité des tuples de la frontière donnée,
         * une fois que leur heure de départ a été fixée sur celle donnée,
         * sont dominés par au moins un tuple du récepteur
         * @param that frontière donnée
         * @param depMins minutes de départ à partir desquelles on va se caler pour comparer la dominance
         * @return (booléen) indiquant des deux
         */
        boolean fullyDominates(Builder that, int depMins){

            // Pour chacun des tuples de that
            for (long thatValue : that.arrayInConstruction) {

                boolean hasBeenDominated = false;

                // On prend sa version modifiée selon depMins donné
                long modifiedValue = PackedCriteria.withDepMins(thatValue, depMins);

                // On la compare avec tous nos tuples de this
                for (long thisValue : this.arrayInConstruction){

                    // Si this domine that
                    if(PackedCriteria.dominatesOrIsEqual(thisValue, modifiedValue)){

                        // On modifie la variable
                        hasBeenDominated = true;
                        break;
                    }
                }

                // Si un that n'a été dominé par aucun this
                if (!hasBeenDominated) {return false;}

            }

            // Si aucun that ne s'est pas fait dominer, c'est que tous ceux
            // de that se font dominés par au moins un de true
            // C'est donc que this domine that, et faut retourner true
            return true;
        }

        /**
         * Appelle la méthode accept de l'action de type LongConsumer donnée avec chacun des critères de la frontière
         * @param action action
         */
        public void forEach(LongConsumer action) {

            // attention on ne doit pas parcourir tout l'array en construction
            // car il a des 0 en trop, il faut donc s'arrêter à la taille effective
            for (int i = 0; i < this.effectiveSize; ++i) {

                // et on appelle l'action sur chaque tuple
                action.accept(arrayInConstruction[i]);
            }

        }

        /**
         * Fonction qui retourne la frontière de Pareto en cours de construction par ce bâtisseur
         * @return une instance de ParetoFront avec les paramètres du batisseur
         */
        public ParetoFront build() {

            // la dernière étape est de récréer un tableau final qui a
            // exactement la bonne taille
            long[] finalPackedCriteriaArray = new long[effectiveSize];


            // on part du début dans les deux cas
            int srcPos = 0;
            int desPos = 0;

            // on fait la copie de notre ancien tableau dans le nouveau
            // tableau qui a maitenant la bonne taille
            System.arraycopy(arrayInConstruction, srcPos, finalPackedCriteriaArray, desPos, effectiveSize);


            // finalement on crée notre instance
            ParetoFront paretoFront = new ParetoFront(
                    finalPackedCriteriaArray
            );

            // et on la retourne
            return  paretoFront;
        }

    }

}
