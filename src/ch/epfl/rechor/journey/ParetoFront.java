package ch.epfl.rechor.journey;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * Frontière de Pareto
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class ParetoFront {

    private long[] packed_criterias;
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    private ParetoFront(long[] packed_criterias){
        this.packed_criterias = packed_criterias;
        // TODO La méthode build  doit garantir ...
    }

    /**
     * Retourne la taille de la frontière de Pareto
     * @return la taille (int)
     */
    public int size(){
        return packed_criterias.length;
    }

    /**
     * Retourne les critères d'optimisation empaquetés dont l'heure d'arrivée et le nombre de changements sont ceux donnés
     * @param arrMins entier représentant le nombre de minutes passées depuis minuit
     * @param changes entier représentant le nombre de changements
     * @return le critère correspondant (long)
     */
    public long get(int arrMins, int changes){

        for (long pc : packed_criterias){
            if(PackedCriteria.arrMins(pc) == arrMins &&
               PackedCriteria.changes(pc) == changes)
            {
                return pc;
            }
        }
        throw new NoSuchElementException("Aucun long ne contient ces données");
    }

    /**
     * Appelle la méthode accept de l'action de type LongConsumer donnée avec chacun des critères de la frontière
     * @param action action
     */
    public void forEach(LongConsumer action){
        for (long packed_criteria : packed_criterias){
            action.accept(packed_criteria);
        }
    }

    /**
     * Méthode toString basique
     * @return la chaîne de caractère décrivant au mieux la classe
     */
    public String toString(){
        StringBuilder s = new StringBuilder();

        for (long pc : packed_criterias){

            if(PackedCriteria.hasDepMins(pc)){
                s.append("Heure de départ : ")
                        .append(PackedCriteria.depMins(pc));
            }

            s.append("\n Heure d'arrivée : ")
                    .append(PackedCriteria.arrMins(pc))
                    .append("\n Changements : ")
                    .append(PackedCriteria.changes(pc));
        }

        return s.toString();
    }

    /**
     * Builder statiquement imbriqué
     */
    public final static class Builder{

        // Tableau de type long qui contient les tuples
        private long[] arrayInConstruction;
        private int effectiveSize;

        /**
         * Constructeur par défaut
         */
        public Builder(){
            this.arrayInConstruction = new long[2];
            this.effectiveSize =0;
        }

        /**
         * Constructeur de copie
         * @param that objet que l'on veut copier
         */
        public Builder(Builder that){
            this.arrayInConstruction = that.arrayInConstruction.clone();
            this.effectiveSize = that.effectiveSize;

        }

        /**
         * Retourne true si le tableau en cours de construction est vide, false sinon
         * @return (booléen)
         */
        public boolean isEmpty(){
            return effectiveSize == 0;
        }

        /**
         * vide la frontière en cours de construction en supprimant tous ses éléments
         * @return ce builder
         */
        public Builder clear(){
            this.arrayInConstruction = new long[2];
            this.effectiveSize = 0;
            return this;
        }

        /**
         * Ajoute à la frontière le tuple de critères empaquetés donné
         * @param packedTuple tuple de critère empaqueté
         * @return le builder mis à jour
         */
        public Builder add(long packedTuple) {

            // Vérifions qu'aucun tuple ne domine packedTuple
            for (long tupleOnArray : arrayInConstruction){
                // Est-ce que le tuple déja présent domine le nouveau ?
                if (PackedCriteria.dominatesOrIsEqual(tupleOnArray, packedTuple)){
                    // On ne veut pas l'ajouter, on ne modifie rien et on retourne cette instance
                    return this;
                }
            }

            // La vérification est passée, on peut donc l'ajouter, trouvons le bon endroit
            // Selon l'ordre lexicographique, qui est ici l'ordre croissant car c'est des nombres positifs
            int position = 0;
            while (packedTuple > arrayInConstruction[position]){
                position += 1;
            }

            // Ajoutons-le à cette position
            arrayInConstruction[position] = packedTuple;
            this.effectiveSize += 1;

            // Supprimons tous ceux qui se font dominer (ils se trouvent forcément après, voir théorie)
            for (int i = position + 1; i <= effectiveSize; i++){
                if (PackedCriteria.dominatesOrIsEqual(packedTuple, arrayInConstruction[i])){
                    // TODO supprimer le critère dominé (arrayInConstruction[i])
                }
            }

            // TODO supprimer ça, c'est juste pour par avoir d'erreur
            return this;


        }
    }

}
