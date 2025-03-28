package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyJourneyExtractorOtherTest {

    @Test
    public void MyJourneyExtTest() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486);
        List<Journey> js = JourneyExtractor.journeys(p, 17914); // 3711, 156, 148, 142, 140, 860, 1521, 1522// Max number 33349
        String j = JourneyIcalConverter.toIcalendar(js.get(0));
        System.out.println(j);
    }

    private Profile readProfile(TimeTable timeTable,
                                LocalDate date,
                                int arrStationId) throws IOException {
        Path path =
                Path.of("profile_" + date + "_" + arrStationId + ".txt");
        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB =
                    new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(","))
                    frontB.add(Long.parseLong(t, 16));
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }



    @Test
    public void testProblemStations() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486);

        // Array of station IDs to test, including both problematic and working ones
        int[] stationsToTest = {349, 1000, 3400, 6900, 33349, 33350};

        for (int stationId : stationsToTest) {
            try {
                System.out.println("Testing station ID: " + stationId);
                List<Journey> journeys = JourneyExtractor.journeys(p, stationId);

                if (!journeys.isEmpty()) {
                    System.out.println("✓ Found " + journeys.size() + " journeys for station " + stationId);

                    // Print details of first journey
                    Journey firstJourney = journeys.get(0);
                    System.out.println("  First journey: " + firstJourney.depStop().name() +
                            " → " + firstJourney.arrStop().name() +
                            " (" + firstJourney.legs().size() + " legs)");

                    // Test generating iCalendar format
                    String ical = JourneyIcalConverter.toIcalendar(firstJourney);
                    System.out.println("  Successfully generated iCalendar format");
                } else {
                    System.out.println("✓ No journeys found for station " + stationId + " (this is valid)");
                }
            } catch (Exception e) {
                System.out.println("✗ Error with station " + stationId + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());

                // Optional: Print more detailed diagnostics
                if (stationId == 6900) {
                    System.out.println("\nDetailed diagnosis for station 6900:");
                    try {
                        // Try to get the raw ParetoFront for this station
                        ParetoFront front = p.forStation(stationId);
                        System.out.println("ParetoFront size: " + front.size());

                        // Manually extract the first criteria to see what's happening
                        if (front.size() > 0) {
                            front.forEach(criteria -> {
                                int arrMins = PackedCriteria.arrMins(criteria);
                                int changes = PackedCriteria.changes(criteria);
                                int payload = PackedCriteria.payload(criteria);

                                System.out.println("Criteria: arrMins=" + arrMins +
                                        ", changes=" + changes +
                                        ", payload=" + payload);
                            });
                        }
                    } catch (Exception diagEx) {
                        System.out.println("Diagnostic error: " + diagEx.getMessage());
                    }
                }
            }
            System.out.println();
        }
    }


    @Test
    public void diagnoseProblemStationsSkipValues() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486);

        // List of problematic stations to test
        int[] problemStations = {6900, 3711, 156, 148, 142, 140, 860, 1521, 1522};

        for (int stationId : problemStations) {
            System.out.println("\nTesting station ID: " + stationId);
            ParetoFront front = p.forStation(stationId);

            if (front == null || front.size() == 0) {
                System.out.println("No ParetoFront available for station " + stationId);
                continue;
            }

            System.out.println("ParetoFront size: " + front.size());

            // Group criteria by skips value to see if there's a pattern
            Map<Integer, List<Long>> criteriaBySkips = new HashMap<>();

            front.forEach(criteria -> {
                int payload = PackedCriteria.payload(criteria);
                int skips = payload & 0xFF;

                criteriaBySkips.computeIfAbsent(skips, k -> new ArrayList<>()).add(criteria);
            });

            // Print summary of criteria by skips value
            System.out.println("Criteria distribution by skips value:");
            criteriaBySkips.forEach((skips, criteriaList) -> {
                System.out.println("  Skips=" + skips + ": " + criteriaList.size() + " criteria");
            });

            // Test journey extraction with different skips values
            for (Map.Entry<Integer, List<Long>> entry : criteriaBySkips.entrySet()) {
                int skips = entry.getKey();
                List<Long> criteriaList = entry.getValue();

                if (!criteriaList.isEmpty()) {
                    long sampleCriteria = criteriaList.get(0);

                    System.out.println("\nTesting extraction with skips=" + skips);
                    System.out.println("  Sample criteria: " + Long.toHexString(sampleCriteria));

                    try {
                        // Use reflection to access private method
                        java.lang.reflect.Method extractMethod = JourneyExtractor.class.getDeclaredMethod(
                                "extractJourney", Profile.class, int.class, long.class);
                        extractMethod.setAccessible(true);

                        Journey journey = (Journey) extractMethod.invoke(null, p, stationId, sampleCriteria);
                        System.out.println("  ✓ Success: Journey from " + journey.depStop().name() +
                                " to " + journey.arrStop().name() +
                                " with " + journey.legs().size() + " legs");
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        System.out.println("  ✗ Failed: " + cause.getClass().getSimpleName() +
                                (cause.getMessage() != null ? " - " + cause.getMessage() : ""));

                        // Show where the error occurred
                        StackTraceElement[] stack = cause.getStackTrace();
                        if (stack.length > 0) {
                            System.out.println("    at " + stack[0]);
                        }

                        // If it's a problem in Journey constructor, try to identify the specific check
                        if (cause instanceof IllegalArgumentException && stack.length > 0 &&
                                stack[0].getClassName().contains("Journey")) {
                            System.out.println("    This appears to be a validation failure in the Journey constructor");
                        }
                    }
                }
            }
        }
    }



}