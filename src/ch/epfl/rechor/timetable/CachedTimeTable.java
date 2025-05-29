package ch.epfl.rechor.timetable;
import java.time.LocalDate;

/**
 * Classe qui représente un horaire dont les données qui dépendent de la date sont stockées
 * dans un cache. De la sorte, si ces données sont demandées plusieurs fois de suite pour
 * une seule et même date, elles ne sont pas rechargées à chaque fois.
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class CachedTimeTable implements TimeTable {

    private final TimeTable underlyingTimetable;

    // Variables qui vont contenir les données misent en cache
    private Connections currentCachedConnections = null;
    private Trips       currentCachedTrips       = null;

    // La date actuelle du cache
    private LocalDate cachedDate = null;

    /**
     * Crée un horaire mis en cache autour de l'horaire sous-jacent donné.
     * @param timeTable l'horaire dont on veut mettre en cache les données
     */
    public CachedTimeTable(TimeTable timeTable) {
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

    // Fonctions avec données mises en cache

    @Override
    public Trips tripsFor(LocalDate date) {

        // On vérifie si la donnée est déjà mise en cache et si la date a changé
        ensureCacheFor(date);

        return currentCachedTrips;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {

        // On vérifie si la donnée est déjà mise en cache et si la date a changé
        ensureCacheFor(date);

        return currentCachedConnections;
    }


    /**
     * Fonction qui recalcule le cache s'il le faut
     * @param date la date voulue
     */
    private void ensureCacheFor(LocalDate date) {
        if (currentCachedTrips == null
                || currentCachedConnections == null
                || cachedDate == null
                || !cachedDate.equals(date)) {
            currentCachedTrips       = underlyingTimetable.tripsFor(date);
            currentCachedConnections = underlyingTimetable.connectionsFor(date);
            cachedDate               = date;
        }
    }
}
