package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.Platforms;

public final class BufferedPlatforms implements Platforms {

    /**
     * Fonction qui retourne le nom de la voie (p. ex. 70) ou du quai (p. ex. A), qui peut être vide
     *
     * @param id index de la voie ou du quai
     * @return nom de la voie (p. ex. 70) ou du quai (p. ex. A), qui peut être vide
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String name(int id) {
        return "";
    }

    /**
     * Fonction qui retourne l'index de la gare à laquelle cette voie ou ce quai appartient.
     *
     * @param id id de la voie ou du quai
     * @return index de la gare à laquelle cette voie ou ce quai appartient
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public int stationId(int id) {
        return 0;
    }

    /**
     * Fonction qu retourne le nombre d'éléments des données
     *
     * @return un int qui représente le nombre d'éléments
     * des données
     */
    @Override
    public int size() {
        return 0;
    }
}
