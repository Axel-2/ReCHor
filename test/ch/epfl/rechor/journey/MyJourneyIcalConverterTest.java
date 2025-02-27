package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;


class MyJourneyIcalConverterTest {

    // Example pris des anciens tests et légérement modifié
    private static List<Journey.Leg> exampleLegs() {
        var s1 = new Stop("Ecublens VD, EPFL", null, 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", null, 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);
        var s7 = new Stop("Romont FR", "1", 6.911811, 46.693508);
        var s8 = new Stop("Bulle", "2", 6.911811, 46.693508);
        var s9 = new Stop("Bulle", "4", 6.911811, 46.693508);
        var s10 = new Stop("Gruyères", "2",6.911811, 46.693508);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);

        var l1 = new Journey.Leg.Transport(
                s1,
                d.atTime(16, 13),
                s2,
                d.atTime(16, 19),
                List.of(),
                Vehicle.METRO,
                "m1",
                "Renens VD, gare");

        var l2 = new Journey.Leg.Foot(s2, d.atTime(16, 19), s3, d.atTime(16, 22));

        var l3 = new Journey.Leg.Transport(
                s3,
                d.atTime(16, 26),
                s4,
                d.atTime(16, 33),
                List.of(),
                Vehicle.TRAIN,
                "R4",
                "Bex");

        var l4 = new Journey.Leg.Foot(s4, d.atTime(16, 33), s5, d.atTime(16, 38));

        var l5 = new Journey.Leg.Transport(
                s5,
                d.atTime(16, 40),
                s6,
                d.atTime(17, 13),
                List.of(),
                Vehicle.TRAIN,
                "IR15",
                "Luzern");

        // Changement à Romont FR : de la voie 2 à la voie 1 (3 min)
        var l6 = new Journey.Leg.Foot(
                s6,
                d.atTime(17, 13),
                s7,
                d.atTime(17, 16));

        // Transport de Romont FR (voie 1) à Bulle (arr. 17h41 voie 2)
        var l7 = new Journey.Leg.Transport(
                s7,
                d.atTime(17, 22),
                s8,
                d.atTime(17, 41),
                List.of(),
                Vehicle.TRAIN,
                "R?",
                "Bulle");

        // Changement à Bulle : de la voie 2 à la voie 4 (3 min)
        var l8 = new Journey.Leg.Foot(
                s8,
                d.atTime(17, 41),
                s9,
                d.atTime(17, 44));

        // Transport de Bulle (voie 4) à Gruyères (arr. 17h57 voie 2)
        var l9 = new Journey.Leg.Transport(
                s9,
                d.atTime(17, 50),
                s10,
                d.atTime(17, 57),
                List.of(),
                Vehicle.TRAIN,
                "R?",
                "Gruyères");


        return List.of(l1, l2, l3, l4, l5, l6, l7, l8, l9);
    }

    @Test
    void toIcalendarWorksOnExampleLegs() {

        // Créer un exemple de trajet basé sur exampleLegs()
        var journey = new Journey(exampleLegs());

        // Convertir en iCalendar
        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        String expectedIcal =
                "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:ReCHor\r\n" +
                "BEGIN:VEVENT\r\n" +
                "UID:SHOULDNTBETHESAME\r\n" +
                "DTSTAMP:SHOULDNTBETHESAME\r\n" +
                "DTSTART:20250218T161300\r\n" +
                "DTEND:20250218T175700\r\n"+
                "SUMMARY:Ecublens VD, EPFL → Gruyères\r\n" +
                "DESCRIPTION:16h13 Ecublens VD, EPFL → Renens VD, gare (arr. 16h19)\\ntrajet \r\n" +
                " à pied (3 min)\\n16h26 Renens VD (voie 4) → Lausanne (arr. 16h33 voie 5)\\nc\r\n" +
                " hangement (5 min)\\n16h40 Lausanne (voie 1) → Romont FR (arr. 17h13 voie 2)\r\n" +
                " \\nchangement (3 min)\\n17h22 Romont FR (voie 1) → Bulle (arr. 17h41 voie 2)\r\n" +
                " \\nchangement (3 min)\\n17h50 Bulle (voie 4) → Gruyères (arr. 17h57 voie 2)\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";


        // Remplacer les champs dynamiques (UID, DTSTAMP) pour éviter les erreurs de comparaison
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        // Vérifier que l'output correspond à l'attendu
        assertEquals(expectedIcal, actualIcal);
    }

    @Test
    void toIcalendarWorksOnSingleLegJourney() {
        // Définir deux arrêts pour le trajet
        var s1 = new Stop("Geneva", null, 6.143158, 46.204391);
        var s2 = new Stop("Lausanne", null, 6.633597, 46.519962);

        // Définir la date du trajet
        var d = LocalDate.of(2025, Month.FEBRUARY, 20);

        // Créer une étape de transport unique de Geneva à Lausanne, de 10h00 à 11h00
        var leg = new Journey.Leg.Transport(
                s1,
                d.atTime(10, 0),
                s2,
                d.atTime(11, 0),
                List.of(),
                Vehicle.TRAIN,
                "T1",
                "Geneva-Lausanne Express");

        // Construire le trajet avec une seule étape
        var journey = new Journey(List.of(leg));

        // Convertir le trajet en format iCalendar
        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        String expectedIcal =
                "BEGIN:VCALENDAR\r\n" +
                        "VERSION:2.0\r\n" +
                        "PRODID:ReCHor\r\n" +
                        "BEGIN:VEVENT\r\n" +
                        "UID:SHOULDNTBETHESAME\r\n" +
                        "DTSTAMP:SHOULDNTBETHESAME\r\n" +
                        "DTSTART:20250220T100000\r\n" +
                        "DTEND:20250220T110000\r\n" +
                        "SUMMARY:Geneva → Lausanne\r\n" +
                        "DESCRIPTION:10h00 Geneva → Lausanne (arr. 11h00)\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR\r\n";

        // Remplacer les champs dynamiques (UID, DTSTAMP) pour éviter les erreurs de comparaison
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        // Vérifier que l'output correspond à l'attendu
        assertEquals(expectedIcal, actualIcal);
    }

