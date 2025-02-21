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

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.MINUTE_OF_DAY)
                .appendLiteral(" min")
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('/')
                .appendValue(ChronoField.YEAR)
                .toFormatter();




        return "s";

    }

    public static String formatTime(LocalDateTime dateTime) {

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_AMPM)
                .appendLiteral('/')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('/')
                .appendValue(ChronoField.YEAR)
                .toFormatter();

        return "s";


    }

    public static String formatPlatformName(Stop stop) {
        return "s";


    }

    public static String formatLeg(Journey.Leg.Foot footLeg) {

        // changement (5 min)
        // trajet à pied (3 min)

        return "s";

    }

    public static String formatLeg(Journey.Leg.Transport leg) {

        // 16h26 Renens VD (voie 4) → Lausanne (arr. 16h33 voie 5)
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral("h")
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .toFormatter();

        // depTime
        String depTimeString = fmt.format(leg.depTime());
        String arrTimeString = fmt.format(leg.arrTime());

        StringBuilder builder = new StringBuilder()
                .append(depTimeString)
                .append(" ")
                .append(leg.depStop().name());


        if (leg.depStop().platformName() != null) {
            builder.append(" (voie ")
                    .append(leg.depStop().platformName())
                    .append(")");
        }

        builder.append(" → ")
                .append(leg.arrStop().name())
                .append(" (arr. ")
                .append(arrTimeString);

                if (leg.arrStop())
                .append(" voie ")
                .append(leg.arrStop().platformName())
                .append(")")
                .toString();

        return builder.toString();

    }

    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {

        // IR 15 Direction Luzern

        StringBuilder string = new StringBuilder()
                .append(transportLeg.route())
                .append(" Direction ")
                .append(transportLeg.destination());


        return string.toString();


    }


}
