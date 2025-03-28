
package ch.epfl.rechor.journey;
import static org.junit.jupiter.api.Assertions.*;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class MyJourneyExtractorTest {

    @Test
    void testExtractionDeVoyages() throws IOException {
        // Charger l’horaire (nécessaire pour le profil)
        TimeTable t = FileTimeTable.in(Path.of("timetable"));

        // Lire le profil depuis le fichier donné
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile profile = readProfile(t, date, 11486);

        // Extraire les voyages depuis la gare 7872 (Ecublens VD, EPFL)
        List<Journey> js = JourneyExtractor.journeys(profile, 7872);
        String j = JourneyIcalConverter.toIcalendar(js.get(32));

        System.out.println(j);
    }

    private Profile readProfile(TimeTable timeTable, LocalDate date, int arrStationId) throws IOException {
        Path path = Path.of("profile_2025-03-18_11486.txt");
        try (var r = java.nio.file.Files.newBufferedReader(path)) {
            Profile.Builder profileB = new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(",")) {
                    frontB.add(Long.parseLong(t, 16));
                }
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }
}
