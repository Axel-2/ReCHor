package ch.epfl.rechor.journey;


import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

public final class JourneyIcalConverter {

    static String toIcalendar(Journey journey){

        // Création du builder
        IcalBuilder builder = new IcalBuilder();

        // Begin
        builder.begin(IcalBuilder.Component.VCALENDAR);

        // Version
        builder.add(IcalBuilder.Name.VERSION, "2.0");

        // Prodid
        builder.add(IcalBuilder.Name.PRODID, "ReCHor");

        // Begin 2
        builder.begin(IcalBuilder.Component.VEVENT);

        // UID
        builder.add(IcalBuilder.Name.UID, UUID.randomUUID().toString());

        // DTSTAMP

        // DTSTART

        // DTEND

        // SUMMARY

        // DESCRIPTION
        StringJoiner j = new StringJoiner("\n");

        // END 2
        builder.end();

        // END
        builder.end();

        return "s";
    }
}
