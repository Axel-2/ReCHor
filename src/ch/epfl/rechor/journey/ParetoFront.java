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
        // TODO La méthode build  doit garantir ...
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
     * Builder statiquement imbriqué de ParetoFront
     */
    public final static class Builder{

        // Tableau de type long qui contient les tuples
        private long[] arrayInConstruction;
        private int effectiveSize;

        /**
         * Constructeur par défaut
         */
        public Builder(){
            // TODO pas sûr de la taille
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
            return arrayInConstruction.length == 0;
        }

        public Builder clear(){
            return null;
        }
    }

}
