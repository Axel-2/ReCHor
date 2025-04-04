package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyRouterTest {

    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i += 1)
            if (stations.name(i).equals(stationName)) return i;
        throw new NoSuchElementException();
    }

    @Test
    public void routerTest() throws IOException {
        long tStart = System.nanoTime();

        TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.APRIL, 1);
        int depStationId = stationId(stations, "Ecublens VD, EPFL");
        int arrStationId = stationId(stations, "Gruyères");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);

        var journeys = JourneyExtractor.journeys(profile, depStationId);
        if (journeys.size() > 32) {
            Journey journey = journeys.get(32);
            System.out.println(JourneyIcalConverter.toIcalendar(journey));
        } else {
            System.out.println("Pas assez de voyages trouvés pour obtenir l'élément à l'index 32");
        }

        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
}