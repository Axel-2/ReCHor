package ch.epfl.rechor.journey;


import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

public final class JourneyIcalConverter {

    static String toIcalendar(Journey journey){

        // Création du builder
        IcalBuilder builder = new IcalBuilder()

                // Begin
                .begin(IcalBuilder.Component.VCALENDAR)

                // Version
                .add(IcalBuilder.Name.VERSION, "2.0")

                // Prodid
                .add(IcalBuilder.Name.PRODID, "ReCHor")

                // Begin 2
                .begin(IcalBuilder.Component.VEVENT)

                // UID
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())

                // DTSTAMP
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now())

                // DTSTART
                .add(IcalBuilder.Name.DTSTART, journey.legs().getFirst().depTime())

                // DTEND
                .add(IcalBuilder.Name.DTEND, journey.legs().getLast().arrTime())

                // SUMMARY
                .add(IcalBuilder.Name.SUMMARY, journey.legs().getFirst().depStop().name() + " -> " +
                        journey.legs().getLast().arrStop().name());


        // DESCRIPTION
        StringJoiner j = new StringJoiner("\n");

        // END 2
        builder.end()

        // END
        .end();

        return "s";
    }
}
