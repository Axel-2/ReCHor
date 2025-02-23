package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.StringJoiner;

class GptJourneyIcalConverterTest {

    /**
     * Génère un exemple de trajet avec plusieurs étapes.
     */
    private static List<Journey.Leg> exampleLegs() {
        var s1 = new Stop("Ecublens VD, EPFL", null, 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", null, 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var l1 = new Journey.Leg.Transport(s1, d.atTime(16, 13), s2, d.atTime(16, 19), List.of(), Vehicle.METRO, "m1", "Renens VD, gare");
        var l2 = new Journey.Leg.Foot(s2, d.atTime(16, 19), s3, d.atTime(16, 22));
        var l3 = new Journey.Leg.Transport(s3, d.atTime(16, 26), s4, d.atTime(16, 33), List.of(), Vehicle.TRAIN, "R4", "Bex");
        var l4 = new Journey.Leg.Foot(s4, d.atTime(16, 33), s5, d.atTime(16, 38));
        var l5 = new Journey.Leg.Transport(s5, d.atTime(16, 40), s6, d.atTime(17, 13), List.of(), Vehicle.TRAIN, "IR15", "Luzern");

        return List.of(l1, l2, l3, l4, l5);
    }

    /**
     * Teste la conversion d'un trajet en iCalendar.
     */
    @Test
    void toIcalendarWorksOnExampleLegs() {
        var journey = new Journey(exampleLegs());
        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        // Générer dynamiquement l'expectedIcal
        String expectedIcal = generateExpectedIcal(journey);

        // Remplacer les champs dynamiques
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        assertEquals(expectedIcal.strip(), actualIcal.strip());
    }

    /**
     * Vérifie que la structure du fichier `.ics` est valide.
     */
    @Test
    void toIcalendarGeneratesValidIcsFile() {
        var journey = new Journey(exampleLegs());
        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        assertTrue(actualIcal.contains("BEGIN:VCALENDAR"));
        assertTrue(actualIcal.contains("BEGIN:VEVENT"));
        assertTrue(actualIcal.contains("SUMMARY:"));
        assertTrue(actualIcal.contains("DESCRIPTION:"));
        assertTrue(actualIcal.contains("END:VEVENT"));
        assertTrue(actualIcal.contains("END:VCALENDAR"));
    }

    /**
     * Teste un trajet avec un seul segment.
     */
    @Test
    void toIcalendarHandlesSingleLegJourney() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s2 = new Stop("Genève", "5", 6.143158, 46.204391);

        var leg = new Journey.Leg.Transport(s1, d.atTime(8, 00), s2, d.atTime(9, 00), List.of(), Vehicle.TRAIN, "IC5", "Zürich HB");
        var journey = new Journey(List.of(leg));

        String actualIcal = JourneyIcalConverter.toIcalendar(journey);
        assertTrue(actualIcal.contains("SUMMARY:Lausanne -> Genève"));
    }

    /**
     * Vérifie si un trajet entièrement à pied est bien formaté.
     */
    @Test
    void toIcalendarHandlesWalkingOnlyJourney() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Rue de la Gare", null, 6.566141, 46.522196);
        var s2 = new Stop("Université", null, 6.578519, 46.537619);

        var leg = new Journey.Leg.Foot(s1, d.atTime(8, 00), s2, d.atTime(8, 10));
        var journey = new Journey(List.of(leg));

        String actualIcal = JourneyIcalConverter.toIcalendar(journey);
        assertTrue(actualIcal.contains("SUMMARY:Rue de la Gare -> Université"));
    }

    /**
     * Vérifie la gestion des caractères spéciaux.
     */
    @Test
    void toIcalendarHandlesSpecialCharacters() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Écublens VD", null, 6.566141, 46.522196);
        var s2 = new Stop("Morges–Saint-Jean", null, 6.499769, 46.511011);

        var leg = new Journey.Leg.Foot(s1, d.atTime(8, 00), s2, d.atTime(8, 20));
        var journey = new Journey(List.of(leg));

        String actualIcal = JourneyIcalConverter.toIcalendar(journey);
        assertTrue(actualIcal.contains("Écublens VD -> Morges–Saint-Jean"));
    }

    /**
     * Génère dynamiquement un fichier `.ics` attendu à partir d'un `Journey`.
     */
    private static String generateExpectedIcal(Journey journey) {
        IcalBuilder builder = new IcalBuilder()
                .begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, "SHOULDNTBETHESAME")
                .add(IcalBuilder.Name.DTSTAMP, "SHOULDNTBETHESAME")
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime())
                .add(IcalBuilder.Name.SUMMARY, journey.depStop().name() + " -> " + journey.arrStop().name());

        // Générer la description
        StringJoiner j = new StringJoiner("\n");
        for (Journey.Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot f -> j.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> j.add(FormatterFr.formatLeg(t));
            }
        }
        builder.add(IcalBuilder.Name.DESCRIPTION, j.toString());

        return builder.end().end().build();
    }
}