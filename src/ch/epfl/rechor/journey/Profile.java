package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.List;

/**
 * Représente un profil
 * @author Yoann Salamin (390522)
 * * @author Axel Verga (398787)
 */
public record Profile(TimeTable timeTable, LocalDate date,
                      int arrStationId, List<ParetoFront> stationFront) {

    /**
     * Constructeur compact
     */
    public Profile {
        // TODO
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

}
