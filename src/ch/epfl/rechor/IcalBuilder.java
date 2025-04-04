package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Batisseur d'événement au format iCalendar (format ics)
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class IcalBuilder {

    // liste qui contient les composants commencés, mais pas terminés
    private final ArrayList<Component> components = new ArrayList<Component>();

    // String en cours de construction
    private final StringBuilder icalString = new StringBuilder();

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
        String stringToAdd = new StringBuilder()
                .append(name)
                .append(':')
                .append(value)
                .toString();

        int totalLength = stringToAdd.length();

        // valeur maximale pour une ligne
        int maxStringLength = 75;

        // On itère en créant des lignes de taille 75
        // tant qu'il reste des characters dans la chaine initiale
        for (int currentIndex = 0; currentIndex < totalLength; currentIndex += maxStringLength) {


            if (currentIndex > 0) {
                // après le premier tour de boucle, on passe la limite de ligne à 74, car
                // l'espace ajouté par le pliage compte comme caractère de plus
                maxStringLength = 74;
            }

            // On doit calculer l'indice de fin pour découper la chaîne,
            // en s'assurant de ne pas dépasser la longueur totale de la chaîne initiale.
            int currentEnd = Math.min(currentIndex+maxStringLength, totalLength);

            // On ajoute à icalString la sous-chaîne de stringToAdd comprise entre currentIndex  et currentEnd,
            icalString.append(stringToAdd, currentIndex, currentEnd);

            // Au dernier tour de boucle, on n'ajoute pas d'espace supplémentaire
            // en début de ligne
            if (currentEnd < stringToAdd.length()) {
                // saut de ligne avec espace en début de ligne
                icalString.append("\r\n ");
            } else {
                // saut de ligne sans espace en début de nouvelle ligne
                icalString.append("\r\n");
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

        // on crée un formatteur avec le format demandé
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                // année sur 4 chiffres
                .appendValue(ChronoField.YEAR, 4)
                // mois sur 2 chiffres
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                // le jour du mois, sur 2 chiffres (de 01 à 31)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                // la lettre T
                .appendLiteral('T')
                // les heures, sur 2 chiffres (de 00 à 23)
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                // les minutes, sur 2 chiffres (de 00 à 59)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                // les secondes, sur 2 chiffres (de 00 à 59).
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();

        // Ici pas besoin de plier une ligne, car un dateTime ne sera jamais trop long
        icalString
                .append(name)
                .append(':')
                .append(fmt.format(dateTime))
                // CRLF
                .append("\r\n");

        return this;
    }

    /**
     * Commence un composant en ajoutant une ligne dont le nom est BEGIN et la valeur est le nom du composant donné
     * @param component composant donné
     * @return ce même builder
     */
    public IcalBuilder begin(Component component) {

        icalString
                .append("BEGIN")
                .append(':')
                // nom du composant
                .append(component.name())
                // CRLF
                .append("\r\n");

        // ajouter dans la liste components pour que la fonction
        // end() puisse savoir avec quel composant fermer
        components.add(component);

        return this;

    }

    /**
     *  Termine le dernier composant qui a été commencé précédemment par begin mais pas encore terminé
     *  par un appel à end précédent, ou lève une IllegalArgumentException s'il n'y en a aucun
     * @return ce même builder
     */
    public IcalBuilder end() {

        // lève une IllegalArgumentException si aucun composant
        // n'est dans la liste
        Preconditions.checkArgument(!components.isEmpty());

        // on prend le dernier composant de la liste
        Component endComponent = components.removeLast();
        icalString
                .append("END")
                .append(':')
                .append(endComponent.name())
                // CRLF
                .append("\r\n");

        return this;

    }


    /**
     * Transforme le builder en string immuable
     * @return String immuable
     */
    public String build() {

        // Lève une IllegalArgumentException si un composant qui a été commencé par un appel à begin n'a, à ce stade,
        // pas été terminé par un appel à end.
        // C'est le cas si la liste components n'est pas vide
        Preconditions.checkArgument(components.isEmpty());

        // on retourne la string finale
        return icalString.toString();
    }
}
