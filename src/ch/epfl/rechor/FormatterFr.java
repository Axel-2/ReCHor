package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public final class FormatterFr {


    public static String formatDuration(Duration duration) {

        return "s";
    }

    public static String formatTime(LocalDateTime dateTime) {

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_AMPM)
                .appendLiteral('h')
                .appendValue(ChronoField.MINUTE_OF_DAY)
                .toFormatter();

        return fmt.format(dateTime);


    }

    public static String formatPlatformName(Stop stop) {
        return "s";


    }

    public static String formatLeg(Journey.Leg.Foot footLeg) {

        return "s";

    }

    public static String formatLeg(Journey.Leg.Transport leg) {
        return "s";


    }

    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {

        return "s";


    }


}
