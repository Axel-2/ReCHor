package ch.epfl.rechor.journey;

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
            if (legs[i].getClass() == legs[i - 1].getClass()){
                throw new IllegalArgumentException();
            }
            // Condition 3 : Sauf début, l'instant de départ est après le départ du précédent
            if (legs[i].depTime().isBefore(legs[i - 1].depTime())){
                throw new IllegalArgumentException();
            }
            // Condition 4 : Sauf début, le départ = l'arivée des étapes précédentes
            // La méthode compareTo renvoie 0 si elles sont égales, et c'est ce qu'on veut
            if (legs[i].depTime().compareTo(legs[i - 1].arrTime()) != 0){
                throw new IllegalArgumentException();
            }
        }

        // Si toutes les conditions sont validées
        // On rend la liste immuable, c'est tout bon
        legs = List.copyOf(legs);

    }

    // TODO : "Journey offre ces classes publiques. Mais Leg aussi ??
    // Oui car c'est différent, ici c'est voyage alors que dans Leg
    // c'est étape ! donc refaire les trucs ici aussi.
//    public Stop depStop(){}
//
//    public Stop arrStop(){};
//
//    public LocalDateTime depTime(){};
//
//    public LocalDateTime arrTime(){};
//
//    public Duration duration(){};


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
