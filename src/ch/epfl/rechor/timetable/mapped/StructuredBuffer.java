package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;

/**
 * Classe qui représente un tableau d'octets structuré
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;

    // unique constructeur public
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {

        // lève une IllegalArgumentException si le nombre d'octets de
        // ce tableau n'est pas un multiple de la taille totale de la structure.
        int nbOfByteInBuffer = buffer.capacity();
        Preconditions.checkArgument(nbOfByteInBuffer % structure.totalSize() == 0);

        // on stocke les paramètres dans notre instance
        this.structure = structure;
        this.buffer = buffer;
    }

    /**
     * Fonction qui retourne le nombre d'éléments que contient le tableau
     * @return un int représentant le nombre d'éléments que contient le tableau
     */
    public int size() {

        // le nombre d'éléments s'obtient en divisant la capacité totale
        // du buffer par la taille d'un seul élément
        // ici, on utilise une division entière, mais ça ne doit pas poser
        // problèmes, car on a vérifié dans le constructeur que la taille du buffer
        // est bien un multiple de la taille de la structure
        return buffer.capacity() / structure.totalSize();
    }


    /**
     * Fonction qui retourne l'entier U8 correspondant au champ d'index fieldIndex de
     * l'élément d'index elementIndex du tableau,
     * ou lève IndexOutOfBoundsException si l'un des deux index est invalide
     *
     * @param fieldIndex champ d'index voulu
     * @param elementIndex élément d'index du tableau
     * @return entier u8 correspondant aux paramètres voulus
     * @throws IndexOutOfBoundsException si l'un des deux index est invalide
     */
    public int getU8(int fieldIndex, int elementIndex) {

        int offset = structure.offset(fieldIndex, elementIndex);

        // la méthode get de buffer nous donne le byte
        // à l'index donné
        byte u8 = buffer.get(offset);

        // le byte doit être non signé
        return Byte.toUnsignedInt(u8);

    }

    /**
     * Fonction qui fait la même chose que getU8 mais pour un champ de type U16
     *
     * @param fieldIndex champ d'index voulu
     * @param elementIndex élément d'index du tableau
     * @return entier u16 correspondant aux paramètres voulus
     * @throws IndexOutOfBoundsException si l'un des deux index est invalide
     */
    public int getU16(int fieldIndex, int elementIndex) {

        int offset = structure.offset(fieldIndex, elementIndex);

        // on fait la même chose, mais cette fois avec un u16,
        // on utilise donc la méthode getShort()
        short u16 = buffer.getShort(offset);

        // on retourne une valeur positive donc
        // non signée
        return Short.toUnsignedInt(u16);
    }

    /**
     * Fonction qui fait la même chose que getU8 mais pour un champ de type S32.
     *
     * @param fieldIndex champ d'index voulu
     * @param elementIndex élément d'index du tableau
     * @return entier u32 correspondant aux paramètres voulus
     * @throws IndexOutOfBoundsException si l'un des deux index est invalide
     */
    public int getS32(int fieldIndex, int elementIndex) {

        int offset = structure.offset(fieldIndex, elementIndex);

        // on récupère un int avec getInt et on le retourne
        return buffer.getInt(offset);
    }

}
