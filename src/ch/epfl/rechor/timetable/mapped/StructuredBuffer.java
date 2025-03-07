package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;

/**
 * Classe qui représente un tableau d'octets structuré
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class StructuredBuffer {

    private Structure structure;
    private ByteBuffer buffer;

    // unique constructeur publique
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {


        // lève une IllegalArgumentException si le nombre d'octets de
        // ce tableau n'est pas un multiple de la taille totale de la structure.
        int nbOfByteInBuffer = buffer.capacity();
        Preconditions.checkArgument(structure.totalSize() % nbOfByteInBuffer == 0);

        // on stock les variables dans notre instance
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
        // ici on utilise une division entière mais ça ne doit pas poser
        // problème car on a vérifié dans le constructeur que la taille du buffer
        // est bien un multiple de la taille de la structure
        int nbOfElement = buffer.capacity() / structure.totalSize();

        return nbOfElement;
    }


    /**
     * Fonction qui retourne l'entier U8 correspondant au champ d'index fieldIndex de
     * l'élément d'index elementIndex du tableau,
     * ou lève IndexOutOfBoundsException si l'un des deux index est invalide
     *
     * @param fieldIndex champ d'index voulu
     * @param elementIndex élément d'index du tableau
     * @return entier u8 correspondant aux paramètres voulus
     */
    public int getU8(int fieldIndex, int elementIndex) {

        // TODO preconditions
        

        int offset = Structure.offset(fieldIndex, elementIndex);

        // la méthode get de buffer nous donne le byte
        // à l'index donné
        byte u8 = buffer.get(offset);

        // le byte doit être non signé
        int unsignedU8 = Byte.toUnsignedInt(u8);

        return unsignedU8;

    }

}
