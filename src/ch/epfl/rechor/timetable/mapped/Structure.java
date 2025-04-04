package ch.epfl.rechor.timetable.mapped;

import java.util.Objects;

/**
 * Classe qui a pour but de faciliter la description de la structure des données aplaties
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class Structure {

    // Tableau contenant la position, en octets, du premier
    // octet de chacun des champs dans la structure
    private final short[] firstByteIndexOfEachField;

    // taille totale de la structure, en octets.
    private final int totalStructureSizeInBytes;

    /**
     * Type énuméré des différentes tailles d'octet
     */
    public enum FieldType {
        U8,
        U16,
        S32
    }

    /**
     * Enregistrement qui représente un champ
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
        // Retourne une instance de Field avec les attributs donnés
        return new Field(index, type);
    }

    /**
     * Constructeur
     * @param fields champs de taille arbitraire, devant être donnés dans l'ordre
     * @throws IllegalArgumentException si l'index du champ est invalide
     */
    public Structure(Field... fields) {

        // Initialisation du tableau contenant la position, en octets, du premier
        // octet de chacun des champs dans la structure,
        // Ce tableau a une taille égale au nombre de champs dans la structure
        this.firstByteIndexOfEachField = new short[fields.length];

        // Initialisation de la taille, qui va être incrémentée, mais qui commence à 0.
        int currentByteIndex = 0;

        // On va itérer sur chaque champ
        for (int i = 0; i < fields.length; i++) {

            // 1) Vérifie que les champs sont donnés dans l'ordre
            if (fields[i].index() != i) {
                throw new IllegalArgumentException("l'index des champs ne correspond pas à leur position");
            }

            // 2) Stock l'index du premier octet de chacun des champs dans la structure
            firstByteIndexOfEachField[i] = (short) currentByteIndex;

            // 3) Ajoute à la taille actuelle (mesurée en nombre d'octets), la taille du champ d'index i
            switch (fields[i].type()) {

                // ajouter la bonne taille en fonction du nombre d'octets

                // U16 -> on a 2 octets
                case U16 -> currentByteIndex += 2;
                // U8 → un byte donc un octet
                case U8 -> currentByteIndex += 1;
                // et 4 octets pour S32
                case S32 -> currentByteIndex += 4;

            }
        }

        // Nous avons notre taille finale, l'incrémentation est finie. On l'injecte dans l'attribut
        this.totalStructureSizeInBytes = currentByteIndex;
    }

    /**
     * Retourne la taille totale en octets
     * @return la taille
     */
    public int totalSize() {
        return totalStructureSizeInBytes;
    }

    /**
     * Retourne l'index, dans le tableau d'octets contenant les données aplaties,
     * du premier octet du champ d'index fieldIndex de l'élément d'index elementIndex
     * @param fieldIndex index du champ
     * @param elementIndex index de l'élément
     * @throws IndexOutOfBoundsException si l'index du champ est invalide
     * @return index correspondant aux paramètres
     */
    public int offset(int fieldIndex, int elementIndex) {

        // Vérifie que l'index du champ est valide
        if (fieldIndex < 0 || fieldIndex >= firstByteIndexOfEachField.length) {
            throw new IndexOutOfBoundsException();
        }

        // Retourne l'index correspondant dans le tableau de donnée aplati.
        return firstByteIndexOfEachField[fieldIndex] + (elementIndex * totalStructureSizeInBytes);
    }

}
