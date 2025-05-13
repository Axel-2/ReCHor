package ch.epfl.rechor.timetable;

import java.time.LocalDate;
import java.util.Date;

/**
 * Classe qui représente un horaire dont les données qui dépendent de la date sont stockées
 * dans un cache. De la sorte, si ces données sont demandées plusieurs fois de suite pour
 * une seule et même date, elles ne sont pas rechargées à chaque fois.
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class CachedTimeTable implements TimeTable {

    private TimeTable underlyingTimetable;

    // Variables qui vont contenir les données misent en cache
    private Connections currentCachedConnection = null;
    private Trips currentCachedTrips = null;

    private LocalDate cachedTripsDate        = null;
    private LocalDate cachedConnectionsDate  = null;
    
    // TODO utile ?
    private CachedTimeTable() {

    }

    public  CachedTimeTable(TimeTable timeTable) {
        // TODO immuabilité ??
        this.underlyingTimetable = timeTable;
    }

    // Fonctions déléguées à l'autre horaire

    @Override
    public Stations stations() {
        return underlyingTimetable.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return underlyingTimetable.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return underlyingTimetable.platforms();
    }

    @Override
    public Routes routes() {
        return underlyingTimetable.routes();
    }

    @Override
    public Transfers transfers() {
        return underlyingTimetable.transfers();
    }

    @Override
    public boolean isStationId(int stopId) {
        return underlyingTimetable.isStationId(stopId);
    }

    @Override
    public boolean isPlatformId(int stopId) {
        return underlyingTimetable.isPlatformId(stopId);
    }

    @Override
    public int stationId(int stopId) {
        return underlyingTimetable.stationId(stopId);
    }

    @Override
    public String platformName(int stopId) {
        return underlyingTimetable.platformName(stopId);
    }

    // Fonctions avec données cachées

    @Override
    public Trips tripsFor(LocalDate date) {

        // On vérifie si la donnée est déjà mise en cache et si la date a changé
        if (currentCachedTrips == null || cachedTripsDate == null || !cachedTripsDate.equals(date)) {
            // s'il faut changer le cache, on va chercher les infos dans l'horaire
            currentCachedTrips =  underlyingTimetable.tripsFor(date);
            cachedTripsDate = date;
        }

        return currentCachedTrips;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {

        // On vérifie si la donnée est déjà mise en cache et si la date a changé
        if (currentCachedConnection == null || cachedConnectionsDate == null || !cachedConnectionsDate.equals(date)) {
            // s'il faut changer le cache, on va chercher les infos dans l'horaire
            currentCachedConnection = underlyingTimetable.connectionsFor(date);
            cachedConnectionsDate = date;
        }

        return currentCachedConnection;
    }
}
