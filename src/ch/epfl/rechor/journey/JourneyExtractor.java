package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.TimeTable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Classe qui représente un extracteur de voyage.
 * Publique et non instantiable
 *@author Yoann Salamin (390522)
 *@author Axel Verga (398787)
 */
public class JourneyExtractor {

    // Pour rendre la classe non instantiable
    private JourneyExtractor() {}

    /**
     *  Méthode qui retourne la totalité des voyages optimaux correspondant au profil et à la gare de départ donnés,
     *  triés d'abord par heure de départ (croissante) puis par heure d'arrivée (croissante).
     * @param profile Profil
     * @param depStationId id de la station de départ
     * @return la totalité des voyages optimaux correspondants aux paramètres
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        // liste finale qui contiendra tous les voyages
        List<Journey> journeys = new ArrayList<>();

        // on va itérer sur tous les critères de la frontière et ajouter un voyage complet pour chacun
        ParetoFront paretoFront = profile.forStation(depStationId);
        paretoFront.forEach(criteria -> journeys.add(new JourneyBuilder(profile, depStationId, criteria).build()));

        // méthode fournie sur le site pour trier les voyages
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));

        return journeys;
    }
    
    // Builder pour simplifier la création des voyages
    private static class JourneyBuilder {

        private final Profile profile;
        private final int chosenDepId;
        private final long initialCriteria;

        JourneyBuilder(Profile profile, int chosenDepId, long initialCriteria) {
            this.profile = profile;
            this.chosenDepId = chosenDepId;
            this.initialCriteria = initialCriteria;
        }

        Journey build() {

            List<Journey.Leg> legs = new ArrayList<>();

            // Extraction des informations empaquetées dans le critère initial
            int numChanges = PackedCriteria.changes(initialCriteria);
            int finalArrMins = PackedCriteria.arrMins(initialCriteria);
            int criteriaPayload = PackedCriteria.payload(initialCriteria);

            int firstConnId = Bits32_24_8.unpack24(criteriaPayload);
            int firstStopsBeforeLastStop = Bits32_24_8.unpack8(criteriaPayload);

            // Création du premier segment de transport
            TransportLegResult firstLegResult = LegBuilder.createTransportLeg(profile, firstConnId, firstStopsBeforeLastStop);
            Journey.Leg.Transport firstLeg = firstLegResult.leg();

            // Ajout éventuel d'un segment de marche depuis la station choisie
            // jusqu'à la station réelle de départ du premier transport
            LegBuilder.addInitialFootLegIfNeeded(profile, legs, chosenDepId, firstConnId, firstLeg.depTime());
            legs.add(firstLeg);

            // Préparation pour ajouter les segments suivants
            int remainingChanges = numChanges - 1;
            int currentArrivalStopId = firstLegResult.endStopId();
            LocalDateTime currentArrivalTime = firstLeg.arrTime();

            // Pour chaque changement restant, construire le segment correspondant
            while (remainingChanges >= 0) {
                // on récupère la frontière pour la station d'arrivée courante
                ParetoFront nextFront = profile.forStation(profile.timeTable().stationId(currentArrivalStopId));

                // on prend le prochain critère optimal
                long nextCriteria = nextFront.get(finalArrMins, remainingChanges);
                int nextPayload = PackedCriteria.payload(nextCriteria);
                int nextConnId = Bits32_24_8.unpack24(nextPayload);
                int nextStopsBeforeLastStop = Bits32_24_8.unpack8(nextPayload);

                // Ajout d'un segment de marche entre le segment précédent et la prochaine connexion
                LegBuilder.addFootLeg(profile, legs, currentArrivalStopId, nextConnId, currentArrivalTime);

                // Création du segment de transport correspondant à la prochaine connexion
                TransportLegResult nextLegResult = LegBuilder.createTransportLeg(profile, nextConnId, nextStopsBeforeLastStop);
                legs.add(nextLegResult.leg());

                // Mise à jour de l'état pour la prochaine itération
                currentArrivalStopId = nextLegResult.endStopId();
                currentArrivalTime = nextLegResult.leg().arrTime();
                remainingChanges--;
            }
            return new Journey(legs);
        }
    }

    // Classe pour faciliter la création des legs
    private static class LegBuilder {

        static TransportLegResult createTransportLeg(Profile profile, int connectionId, int stopsBeforeLastStop) {

            int initialConnId = connectionId;
            TimeTable tt = profile.timeTable();
            LocalDate date = profile.date();
            List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();

            // Ajout des arrêts intermédiaires, si nécessaire
            while (stopsBeforeLastStop > 0) {
                int nextConnId = profile.connections().nextConnectionId(connectionId);
                int stopId = profile.connections().depStopId(nextConnId);

                Stop intermediateStop = createStop(profile, stopId);
                LocalDateTime arrTime = TimeUtils.minutesToLocalDateTime(date, profile.connections().arrMins(connectionId));

                // Récupère l'heure de départ du prochain segment
                LocalDateTime depTime = TimeUtils.getLocalDateTime(profile, nextConnId);
                intermediateStops.add(new Journey.Leg.IntermediateStop(intermediateStop, arrTime, depTime));

                connectionId = nextConnId;
                stopsBeforeLastStop--;
            }

            // Récupération des informations pour le dernier arrêt du segment
            int arrStopId = profile.connections().arrStopId(connectionId);
            Stop arrStop = createStop(profile, arrStopId);

            LocalDateTime arrivalTime = TimeUtils.minutesToLocalDateTime(date, profile.connections().arrMins(connectionId));

            // Récupération des informations pour le départ du segment (première connexion)
            int depStopId = profile.connections().depStopId(initialConnId);
            Stop depStop = createStop(profile, depStopId);

            LocalDateTime departureTime = TimeUtils.getLocalDateTime(profile, initialConnId);

            // Récupération des informations liées au trajet et à l'itinéraire
            int tripId = profile.connections().tripId(initialConnId);
            int routeId = profile.trips().routeId(tripId);

            // Création du segment de transport avec toutes ses informations
            Journey.Leg.Transport transportLeg = new Journey.Leg.Transport(
                    depStop, departureTime, arrStop, arrivalTime, intermediateStops,
                    tt.routes().vehicle(routeId), tt.routes().name(routeId), profile.trips().destination(tripId)
            );

            return new TransportLegResult(transportLeg, arrStopId);
        }

        static void addInitialFootLegIfNeeded(Profile profile, List<Journey.Leg> legs, int chosenDepId, int firstConnId, LocalDateTime transportDepTime) {
            // Création des stops correspondants à la station choisie et à la station réelle de départ
            Stop chosenStop = createStop(profile, chosenDepId);

            int firstDepStopId = profile.connections().depStopId(firstConnId);
            Stop actualDepStop = createStop(profile, firstDepStopId);

            // Si les noms des stations diffèrent, cela indique qu'une marche est nécessaire
            if (!chosenStop.name().equals(actualDepStop.name())) {
                int walkMinutes = profile.timeTable().transfers()
                        .minutesBetween(chosenDepId, profile.timeTable().stationId(firstDepStopId));
                legs.add(new Journey.Leg.Foot(chosenStop, transportDepTime, actualDepStop, transportDepTime.plusMinutes(walkMinutes)));
            }
        }

        static void addFootLeg(Profile profile, List<Journey.Leg> legs, int currentArrStopId, int nextConnId, LocalDateTime currentArrivalTime) {

            int nextDepStopId = profile.connections().depStopId(nextConnId);

            Stop currentStop = createStop(profile, currentArrStopId);
            Stop nextDepStop = createStop(profile, nextDepStopId);
            int walkMinutes = profile.timeTable().transfers().minutesBetween(

                    profile.timeTable().stationId(currentArrStopId),
                    profile.timeTable().stationId(nextDepStopId));

            legs.add(new Journey.Leg.Foot(currentStop, currentArrivalTime, nextDepStop, currentArrivalTime.plusMinutes(walkMinutes)));
        }

        private static Stop createStop(Profile profile, int stopId) {
            TimeTable tt = profile.timeTable();

            int stationId = tt.isStationId(stopId) ? stopId : tt.stationId(stopId);

            return new Stop(
                    tt.stations().name(stationId),
                    tt.platformName(stopId),
                    tt.stations().longitude(stationId),
                    tt.stations().latitude(stationId));
        }
    }

    // Classe avec fonctions utiles pour le temps
    private static class TimeUtils {

        // fonction qui retourne une LocalDateTime à partir du nombre de minutes
        static LocalDateTime minutesToLocalDateTime(LocalDate date, int minutes) {
            return date.atStartOfDay().plusMinutes(minutes);
        }


        static LocalDateTime getLocalDateTime(Profile profile, int connectionId) {
            int minutesSinceMidnight = profile.connections().depMins(connectionId);
            return profile.date().atTime(minutesSinceMidnight / 60, minutesSinceMidnight % 60);
        }
    }

    private record TransportLegResult(Journey.Leg.Transport leg, int endStopId) {}
}
