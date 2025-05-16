package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
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

        // il faut copier la table des frontières de Pareto afin de garantir l'immuabilité de la classe
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Méthode qui retourne les liaisons correspondant au profil,
     * qui sont simplement celles de l'horaire, à la date à laquelle correspond le profil
     * @return les liaisons (ou connections).
     */
    public Connections connections() {

        // les liaisons sont simplement celles de l'horaire, à la date à laquelle correspond le profil
        return timeTable.connectionsFor(date);
    }

    /**
     * Méthode qui retourne les courses correspondant au profil, qui sont simplement celles de l'horaire,
     * à la date à laquelle correspond le profil,
     * @return les courses / "trips" correspondantes.
     */
    public Trips trips() {

        // Les courses sont simplement celles de l'horaire,
        // à la date à laquelle correspond le profil,
        return timeTable.tripsFor(date);
    }

    /**
     * Méthode qui retourne la frontière de Pareto pour la gare d'index donné
     * @param stationId id de la gare
     * @throws IndexOutOfBoundsException si l'index est invalide
     * @return la frontière de pareto pour la gare d'index donné
     */
    public ParetoFront forStation(int stationId) {

        // On utilise simplement la fonction get de notre liste
        // pour avoir la bonne frontière d'index donné
        // get lance une erreur si l'index est invalide
        return stationFront.get(stationId);

    }

    /**
     * Classe qui représente un bâtisseur de profil
     *  @author Yoann Salamin (390522)
     *  @author Axel Verga (398787)
     */
    public static final class Builder {

        // on stocke les attributs en cours de constructions pour pouvoir les passer
        // au constructeur de Profile
        private final TimeTable currentTimetable;
        private final LocalDate currentLocalDate;
        private final int currentArrStationId;

        // tableau qui contient les bâtisseurs des frontières de Pareto des gares
        private final ParetoFront.Builder[] paretoFrontStationList;

        // tableau qui contient les bâtisseurs des frontières de Pareto des courses
        private final ParetoFront.Builder[]  paretoFrontTripsList;


        /**
         * Constructeur qui construit un bâtisseur de profil pour l'horaire, la date et la gare de destination donnés.
         * @param timeTable horaire donné
         * @param date date donnée
         * @param arrStationId gare de destination donnée
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {


            // On stocke les valeurs données dans nos attributs d'instance
            this.currentTimetable = timeTable;
            this.currentLocalDate = date;
            this.currentArrStationId = arrStationId;


            // On initialise les deux tableaux primitifs qui stockent les frontières de Pareto
            // on doit d'abord récupérer la taille des tableaux à l'aide de l'instance de
            // timetable
            int numberOfStations = timeTable.stations().size();
            int numberOfTripsCurrentDay = timeTable.tripsFor(date).size();
            paretoFrontStationList = new ParetoFront.Builder[numberOfStations];
            paretoFrontTripsList = new ParetoFront.Builder[numberOfTripsCurrentDay];

        }

        /**
         * Fonction qui retourne le bâtisseur de la frontière de Pareto pour la gare d'index donné,
         * qui est null si aucun appel à setForStation n'a été fait précédemment pour cette gare
         * @param stationId gare d'index donné
         * @return bâtisseur de la frontière de Pareto pour la gare d'index donné
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public ParetoFront.Builder forStation(int stationId) {

            // on retourne simplement le bon élément dans le tableau
            // la valeur est bien null si aucun appel à setForstation n'a
            // été fait, car le tableau est initialisé avec de null;
            return paretoFrontStationList[stationId];
        }

        /**
         * Fonction qui associe le bâtisseur de frontière de Pareto donné à la gare d'index donné
         * @param stationId gare d'index donné
         * @param builder bâtisseur de frontière de Pareto
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {

            // On met simplement le builder au bon endroit dans la liste
            paretoFrontStationList[stationId] = builder;
        }

        /**
         * Fonction qui retourne le bâtisseur de la frontière de Pareto pour la ligne d'index donné,
         * @param tripId course d'index donné
         * @return bâtisseur de la frontière de Pareto pour la route d'index donné
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public ParetoFront.Builder forTrip(int tripId) {

            // on retourne simplement le bon élément dans le tableau
            // la valeur est bien nulle si aucun appel à setForstation n'a
            // été fait, car le tableau est initialisé avec que des null
            return paretoFrontTripsList[tripId];
        }

        /**
         * Fonction qui fait la même chose que setForStation mais pour la course d'index donné,
         * @param tripId course d'index donné
         * @param builder bâtisseur de frontière de Pareto
         * @throws IndexOutOfBoundsException si l'index est invalide
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {

            // On met simplement le builder au bon endroit dans la liste
            paretoFrontTripsList[tripId] = builder;
        }

        /**
         * Fonction qui retourne le profil simple sans les frontières de Pareto correspondant aux courses
         * en cours de construction.
         * @return Une instance de Profile
         */
        public Profile build() {

            // Construction de la liste contenant les frontières de Pareto
            List<ParetoFront> paretoFrontList = new ArrayList<>();

            // on itère sur tous les builders
            for (ParetoFront.Builder bld : paretoFrontStationList) {
                // on ne peut appeler que les builders qui ne sont
                // pas nuls
                if (bld == null) {
                    paretoFrontList.add(ParetoFront.EMPTY);
                } else {
                    // en ajoute le Profile dans notre liste
                    // en appelant le builder
                    paretoFrontList.add(bld.build());
                }
            }

            return new Profile(currentTimetable, currentLocalDate, currentArrStationId, paretoFrontList);
        }


    }

}
