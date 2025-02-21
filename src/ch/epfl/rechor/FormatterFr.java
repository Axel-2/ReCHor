package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public final class FormatterFr {


    public static String formatDuration(Duration duration) {

        int minutes = duration.toMinutesPart();

        // Il ne faut pas utiliser toPart car on peut
        // dépasser 24h de trajet
        long hours = duration.toHours();


        StringBuilder builder = new StringBuilder();

        if (hours != 0) {
            builder.append(hours)
                    .append(" h ");
        }

        // On met forcément les minutes
        builder.append(minutes).append(" min");

        return builder.toString();
    }

    public static String formatTime(LocalDateTime dateTime) {


        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral('h')
                .appendValue(ChronoField.MINUTE_OF_HOUR,2)
                .toFormatter();

        return fmt.format(dateTime);


    }

    public static String formatPlatformName(Stop stop) {

        StringBuilder builder = new StringBuilder();

        if (Character.isDigit(stop.platformName().charAt(0))){
            builder.append("voie ");
            builder.append(stop.platformName());
        }else{
            builder.append("quai ");
            builder.append(stop.platformName());
        }

        return builder.toString();


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

        if (leg.arrStop().platformName() != null) {
            builder.append(" voie ")
                    .append(leg.arrStop().platformName());
        }

        builder.append(")");

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
