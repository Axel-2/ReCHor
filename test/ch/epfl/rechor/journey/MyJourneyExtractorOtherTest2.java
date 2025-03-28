package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Platforms;
import ch.epfl.rechor.timetable.Stations;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyJourneyExtractorOtherTest2 {

//    @Test
//    public void MyJourneyExtTest() throws IOException {
//        TimeTable t = FileTimeTable.in(Path.of("timetable"));
//        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
//        Profile p = readProfile(t, date, 11486);
//
//        // Loop through potential departure station IDs
//
//        for (int stationId = 0; stationId < 100; stationId++) { // Adjust range as needed
//            List<Journey> journeys = JourneyExtractor.journeys(p, stationId);
//            for (Journey journey : journeys) {
//                String icalString = JourneyIcalConverter.toIcalendar(journey);
//
//                // Get last leg of journey
//                Journey.Leg lastLeg = journey.legs().getLast();
//                String lastStopName = lastLeg.arrStop().name();
//                if (lastStopName.equals("Gruyères, gare")){
//                    System.out.println(journey.legs().getFirst());
//                }
//                if (journey.legs().getFirst() instanceof Journey.Leg.Foot){
//                    if (((Journey.Leg.Foot) journey.legs().getFirst()).isTransfer()){
//                        System.out.println(icalString);
//                    }else{
//                        System.out.println(icalString);
//                    }
//                }
//                assertTrue(lastStopName.equals("Gruyères") || lastStopName.equals("Gruyères, gare"));
//
//            }
//        }
//
//    }

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



//    @Test
//    public void testJourneyStructuralValidity() throws IOException {
//        TimeTable t = FileTimeTable.in(Path.of("timetable"));
//        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
//
//        // Only use Gruyères (11486) which seems to have a profile file
//        int arrStationId = 11486;
//
//        // First, verify the profile file exists
//        Path profilePath = Path.of("profile_" + date + "_" + arrStationId + ".txt");
//        if (!Files.exists(profilePath)) {
//            System.out.println("Profile file doesn't exist: " + profilePath);
//            return; // Skip test if file doesn't exist
//        }
//
//        Profile p = readProfile(t, date, arrStationId);
//
//        for (int depStationId = 0; depStationId < 100; depStationId++) {
//            List<Journey> journeys = JourneyExtractor.journeys(p, depStationId);
//
//            // Skip if no journeys found for this combination
//            if (journeys.isEmpty()) continue;
//
//            for (Journey journey : journeys) {
//                // Check journey structure
//                validateJStructure(journey);
//
//                // Verify arrival station matches expected arrival station by name
//                String expectedArrivalName = t.stations().name(arrStationId);
//                String actualArrivalName = journey.arrStop().name();
//
//                // Station names should match or be closely related
//                assertTrue(
//                        expectedArrivalName.equals(actualArrivalName) ||
//                                actualArrivalName.startsWith(expectedArrivalName + ",") ||
//                                expectedArrivalName.startsWith(actualArrivalName + ","),
//                        "Journey should arrive at or near the profile's target station"
//                );
//
//                // Verify departure station
//                int actualDepStationId = t.stationId(stopToStopId(journey.depStop(), t));
//                assertEquals(depStationId, actualDepStationId,
//                        "Journey should depart from the specified station");
//            }
//        }
//    }

//    @Test
//    public void testExtractJourneysForAllStations() throws IOException {
//        TimeTable t = FileTimeTable.in(Path.of("timetable"));
//        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
//
//        // Only use Gruyères (11486) which seems to have a profile file
//        int targetStationId = 11486;
//
//        // Verify the profile file exists
//        Path profilePath = Path.of("profile_" + date + "_" + targetStationId + ".txt");
//        if (!Files.exists(profilePath)) {
//            System.out.println("Profile file doesn't exist: " + profilePath);
//            return; // Skip test if file doesn't exist
//        }
//
//        Profile p = readProfile(t, date, targetStationId);
//
//        // Track statistics
//        int totalJourneys = 0;
//        int stationsWithJourneys = 0;
//        int maxJourneysPerStation = 0;
//        int stationWithMostJourneys = -1;
//
//        System.out.println("Testing stations for journeys to " + targetStationId);
//
//        // Test specific stations known to work
//        int[] specificStations = {349, 1000, 7872};
//        for (int depStationId = 0; depStationId < 33349; depStationId ++) {
//            try {
//                List<Journey> journeys = JourneyExtractor.journeys(p, depStationId);
//                if (!journeys.isEmpty()) {
//                    stationsWithJourneys++;
//                    totalJourneys += journeys.size();
//
//                    if (journeys.size() > maxJourneysPerStation) {
//                        maxJourneysPerStation = journeys.size();
//                        stationWithMostJourneys = depStationId;
//                    }
//
//                    System.out.println("Found " + journeys.size() + " journeys from " +
//                            depStationId + " to " + targetStationId);
//
//                    // Validate first journey
//                    Journey firstJourney = journeys.get(0);
//                    System.out.println(firstJourney);
//                    validateJStructure(firstJourney);
//
//                    // Print details of first journey
//                    System.out.println("Sample journey: " + firstJourney.depStop().name() +
//                            " -> " + firstJourney.arrStop().name() +
//                            ", departure: " + firstJourney.depTime() +
//                            ", arrival: " + firstJourney.arrTime() +
//                            ", legs: " + firstJourney.legs().size());
//                }
//            } catch (Exception e) {
//                System.err.println("Error with specific station " + depStationId + ": " + e.getMessage());
//            }
//        }
//
//        System.out.println("For target station " + targetStationId + ":");
//        System.out.println("- Found " + totalJourneys + " total journeys");
//        System.out.println("- Across " + stationsWithJourneys + " departure stations");
//        System.out.println("- Max journeys per station: " + maxJourneysPerStation +
//                " (from station " + stationWithMostJourneys + ")");
//    }


    @Test
    public void testFullJourneyPlanning() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrStationId = 11486; // Gruyères
        Profile p = readProfile(t, date, arrStationId);
        int depStationId = 349;  // A known station

        List<Journey> journeys = JourneyExtractor.journeys(p, depStationId);
        assertFalse(journeys.isEmpty(), "Should find at least one journey");

        Journey j = journeys.get(0);
        validateJStructure(j);
        assertEquals(t.stations().name(depStationId), j.depStop().name(),
                "Journey starts at departure station");
        assertTrue(j.arrStop().name().startsWith(t.stations().name(arrStationId)),
                "Journey ends at arrival station");
    }

    private int stopToStopId(Stop stop, TimeTable t) {
        Stations stations = t.stations();

        // First, try to find a matching station
        for (int stationId = 0; stationId < stations.size(); stationId++) {
            if (stations.name(stationId).equals(stop.name()) &&
                    (stop.platformName() == null || stop.platformName().isEmpty())) {
                return stationId;
            }
        }

        // If not found or has a platform, look for a matching platform
        Platforms platforms = t.platforms();
        for (int platformId = 0; platformId < platforms.size(); platformId++) {
            int stationId = platforms.stationId(platformId);
            if (stations.name(stationId).equals(stop.name()) &&
                    platforms.name(platformId).equals(stop.platformName())) {
                // Platform IDs are offset by the number of stations
                return stations.size() + platformId;
            }
        }

        // If no match is found, throw an exception
        throw new IllegalArgumentException(
                "Could not find stopId for stop: " + stop.name() +
                        (stop.platformName() != null && !stop.platformName().isEmpty() ?
                                " platform: " + stop.platformName() : "")
        );
    }


    @Test
    public void testJourneyTemporalValidity() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486);

        List<Journey> journeys = JourneyExtractor.journeys(p, 8503); // Use a known departure station

        // Verify journeys are sorted by departure time and then by arrival time
        for (int i = 1; i < journeys.size(); i++) {
            Journey current = journeys.get(i);
            Journey previous = journeys.get(i-1);

            if (current.depTime().equals(previous.depTime())) {
                // If departure times are equal, arrival times should be in ascending order
                assertTrue(
                        !current.arrTime().isBefore(previous.arrTime()),
                        "When departure times are equal, journeys should be sorted by arrival time"
                );
            } else {
                // Otherwise, departure times should be in ascending order
                assertTrue(
                        !current.depTime().isBefore(previous.depTime()),
                        "Journeys should be sorted by departure time"
                );
            }
        }

        // Verify leg and intermediate stop time consistency for each journey
        for (Journey journey : journeys) {
            for (Journey.Leg leg : journey.legs()) {
                if (leg instanceof Journey.Leg.Transport transport) {
                    // Verify intermediate stops are chronologically ordered
                    List<Journey.Leg.IntermediateStop> stops = transport.intermediateStops();

                    // Verify first intermediate stop is after departure
                    if (!stops.isEmpty()) {
                        assertFalse(
                                stops.get(0).arrTime().isBefore(transport.depTime()),
                                "First intermediate stop arrival cannot be before leg departure"
                        );
                    }

                    // Verify intermediate stops are in time order
                    for (int i = 1; i < stops.size(); i++) {
                        assertFalse(
                                stops.get(i).arrTime().isBefore(stops.get(i-1).depTime()),
                                "Intermediate stops must be in chronological order"
                        );
                    }

                    // Verify last intermediate stop is before arrival
                    if (!stops.isEmpty()) {
                        assertFalse(
                                transport.arrTime().isBefore(stops.get(stops.size()-1).depTime()),
                                "Leg arrival cannot be before last intermediate stop departure"
                        );
                    }
                }
            }
        }
    }

    @Test
    public void testJourneyWithMultipleLegs() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrStationId = 11486; // Gruyères
        int depStationId = 7872;  // Ecublens VD, EPFL
        Profile p = readProfile(t, date, arrStationId);

        List<Journey> journeys = JourneyExtractor.journeys(p, depStationId);
        Journey multiLegJourney = journeys.stream()
                .filter(j -> j.legs().stream().filter(leg -> leg instanceof Journey.Leg.Transport).count() >= 2)
                .findFirst()
                .orElse(null);

        assertNotNull(multiLegJourney, "Should find a journey with multiple transport legs");
        validateJStructure(multiLegJourney);
        assertEquals(t.stations().name(depStationId), multiLegJourney.depStop().name(),
                "Journey should start at the departure station");
        assertTrue(multiLegJourney.arrStop().name().startsWith(t.stations().name(arrStationId)),
                "Journey should end at or near the arrival station");
    }

    @Test
    public void testSpecificJourneyScenario() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486); // Using Gruyères as the arrival station

        // Test a specific journey from Ecublens to Gruyères
        List<Journey> journeys = JourneyExtractor.journeys(p, 7872);

        // Find a journey that matches expected characteristics
        boolean foundExpectedJourney = false;
        for (Journey journey : journeys) {
            if (journey.depStop().name().equals("Ecublens VD, EPFL") &&
                    (journey.arrStop().name().equals("Gruyères") ||
                            journey.arrStop().name().equals("Gruyères, gare"))) {

                // Verify this journey has the expected structure
                List<String> expectedStations = Arrays.asList("Ecublens VD, EPFL", "Renens VD", "Lausanne", "Romont FR", "Bulle", "Gruyères");
                List<String> actualStations = new ArrayList<>();

                // Extract station names from transport legs only
                for (Journey.Leg leg : journey.legs()) {
                    if (leg instanceof Journey.Leg.Transport) {
                        actualStations.add(leg.depStop().name());
                    }
                }
                actualStations.add(journey.arrStop().name());

                // Check if this journey matches our expected route
                if (actualStations.containsAll(expectedStations)) {
                    foundExpectedJourney = true;

                    // More detailed checks about this specific journey
                    // For example, verify transport types, reasonable durations, etc.
                    for (Journey.Leg leg : journey.legs()) {
                        if (leg instanceof Journey.Leg.Transport transport) {
                            // Verify transport leg has reasonable duration
                            assertTrue(transport.duration().toMinutes() < 120,
                                    "Transport leg duration should be reasonable");

                            if (transport.depStop().name().equals("Lausanne")) {
                                assertEquals("TRAIN", transport.vehicle().toString(),
                                        "Should be a train from Lausanne");
                            }
                        } else if (leg instanceof Journey.Leg.Foot foot) {
                            // Verify walking legs aren't too long
                            assertTrue(foot.duration().toMinutes() < 30,
                                    "Walking leg duration should be reasonable");
                        }
                    }

                    break;
                }
            }
        }

        assertTrue(foundExpectedJourney, "Should find a journey from Lausanne to Gruyères with expected characteristics");
    }


    // Helper method to validate journey structure
    private void validateJStructure(Journey journey) {
        List<Journey.Leg> legs = journey.legs();

        // At least one leg
        assertTrue(legs.size() > 0, "Journey must have at least one leg");

        // Alternating leg types
        for (int i = 1; i < legs.size(); i++) {
            assertNotEquals(
                    legs.get(i).getClass(),
                    legs.get(i-1).getClass(),
                    "Adjacent legs must be of different types"
            );
        }

        // Check time continuity
        for (int i = 0; i < legs.size(); i++) {
            Journey.Leg leg = legs.get(i);
            assertFalse(
                    leg.depTime().isAfter(leg.arrTime()),
                    "Arrival time cannot be before departure time"
            );

            if (i > 0) {
                assertFalse(
                        leg.depTime().isBefore(legs.get(i-1).arrTime()),
                        "Departure time cannot be before previous leg's arrival time"
                );
            }
        }
    }
}