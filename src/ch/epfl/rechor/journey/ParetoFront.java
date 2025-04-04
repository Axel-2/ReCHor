package ch.epfl.rechor.journey;

import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * Classe qui représente une frontière de Pareto de critères d'optimisation
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class ParetoFront {

    // tuples de la frontière stockée sous forme empaquetée
    private final long[] packedCriterias;

    /**
     * Attribut qui contient une frontière de pareto vide
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Constructeur privé, qui stock les critères empaquetés sans les copier
     * @param packedCriterias (critères empaquetés)
     */
    private ParetoFront(long[] packedCriterias) {

        // il ne faut pas copier les critères
        this.packedCriterias = packedCriterias;
    }

    /**
     * Retourne la taille de la frontière de Pareto
     * @return la taille (int)
     */
    public int size() {

        return packedCriterias.length;
    }

    /**
     * Retourne les critères d'optimisation empaquetés dont l'heure d'arrivée et le
     * nombre de changements sont ceux donnés
     * @param arrMins entier représentant le nombre de minutes passées depuis minuit
     * @param changes entier représentant le nombre de changements
     * @return le critère correspondant (long)
     */
    public long get(int arrMins, int changes) {

        // On itère sur tous les critères de notre liste
        for (long pc : packedCriterias) {
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
    public void forEach(LongConsumer action) {

        // On itère sur tous les critères de notre liste
        for (long packed_criteria : packedCriterias) {

            // on appelle la méthode accept de LongConsumer
            action.accept(packed_criteria);
        }
    }

    /**
     * Redefinition de la méthode toString sur ParetoFront qui
     * retourne une representation textuelle de la frontière de Pareto
     * @return la chaîne de caractère décrivant au mieux la classe
     */
    public String toString() {

        // Il n'y a pas de spécification, mais il faut que ce soit
        // aussi lisible que possible

        StringBuilder s = new StringBuilder();

        for (long pc : packedCriterias){

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

        return s.append("\n\n").toString();
    }

    /**
     * Classe qui représente un bâtisseur de frontière de Pareto
     */
    public final static class Builder {

        // Tableau de type long qui contient les tuples
        // en cours de construction
        private long[] arrayInConstruction;
        private int effectiveSize;

        // Capacité initiale du tableau de pareto
        private static final int INITIAL_CAPACITY = 2;

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
            this.capacity = that.capacity;
        }

        /**
         * Fonction qui retourne true si le tableau en cours de construction est vide, false sinon
         * @return vrai si le tableau en cours de construction est vide
         */
        public boolean isEmpty() {

            // On contrôle cela avec la valeur d'effectiveSize
            // Attention de ne pas faire ce test avec tableau.length car cela ne
            // représente pas la taille effective
            return effectiveSize == 0;
        }

        /**
         * Fonction qui vide la frontière en cours de construction en supprimant tous ses éléments
         * @return l'instance courante du bâtisseur
         */
        public Builder clear() {

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

            // ------------- 1) On cherche la position d'insertion ----------------
            int insertionPosition = -1;

            for (int i = 0; i < effectiveSize; i++) {

                // On les compare sans les payload
                long packedTupleWithoutPayload = PackedCriteria.withPayload(packedTuple, 0);
                long elementToCompareWithoutPayload = PackedCriteria.withPayload(arrayInConstruction[i], 0);

                // On check just que le tuple ne se fasse pas dominer, sinon ça ne sert à rien de l'ajouter
                if (PackedCriteria.dominatesOrIsEqual(elementToCompareWithoutPayload, packedTupleWithoutPayload))
                    return this;

                // On s'arrête d'itérer dès qu'un tuple est plus grand (dans l'ordre lexicographique)
                if (packedTupleWithoutPayload < elementToCompareWithoutPayload) {
                    insertionPosition = i;
                    break;
                }
            }

            // Si aucune valeur n'a été assignée cela veut dire qu'on est à la fin
            // et donc on met à jour effectiveSize
            if (insertionPosition == -1) {
                insertionPosition = effectiveSize;
            }


            // ------------- 2) Suppression de tous les tuples dominés par le nouveau ----------------
            // Tous ceux de gauches seront gardés, c'est à droite que l'on va devoir trier
            // On peut donc déjà initialiser la variable à une certaine valeur
            int nbOfConservatedValue = insertionPosition;

            // On itère sur tous les critères à droite de la position d'insertion
            for (int src = insertionPosition; src < effectiveSize; src += 1) {

                // Si le critère regardé se fait dominé par celui fraîchement ajouté, on ne le garde pas
                if (PackedCriteria.dominatesOrIsEqual(packedTuple, arrayInConstruction[src])) {
                    continue;
                }

                // Si le critère regardé ne se fait pas dominé par celui fraîchement ajouté, on le place au
                // bon endroit, en mettant à jour la taille des valeurs conservées (utile plus tard)
                if (nbOfConservatedValue != src) {
                    arrayInConstruction[nbOfConservatedValue] = arrayInConstruction[src];
                }

                nbOfConservatedValue += 1;
            }

            // On met à jour la taille effective du tableau pour quil englobe uniquement les tuples non dominés
            effectiveSize = nbOfConservatedValue;


            // ------------ 3) On augmente la taille si nécessaire ---------------
            if (effectiveSize == arrayInConstruction.length){
                this.capacity *= 2;
                long[] newArrayInConstruction = new long[capacity];
                System.arraycopy(arrayInConstruction, 0,newArrayInConstruction, 0, effectiveSize);
                arrayInConstruction = newArrayInConstruction;
            }


            // ------------ 4) On crée de la place pour ajouter notre tuple
            System.arraycopy(arrayInConstruction, insertionPosition, arrayInConstruction, insertionPosition + 1, effectiveSize - insertionPosition);
            arrayInConstruction[insertionPosition] = packedTuple;
            effectiveSize++;

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

            // il faut d'abord build avant d'appliquer le forEach sinon
            // on va itérer sur des valeurs nulles du tableau qu'on ne veut pas
            that.forEach(this::add);
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
        public boolean fullyDominates(Builder that, int depMins){

            // Pour chacun des tuples de that
            for (int i = 0; i < that.effectiveSize; i++) {

                boolean hasBeenDominated = false;

                // On prend sa version modifiée selon depMins donné
                long modifiedValue = PackedCriteria.withDepMins(that.arrayInConstruction[i], depMins);

                // On la compare avec tous nos tuples de this
                for (int j = 0; j < this.effectiveSize; j++){

                    // Si this domine that
                    if (PackedCriteria.dominatesOrIsEqual(this.arrayInConstruction[j], modifiedValue)) {

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
            // C'est donc que this domine that, et il faut retourner vrai
            return true;
        }

        /**
         * Appelle la méthode accept de l'action de type LongConsumer donnée avec chacun des critères de la frontière
         * @param action action
         */
        public void forEach(LongConsumer action) {

            for (int i = 0; i < this.effectiveSize; ++i) {

                action.accept(arrayInConstruction[i]);
            }

        }

        /**
         * Fonction qui retourne la frontière de Pareto en cours de construction par ce bâtisseur
         * @return une instance de ParetoFront avec les paramètres du batisseur
         */
        public ParetoFront build() {

            // La dernière étape est de récréer un tableau final qui a exactement la bonne taille
            long[] finalPackedCriteriaArray = new long[effectiveSize];


            // On part du début dans les deux cas
            int srcPos = 0;
            int desPos = 0;

            // On fait la copie de notre ancien tableau dans le nouveau
            // tableau qui a maintenant la bonne taille
            System.arraycopy(arrayInConstruction, srcPos, finalPackedCriteriaArray, desPos, effectiveSize);

            // Création de l'instance et on la retourne
            return new ParetoFront(
                    finalPackedCriteriaArray
            );
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();

            for (long pc : arrayInConstruction) {
                sb.append(PackedCriteria.arrMins(pc))
                        .append("|")
                        .append(PackedCriteria.changes(pc))
                        .append("  ");
            }

            return sb.toString();
        }

    }

}
