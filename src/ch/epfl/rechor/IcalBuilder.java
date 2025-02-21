package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
                    // Ne pas oublier d'ajouter le saut de ligne
            ;

            stringBuilder.append("\r\n");


        }

        return this;

    }

    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        return this;
    }

    public IcalBuilder begin(Component component) {

        return this;

    }

    public IcalBuilder end() {
        return this;

    }


    public String build() {
        return stringBuilder.toString();
    }
}
