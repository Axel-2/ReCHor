package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Formateur, contient toutes les méthodes de formatage
 *
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class FormatterFr {

    // Pour rendre la classe non instantiable
    private FormatterFr() {}

    /**
     * Formate une durée pour obtenir le format suivant : 12 min ou alors 1 h 3 min
     * @param duration (une durée)
     * @return un string immuable de la durée formatée
     */
    public static String formatDuration(Duration duration) {

        // récupération des minutes
        int minutes = duration.toMinutesPart();

        // Il ne faut pas utiliser toPart car on peut
        // dépasser 24h de trajet
        long hours = duration.toHours();

        StringBuilder builder = new StringBuilder();

        // S'il y a des heures, on les ajoute
        if (hours != 0) {
            builder.append(hours)
                    .append(" h ");
        }

        // On ajoute forcément les minutes
        builder.append(minutes).append(" min");

        return builder.toString();
    }

    /**
     * Formate une heure pour obtenir le format suivant : 12h30 ou alors 0h05
     * @param dateTime (heure non formatée)
     * @return (String immuable de l'heure formatée)
     */
    public static String formatTime(LocalDateTime dateTime) {

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral('h')
                .appendValue(ChronoField.MINUTE_OF_HOUR,2)
                .toFormatter();

        return fmt.format(dateTime);


    }

    /**
     * Formate le nom d'une voie ou d'un quai selon ce que représente la plateforme
     * @param stop (arrêt dont on veut formater l'attribut "platformName")
     * @return String immuable du nom de plateforme formaté
     */
    public static String formatPlatformName(Stop stop) {

        // Si la gare ne contient pas de voie/quai ou bien le nom est vide,
        // on retourne simplement une chaine vide
        if (stop.platformName() == null || stop.platformName().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        // Il faut différencier le cas d'une voie ou d'un quai
        if (Character.isDigit(stop.platformName().charAt(0))) {
            builder.append("voie ");
            builder.append(stop.platformName());
        } else {
            builder.append("quai ");
            builder.append(stop.platformName());
        }

        return builder.toString();
    }

    /**
     * Formate une étape à pied, et indique s'il s'agit d'un changement ou d'un trajet
     * @param footLeg (étape à pied)
     * @return String immuable de l'étape à pied formatée
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {

        StringBuilder builder = new StringBuilder();

        builder.append(footLeg.isTransfer() ? "changement " : "trajet à pied ");
        builder.append("(");
        builder.append(formatDuration(footLeg.duration()));
        builder.append(")");

        return builder.toString();

    }

    /**
     * Formate une étape en transport selon l'affichage désiré
     * @param leg (étape en transport)
     * @return String immuable de l'étape de transport formaté
     */
    public static String formatLeg(Journey.Leg.Transport leg) {

        // Exemple de formatage :
        // "16h26 Renens VD (voie 4) → Lausanne (arr. 16h33 voie 5)"

        // Heure et arrivée
        String depTimeString = formatTime(leg.depTime());
        String arrTimeString = formatTime(leg.arrTime());

        StringBuilder builder = new StringBuilder()
                .append(depTimeString)
                .append(" ")
                .append(leg.depStop().name());

        // s'il existe un formatage pour le quai de départ, on l'
        // ajoute dans des parenthèses
        if (!formatPlatformName(leg.depStop()).isEmpty()) {
            builder.append(" (")
                    .append(formatPlatformName(leg.depStop()))
                    .append(")");
        }

        builder.append(" → ")
                .append(leg.arrStop().name())
                .append(" (arr. ")
                .append(arrTimeString);

        // Il faut ajouter un espace vide, s'il y a un nom de plateforme
        // pour l'arrivée
        if (!formatPlatformName(leg.arrStop()).isEmpty()) {
            builder.append(" ").append(formatPlatformName(leg.arrStop()));
        }

        builder.append(")");

        return builder.toString();

    }

    /**
     * Formate le titre d'une route selon le format suivant : IR 15 Direction Luzern par exemple
     * @param transportLeg (étape de transport)
     * @return String immuable de du nouvel affichage de la route
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {

        // Exemple:
        // "IR 15 Direction Luzern"

        StringBuilder string = new StringBuilder()
                .append(transportLeg.route())
                .append(" Direction ")
                .append(transportLeg.destination());

        return string.toString();


    }


}
