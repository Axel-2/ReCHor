package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Objects;

public final class IcalBuilder {

    // composants commencés mais pas terminés
    private ArrayList<Component> components = new ArrayList<Component>();

    private StringBuilder stringBuilder = new StringBuilder();

    public enum Component {
        VCALENDAR,
        VEVENT
    }

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION
    }

    public IcalBuilder add(Name name, String value) {

        // String initiale sans que les lignes
        // soient découpées
        String initialString = new StringBuilder()
                .append(name)
                .append(':')
                .append(value)
                .toString();

        int totalLength = initialString.length();

        // valeur maximal pour une ligne
        int maxStringLength = 75;

        // On itère en créant des lignes de taille 75
        // tant qu'il reste des charactères dans la string initiale
        for (int currentIndex = 0; currentIndex < totalLength; currentIndex += maxStringLength) {


            // On doit
            int currentEnd = Math.min(currentIndex+maxStringLength, totalLength);

            stringBuilder
                    .append(initialString, currentIndex, currentEnd)
            ;

            if (currentEnd < initialString.length()) {
                // saut de ligne avec espace
                stringBuilder.append("\r\n ");
            } else {
                // saut de ligne sans espace à la fin
                stringBuilder.append("\r\n");
            }

        }

        return this;

    }

    public IcalBuilder add(Name name, LocalDateTime dateTime) {

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();

        stringBuilder
                .append(name)
                .append(':')
                .append(fmt.format(dateTime))
                .append("\r\n");

        return this;
    }

    public IcalBuilder begin(Component component) {

        stringBuilder
                .append("BEGIN")
                .append(':')
                .append(component.name())
                .append("\r\n");

        // ajouter dans la liste
        components.add(component);

        return this;

    }

    public IcalBuilder end() {

        Objects.requireNonNull(components);
        Preconditions.checkArgument(!components.isEmpty());

        Component endComponent = components.removeLast();

        stringBuilder
                .append("END")
                .append(':')
                .append(endComponent.name())
                .append("\r\n");

        return this;

    }


    public String build() {
        return stringBuilder.toString();
    }
}
