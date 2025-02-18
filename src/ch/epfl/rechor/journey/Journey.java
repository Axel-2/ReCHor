package ch.epfl.rechor.journey;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Journey(List<Leg> legs) {

    // Constructeur compact
    public Journey {
        Objects.requireNonNull(legs, "legs is null");

    }



    public interface Leg {

        public record IntermediateStop() {

        }

        public record Transport() {

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
