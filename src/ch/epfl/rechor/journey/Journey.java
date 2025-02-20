package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Journey(List<Leg> legs) {

    // Constructeur compact
    public Journey {


        // Condition 1 : La liste ne doit pas être nulle
        Objects.requireNonNull(legs, "legs is null");

        // Le reste des conditions se trouvent au sein de la boucle
        // Pour chaque étapes consécutives :
        for (int i = 1; i < legs.size(); i++) {

            // Condition 2 : Les étapes doivent s'alterner
            Preconditions.checkArgument(legs.get(i).getClass() != legs.get(i - 1).getClass());

            // Condition 3 : Sauf début, l'instant de départ est après l'arrivée du précédent
            Preconditions.checkArgument(legs.get(i).depTime().isBefore(legs.get(i - 1).arrTime()));

            // Condition 4 : Sauf début, le départ = l'arivée des étapes précédentes
            Preconditions.checkArgument(!legs.get(i).depTime().isEqual(legs.get(i - 1).arrTime()));
        }

        // Si toutes les conditions sont validées
        // On rend la liste immuable, c'est tout bon
        legs = List.copyOf(legs);

    }

    // TODO : "Journey offre ces classes publiques. Mais Leg aussi ??
    // Oui car c'est différent, ici c'est voyage alors que dans Leg
    // c'est étape ! donc refaire les trucs ici aussi.

    // Retourne l'arrêt de départ du voyage, c.-à-d. celui de sa première étape,
    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    // Retourne l'arrêt d'arrivée du voyage, c.-à-d. celui de sa dernière étape,
    public Stop arrStop() {
        return legs.getLast().arrStop();
    };

    // retourne la date/heure de début du voyage, c.-à-d. celle de sa première étape,
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    };

    // retourne la date/heure de fin du voyage, c.-à-d. celle de sa dernière étape,
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    };

    // etourne la durée totale du voyage, c.-à-d. celle séparant la date/heure de fin de celle de début.
    public Duration duration() {
        return Duration.between(legs.getFirst().depTime(), legs.getLast().arrTime());
    };

    public interface Leg {

        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {
            public IntermediateStop{
                Objects.requireNonNull(stop, "stop is null");
                if (depTime.isBefore(arrTime)){
                    throw new IllegalArgumentException();
                }
            }
        }

        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                                List<IntermediateStop> intermediateStops,
                                Vehicle vehicle, String route, String destination) {

        }

        public record Foot() {

        }

        Stop depStop();
        LocalDateTime depTime();
        Stop arrStop();
        LocalDateTime arrTime();

        List<IntermediateStop> intermediateStops();

        default Duration duration() {
            return Duration.between(arrTime(), depTime());
        }

    }
}
