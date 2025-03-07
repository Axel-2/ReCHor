package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * Structure d'une donnée aplatie
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class Structure {
    short[] firstBytePositions;
    int totalSize;

    /**
     * Type énuméré des différentes tailles d'octet
     */
    public enum FieldType {
        U8(1),
        U16(2),
        S32(4);

        private final int size;

        FieldType(int size){
            this.size = size;
        }

        public int size(){
            return this.size;
        }
    }

    /**
     * Représente un champ
     * @param index index du champ dans la structure
     * @param type type du champ
     */
    public record Field(int index, FieldType type){
        public Field {
            Objects.requireNonNull(type, "le type ne doit pas être null");
        }
    }

    /**
     * Retourne une instance de Field avec les attributs donnés
     * @param index index du champ dans la structure
     * @param type type du champ
     * @return une instance
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    /**
     * Constructeur
     * @param fields champs de taille arbitraire, devant être donnés dans l'ordre
     */
    public Structure(Field... fields) {;

        // Création du tableau, qui a une taille égale au nombre de champs
        this.firstBytePositions = new short[fields.length];

        // Initialisation de la taille, qui va être incrémentée, mais qui commence à 0.
        int size = 0;

        for (int i = 0; i < fields.length; i++) {

            // 1) Vérifie que les champs sont donnés dans l'ordre
            if (fields[i].index() != i) {
                throw new IllegalArgumentException("l'index des champs ne correspond pas à leur position");
            }

            // 2) Stock le premier octet de chacun des champs dans la structure
            firstBytePositions[i] = (short) size;

            // 3) Ajoute à la taille actuelle (mesurée en nombre d'octet), la taille du champ d'index i
            size += fields[i].type().size();

        }
        // Nous avons notre taille finale, l'incrémentation est finie. On l'injecte dans l'attribut
        this.totalSize = size;
    }

    /**
     * Retourne la taille totale en octets
     * @return la taille
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Retourne l'index, dans le tableau d'octets contenant les données aplaties,
     * du premier octet du champ d'index fieldIndex de l'élément d'index elementIndex
     * @param fieldIndex index du champ
     * @param elementIndex index de l'élément
     * @return index correspondant aux paramètres
     */
    public static int offset(int fieldIndex, int elementIndex) {
        // Vérifie que l'index du champ est valide
        Preconditions.checkArgument(fieldIndex >= 0 && fieldIndex < firstBytePositions.length);

        // Retourne l'index correspondant dans le tableau de donnée aplati.
        return firstBytePositions[fieldIndex] + (elementIndex * totalSize);
    }

}
