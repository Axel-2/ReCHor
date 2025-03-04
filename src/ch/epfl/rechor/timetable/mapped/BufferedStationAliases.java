package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.StationAliases;

public final class BufferedStationAliases implements StationAliases {
    /**
     * Fonction qui retourne le nom alternatif d'index donné (p. ex. Losanna),
     *
     * @param id index d'une gare
     * @return Nom alternatif de l'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String alias(int id) {
        return "";
    }

    /**
     * Fonction qui retourne le nom de la gare à laquelle correspond le nom alternatif d'index donné (p. ex. Lausanne).
     *
     * @param id index d'une gare
     * @return nom de la gare à laquelle correspond le nom alternatif d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String stationName(int id) {
        return "";
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