    @Test
    void toIcalendarWorksForJourneySpanningMidnight() {
        // Arrêts pour le trajet
        var s1 = new Stop("Paris", "1", 2.3522, 48.8566);
        var s2 = new Stop("Lyon", "2", 4.8357, 45.7640);

        // Le trajet démarre le 18 février 2025 à 23h55 et se termine le 19 février 2025 à 00h10
        var dStart = LocalDate.of(2025, Month.FEBRUARY, 18);
        var dEnd = LocalDate.of(2025, Month.FEBRUARY, 19);
        var leg = new Journey.Leg.Transport(
                s1,
                dStart.atTime(23, 55),
                s2,
                dEnd.atTime(0, 10),
                List.of(),
                Vehicle.TRAIN,
                "T2",
                "Express"
        );

        var journey = new Journey(List.of(leg));

        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        String expectedIcal =
                "BEGIN:VCALENDAR\r\n" +
                        "VERSION:2.0\r\n" +
                        "PRODID:ReCHor\r\n" +
                        "BEGIN:VEVENT\r\n" +
                        "UID:SHOULDNTBETHESAME\r\n" +
                        "DTSTAMP:SHOULDNTBETHESAME\r\n" +
                        "DTSTART:20250218T235500\r\n" +
                        "DTEND:20250219T001000\r\n" +
                        "SUMMARY:Paris → Lyon\r\n" +
                        "DESCRIPTION:23h55 Paris (voie 1) → Lyon (arr. 0h10 voie 2)\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR\r\n";

        // Remplacer les champs dynamiques (UID, DTSTAMP)
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        assertEquals(expectedIcal, actualIcal);
    }

    @Test
    void toIcalendarWorksOnZeroDurationLeg() {
        // Arrêt unique utilisé pour le départ et l'arrivée
        var s1 = new Stop("X", null, 0.0, 0.0);
        var d = LocalDate.of(2025, Month.MARCH, 5);

        // Créer une étape de transport avec une durée nulle (10h00 à 10h00)
        var leg = new Journey.Leg.Transport(
                s1,
                d.atTime(10, 0),
                s1,
                d.atTime(10, 0),
                List.of(),
                Vehicle.BUS,
                "B1",
                "Loop"
        );
        var journey = new Journey(List.of(leg));

        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        String expectedIcal =
                "BEGIN:VCALENDAR\r\n" +
                        "VERSION:2.0\r\n" +
                        "PRODID:ReCHor\r\n" +
                        "BEGIN:VEVENT\r\n" +
                        "UID:SHOULDNTBETHESAME\r\n" +
                        "DTSTAMP:SHOULDNTBETHESAME\r\n" +
                        "DTSTART:20250305T100000\r\n" +
                        "DTEND:20250305T100000\r\n" +
                        "SUMMARY:X → X\r\n" +
                        "DESCRIPTION:10h00 X → X (arr. 10h00)\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR\r\n";

        // Remplacer les champs dynamiques (UID, DTSTAMP)
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        assertEquals(expectedIcal, actualIcal);
    }

    @Test
    void toIcalendarWorksWithSeconds() {
        // Définition de deux arrêts
        var s1 = new Stop("Bern", null, 7.4474, 46.9480);
        var s2 = new Stop("Zurich", null, 8.5417, 47.3769);
        var d = LocalDate.of(2025, Month.APRIL, 1);

        // Créer une étape de transport incluant des secondes (09:15:30 à 10:45:15)
        var leg = new Journey.Leg.Transport(
                s1,
                d.atTime(9, 15, 30),
                s2,
                d.atTime(10, 45, 15),
                List.of(),
                Vehicle.TRAIN,
                "IC",
                "Bern-Zurich"
        );
        var journey = new Journey(List.of(leg));

        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        // On s'attend à voir les secondes dans les champs DTSTART et DTEND,
        // tandis que la description reste formatée en hhmm.
        String expectedIcal =
                "BEGIN:VCALENDAR\r\n" +
                        "VERSION:2.0\r\n" +
                        "PRODID:ReCHor\r\n" +
                        "BEGIN:VEVENT\r\n" +
                        "UID:SHOULDNTBETHESAME\r\n" +
                        "DTSTAMP:SHOULDNTBETHESAME\r\n" +
                        "DTSTART:20250401T091530\r\n" +
                        "DTEND:20250401T104515\r\n" +
                        "SUMMARY:Bern → Zurich\r\n" +
                        "DESCRIPTION:9h15 Bern → Zurich (arr. 10h45)\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR\r\n";

        // Remplacer les champs dynamiques (UID, DTSTAMP)
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        assertEquals(expectedIcal, actualIcal);
    }
}
