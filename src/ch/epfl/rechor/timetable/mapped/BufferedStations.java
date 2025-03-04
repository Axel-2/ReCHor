package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.Stations;

public final class BufferedStations implements Stations {
    /**
     * Retourne le nom de la gare d'index donné
     *
     * @param id index d'une gare
     * @return le nom de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public String name(int id) {
        return "";
    }

    /**
     * Fonctio qui retourne la longitude, en degrés, de la gare d'index donné
     *
     * @param id index d'une gare
     * @return longitude, en degrés, de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public double longitude(int id) {
        return 0;
    }

    /**
     * Fonction qui retourne la latitude, en degrés, de la gare d'index donné.
     *
     * @param id index d'une gare
     * @return latitude, en degrés, de la gare d'index donné
     * @throws IndexOutOfBoundsException Erreur si l'index est invalide
     */
    @Override
    public double latitude(int id) {
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
