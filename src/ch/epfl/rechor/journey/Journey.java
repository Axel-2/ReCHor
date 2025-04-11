package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Un voyage, composé de plusieurs étapes
 * @param legs (les étapes)
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record Journey(List<Leg> legs) {

    /**
     * Constructeur compact, vérifiant les conditions demandées
     */
    public Journey {


        // Condition 1 : La liste ne doit pas être nulle
        Objects.requireNonNull(legs, "legs is null");

        // La liste ne doit pas non plus être vide
        Preconditions.checkArgument(!legs.isEmpty());

        // Le reste des conditions se trouvent au sein de la boucle
        // Pour chaque étape consécutives :
        for (int i = 1; i < legs.size(); i++) {

            // Condition 2 : Les étapes doivent s'alterner
            Preconditions.checkArgument(legs.get(i).getClass() != legs.get(i - 1).getClass());

            // Condition 3 : Sauf début, l'instant de départ est après l'arrivée du précédent
            Preconditions.checkArgument(!legs.get(i).depTime().isBefore(legs.get(i - 1).arrTime()));

            // Condition 4 : Sauf début, le départ = l'arivée des étapes précédentes
            Preconditions.checkArgument(legs.get(i).depStop().equals(legs.get(i - 1).arrStop()));
        }

        // Si toutes les conditions sont validées
        // On rend la liste immuable, c'est tout bon
        legs = List.copyOf(legs);

    }

    /**
     * Méthode qui retourne l'arrêt de départ du voyage, c.-à-d. celui de sa première étape,
     * @return l'arrêt de départ du voyage
     */
    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    /**
     * Méthode qui retourne l'arrêt d'arrivée du voyage, c.-à-d. celui de sa dernière étape,
     * @return l'arrêt d'arrivée du voyage, c.-à-d. celui de sa dernière étape,
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    /**
     * Méthode qui retourne la date/heure de début du voyage, c.-à-d. celle de sa première étape
     * @return la date/heure de début du voyage
     */
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    /**
     * Méthode qui retourne la date/heure de fin du voyage, c.-à-d. celle de sa dernière étape,
     * @return la date/heure de fin du voyage
     */
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }


    /**
     * Calcule la durée d'un voyage c.-à-d. celle séparant la date/heure de fin de celle de début.
     * @return retourne la durée totale du voyage
     */
    public Duration duration() {
        return Duration.between(legs.getFirst().depTime(), legs.getLast().arrTime());
    }

    /**
     * Interface représentant les étapes
     * @author Yoann Salamin (390522)
     * @author Axel Verga (398787)
     */
    public sealed interface Leg {

        Stop depStop();
        LocalDateTime depTime();
        Stop arrStop();
        LocalDateTime arrTime();

        List<IntermediateStop> intermediateStops();

        /**
         * Calcule la durée d'une étape c.-à-d. celle séparant la date/heure de fin de celle de début.
         * @return retourne la durée de l'étape
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Arrêt intermédiaire
         * @param stop (Arrêt où a lieu cette étape intermédiaire)
         * @param arrTime (temps d'arrivée à l'arrêt)
         * @param depTime (temps de départ de l'arrêt)
         * @author Yoann Salamin (390522)
         * @author Axel Verga (398787)
         */
         record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            public IntermediateStop {

                Objects.requireNonNull(stop, "stop is null");

                // ne pas mettre l'inverse, car de cette façon ça valide aussi si
                // les dates sont les mêmes
                Preconditions.checkArgument(!depTime.isBefore(arrTime));
            }
        }

        /**
         * Etape en transport
         * @param depStop (Arrêt de départ, non null)
         * @param depTime (Temps de départ, non null)
         * @param arrStop (Arrêt d'arrivée, non null)
         * @param arrTime (Temps d'arrivée, non null)
         * @param intermediateStops (Arrêts intermédiaires auxquels le moyen de transport s'arrête)
         * @param vehicle (Véhicule du transport (bus, train ...., non null)
         * @param route (Route prise, non null)
         * @param destination (Destination, non null)
         */
        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                                List<IntermediateStop> intermediateStops, Vehicle vehicle, String route,
                                String destination) implements Leg {

            /**
             * Constructeur compact vérifiant la non nullité des paramètres néncessaires
             */
            public Transport {

                // chaque objet est non nul
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);

                // pas nécessaire
                // Objects.requireNonNull(intermediateStops);

                Preconditions.checkArgument(!arrTime.isBefore(depTime));

                // copie profonde
                intermediateStops = List.copyOf(intermediateStops);

            }

        }

        /**
         * Etape à pied
         * @param depStop (Arrêt de départ, non null)
         * @param depTime (Temps de départ, non null)
         * @param arrStop (Arrêt d'arrivée, non null)
         * @param arrTime (Temps d'arrivée, non null)
         */
        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {

            /**
             * Constructeur compact, vérifiant la non nullité des paramètres nécessaires
             */
            public Foot {

                // chaque objet est non nul
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);

                Preconditions.checkArgument(!arrTime.isBefore(depTime));


            }

            /**
             * Retourne une liste vide (immuable)
             * @return la liste
             */
            public List<IntermediateStop> intermediateStops() {
                // retourne une liste vide, car une étape à pied ne comporte jamais d'arrêts intermédiaires
                return List.of();
            }

            /**
             * Retourn true si l'étape représente un changement
             * @return booléen
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }

        }

    }
}
