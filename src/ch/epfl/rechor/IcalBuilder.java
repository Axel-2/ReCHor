package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Constructeur de calendrier (format ics)
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class IcalBuilder {

    // composants commencés, mais pas terminés
    private ArrayList<Component> components = new ArrayList<Component>();

    private StringBuilder stringBuilder = new StringBuilder();

    /**
     * Représentent un composant ou un objet
     */
    public enum Component {
        VCALENDAR,
        VEVENT
    }

    /**
     * Représentent un nom d'une ligne
     */
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

    /**
     * Ajoute à l'événement en cours de construction une ligne dont le nom et la valeur sont ceux donnés
     * @param name titre d'une ligne
     * @param value texte de la ligne
     * @return un ce même builder
     */
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

            // après le premier tour de boucle on passe la limite de ligne à 74 char
            // car l'espace ajouté par le pliage compte comme caractère ne plus
            if (currentIndex > 0) {
                maxStringLength = 74;
            }

            //TODO le commentaire d'en dessous est incomplet
            // On doit
            int currentEnd = Math.min(currentIndex+maxStringLength, totalLength);

            stringBuilder
                    .append(initialString, currentIndex, currentEnd)
            ;

            // Au dernier tour de boucle on n'ajoute pas d'espace supplémentaire
            // en début de ligne
            if (currentEnd < initialString.length()) {
                // saut de ligne avec espace à la fin
                stringBuilder.append("\r\n ");
            } else {
                // saut de ligne sans espace à la fin
                stringBuilder.append("\r\n");
            }

        }

        return this;

    }

    /**
     * Ajoute à l'événement en cours de construction une ligne dont le nom
     * est celui donné et la valeur est la représentation textuelle de la date/heure donnée
     * @param name titre d'une ligne
     * @param dateTime date qui va être formatée
     * @return ce même builder
     */
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

    /**
     * Commence un composant en ajoutant une ligne dont le nom est BEGIN et la valeur est le nom du composant donné
     * @param component composant donné
     * @return ce même builder
     */
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

    /**
     *  Termine le dernier composant qui a été commencé précédemment par begin mais pas encore terminé
     *  par un appel à end précédent, ou lève une IllegalArgumentException s'il n'y en a aucun
     * @return ce même builder
     */
    public IcalBuilder end() {

        //TODO le requireNonNull n'est pas demandé si ?
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


    /**
     * Transforme le builder en string immuable
     * @return String immuable
     */
    public String build() {
        //TODO il manque un checkconditions je crois
        return stringBuilder.toString();
    }
}
