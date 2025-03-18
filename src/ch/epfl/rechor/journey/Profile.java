package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Représente un profil
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {


    /**
     * Constructeur compact de Profile
     */
    public Profile {

        // TODO vérifier si c'est la bonne façon de faire
        // il faut copier la la table des frontières de Pareto afin de garantir l'immuabilité de la classe
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Méthode qui retourne les liaisons correspondant au profil,
     * qui sont simplement celles de l'horaire, à la date à laquelle correspond le profil
     * @return les liaisons (ou connections).
     */
    public Connections connections() {
        // TODO
        return null;
    }

    /**
     * Méthode qui retourne les courses correspondant au profil, qui sont simplement celles de l'horaire,
     * à la date à laquelle correspond le profil,
     * @return les courses / "trips" correspondantes.
     */
    public Trips trips() {
        // TODO
        return null;
    }

    /**
     * Méthode qui retourne la frontière de Pareto pour la gare d'index donné
     * @param stationId id de la gare
     * @throws IndexOutOfBoundsException si l'index est invalide
     * @return la frontière de pareto pour la gare d'index donné
     */
    public ParetoFront forStation(int stationId) {
        // TODO
        return null;

    // TODO : Builder imbriqué
    }

    /**
     * Classe qui représente un bâtisseur de profil
     *  @author Yoann Salamin (390522)
     *  @author Axel Verga (398787)
     */
    public static final class  Builder {



        // on stocke les attributs en cours de constructions pour pouvoir les passer
        // au constructeur de Profile
        private TimeTable currentTimetable;
        private LocalDate currentLocalDate;
        private int currentArrStationId;

        // TODO comprendre concretement comment faire ces tableaux et remplir la taille
        // tableau qui contient les bâtisseurs des frontières de Pareto des gares
        private ParetoFront.Builder[] paretoFrontCurrentList;

        // tableau qui contient les bâtisseurs des frontières de Pareto des courses
        private ParetoFront.Builder[]  paretoFrontTripsList;


        /**
         * Constructeur qui construit un bâtisseur de profil pour l'horaire, la date et la gare de destination donnés.
         * @param timeTable
         * @param date
         * @param arrStationId
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            // TODO

        }

        /**
         * Fonction qui retourne le bâtisseur de la frontière de Pareto pour la gare d'index donné,
         * qui est null si aucun appel à setForStation n'a été fait précédemment pour cette gare
         * @param stationId gare d'index donné
         * @return bâtisseur de la frontière de Pareto pour la gare d'index donné
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public ParetoFront.Builder forStation(int stationId) {
            // TODO
            return null;
        }

        /**
         * Fonction qui associe le bâtisseur de frontière de Pareto donné à la gare d'index donné
         * @param stationId gare d'index donné
         * @param builder bâtisseur de frontière de Pareto
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {

            // TODO
        }

        /**
         * Fonction qui fait la même chose que forStation mais pour la course d'index donné
         * @param tripId course d'index donné
         * @return bâtisseur de la frontière de Pareto pour la gare d'index donné
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public ParetoFront.Builder forTrip(int tripId) {

            // TODO
            return null;
        }

        /**
         * Fonction qui fait la même chose que setForStation mais pour la course d'index donné,
         * @param tripId course d'index donné
         * @param builder bâtisseur de frontière de Pareto
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {

            // TODO

        }

        /**
         * Fonction qui retourne le profil simple sans les frontières de Pareto correspondant aux courses
         * en cours de construction.
         * @return Une instance de Profile
         */
        Profile build() {

            // TODO
            return null;
        }


    }

}
