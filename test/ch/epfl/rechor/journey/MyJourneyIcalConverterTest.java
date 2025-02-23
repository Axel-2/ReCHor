package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyJourneyIcalConverterTest {

    private static List<Journey.Leg> exampleLegs() {
        var s1 = new Stop("Ecublens VD, EPFL", null, 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", null, 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);

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

        return List.of(l1, l2, l3, l4, l5);
    }

    @Test
    void toIcalendarWorksOnExampleLegs() {
        // Créer un exemple de trajet basé sur exampleLegs() (terminant à Romont FR)
        var journey = new Journey(exampleLegs());

        // Convertir en iCalendar
        String actualIcal = JourneyIcalConverter.toIcalendar(journey);

        // Définir l'output attendu avec Romont FR comme destination finale
        String expectedIcal = """
        BEGIN:VCALENDAR
        VERSION:2.0
        PRODID:ReCHor
        BEGIN:VEVENT
        UID:SHOULDNTBETHESAME
        DTSTAMP:SHOULDNTBETHESAME
        DTSTART:20250218T161300
        DTEND:20250218T171300
        SUMMARY:Ecublens VD, EPFL -> Romont FR
        DESCRIPTION:16h13 Ecublens VD, EPFL -> Renens VD, gare (arr. 16h19)
         à pied (3 min)
         16h26 Renens VD (voie 4) -> Lausanne (arr. 16h33 voie 5)
         changement (5 min)
         16h40 Lausanne (voie 1) -> Romont FR (arr. 17h13 voie 2)
        END:VEVENT
        END:VCALENDAR
        """;

        // Remplacer les champs dynamiques (UID, DTSTAMP) pour éviter les erreurs de comparaison
        actualIcal = actualIcal.replaceAll("UID:.*", "UID:SHOULDNTBETHESAME");
        actualIcal = actualIcal.replaceAll("DTSTAMP:.*", "DTSTAMP:SHOULDNTBETHESAME");

        // Vérifier que l'output correspond à l'attendu
        assertEquals(expectedIcal.strip(), actualIcal.strip());
    }


}
