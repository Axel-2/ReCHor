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
 * Publique et non instantiable.
 */
public final class JourneyExtractor {

    // Rendre la classe non instanciable
    private JourneyExtractor() {}

    /**
     * Retourne la totalité des voyages optimaux correspondant au profil et à la gare de départ donnés,
     * triés par heure de départ puis par heure d'arrivée.
     *
     * @param profile      le profil de l'horaire
     * @param depStationId l'ID de la station de départ choisi
     * @return la liste triée des voyages
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {

        List<Journey> journeys = new ArrayList<>();
        ParetoFront pf = profile.forStation(depStationId);

        // on itère sur tous les critères du profile de la gare donné et pour chaque critère
        // on génère le voyage correspondant qu'on ajoute dans la liste
        pf.forEach(criteria -> journeys.add(buildJourney(profile, criteria, depStationId)));

        // tri donné sur le site
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Construit un voyage à partir du premier critère et enchaîne les legs (transport et pied)
     * en traitant séparément le premier leg puis les changements restants.
     */
    private static Journey buildJourney(Profile profile, long firstCriteria, int depStationId) {

        List<Journey.Leg> legs = new ArrayList<>();

        // données du critère
        int changes = PackedCriteria.changes(firstCriteria);
        int finalArrMins = PackedCriteria.arrMins(firstCriteria);
        int payload = PackedCriteria.payload(firstCriteria);
        int initialDepTime = PackedCriteria.depMins(firstCriteria);
        int firstConnId = Bits32_24_8.unpack24(payload);

        // --- Traitement du premier leg ---

        // Ajout éventuel d’un leg à pied si le départ choisi diffère du départ effectif

        // gare initiale choisie par l'utilisateur
        Stop chosenStop = getStopInstance(profile, depStationId);
        int firstDepStopId = profile.connections().depStopId(firstConnId);

        // ----- Ajout du foot leg si besoin

        // vraie gare de départ (plus précise)
        Stop actualStop = getStopInstance(profile, firstDepStopId);
        if (!chosenStop.name().equals(actualStop.name())) {
            int walkMins = profile.timeTable().transfers()
                    .minutesBetween(depStationId, profile.timeTable().stationId(firstDepStopId));

            LocalDateTime footDepTime = profile.date().atStartOfDay().plusMinutes(initialDepTime);

            // On considère que l’heure d’arrivée du leg à pied est égale à l’heure de départ du transport
            LocalDateTime footArrTime = getLocalDateTime(profile, firstConnId);
            legs.add(new Journey.Leg.Foot(chosenStop, footDepTime, actualStop, footArrTime));
        }

        // ----- Ajout du premier segment en transport

        // nombre d'arrêts intermédiaires à laisser passer avant de descendre du véhicule.
        int nbOfIntermediateStopsOfCurrentLeg = Bits32_24_8.unpack8(payload);
        TransportLegResult firstLegResult = createTransportLeg(profile, firstConnId, nbOfIntermediateStopsOfCurrentLeg);
        Journey.Leg.Transport firstTransportLeg = firstLegResult.leg();
        legs.add(firstTransportLeg);

        // Préparation pour le traitement des legs supplémentaires
        int remainingChanges = changes - 1;
        int currentArrStopId = firstLegResult.endStopId();
        LocalDateTime currentArrivalTime = firstTransportLeg.arrTime();

        // --- Traitement des changements restants ---
        while (remainingChanges >= 0) {

            // Récupérer le critère suivant pour le nombre de changements restant
            ParetoFront nextFront = profile.forStation(profile.timeTable().stationId(currentArrStopId));
            long nextCriteria = nextFront.get(finalArrMins, remainingChanges);
            int nextPayload = PackedCriteria.payload(nextCriteria);
            int nextConnId = Bits32_24_8.unpack24(nextPayload);
            int nextNbOfIntermediateStopsOfCurrentLeg = Bits32_24_8.unpack8(nextPayload);

            // Ajout d'un leg à pied entre l'arrivée du leg précédent et le départ du suivant
            int nextDepStopId = profile.connections().depStopId(nextConnId);
            Stop currentStop = getStopInstance(profile, currentArrStopId);
            Stop nextDepStop = getStopInstance(profile, nextDepStopId);
            int walkMins = profile.timeTable().transfers()
                    .minutesBetween(profile.timeTable().stationId(currentArrStopId),
                            profile.timeTable().stationId(nextDepStopId));
            LocalDateTime nextTransportDepTime = getLocalDateTime(profile, nextConnId);
            LocalDateTime footArrTime = nextTransportDepTime;
            legs.add(new Journey.Leg.Foot(currentStop, currentArrivalTime, nextDepStop, footArrTime));

            // Création du leg de transport suivant
            TransportLegResult nextLegResult = createTransportLeg(profile, nextConnId, nextNbOfIntermediateStopsOfCurrentLeg);
            legs.add(nextLegResult.leg());

            // Mise à jour pour la prochaine itération
            currentArrStopId = nextLegResult.endStopId();
            currentArrivalTime = nextLegResult.leg().arrTime();
            remainingChanges--;
        }

        return new Journey(legs);
    }

    /**
     * Construit un leg de transport complet (incluant les arrêts intermédiaires) et renvoie
     * le leg ainsi que l’ID du stop d’arrivée final.
     */
    private static TransportLegResult createTransportLeg(Profile profile, int connectionId, int stopsBeforeLastStop) {
        int initialConnId = connectionId;
        TimeTable timeTable = profile.timeTable();
        LocalDate date = profile.date();
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();

        // Construction des arrêts intermédiaires s'il y en a
        while (stopsBeforeLastStop > 0) {
            int nextConnId = profile.connections().nextConnectionId(connectionId);
            int stopId = profile.connections().depStopId(nextConnId);
            Stop intermediateStop = getStopInstance(profile, stopId);

            LocalDateTime arrTime = minutesToLocalDateTime(date, profile.connections().arrMins(connectionId));
            LocalDateTime depTime = getLocalDateTime(profile, nextConnId);

            intermediateStops.add(new Journey.Leg.IntermediateStop(intermediateStop, arrTime, depTime));
            connectionId = nextConnId;
            stopsBeforeLastStop--;
        }

        // Création de l'arrêt d'arrivée et de l'heure d'arrivée finale
        int arrStopId = profile.connections().arrStopId(connectionId);
        Stop arrStop = getStopInstance(profile, arrStopId);
        LocalDateTime arrivalTime = minutesToLocalDateTime(date, profile.connections().arrMins(connectionId));

        // Création de l'arrêt de départ et de l'heure de départ initiale
        int depStopId = profile.connections().depStopId(initialConnId);
        Stop depStop = getStopInstance(profile, depStopId);
        LocalDateTime departureTime = getLocalDateTime(profile, initialConnId);

        int tripId = profile.connections().tripId(initialConnId);
        int routeId = profile.trips().routeId(tripId);

        Journey.Leg.Transport leg = new Journey.Leg.Transport(
                depStop,
                departureTime,
                arrStop,
                arrivalTime,
                intermediateStops,
                timeTable.routes().vehicle(routeId),
                timeTable.routes().name(routeId),
                profile.trips().destination(tripId)
        );

        return new TransportLegResult(leg, arrStopId);
    }

    /**
     * Crée une instance de Stop en distinguant l'ID de station de celui du quai.
     */
    private static Stop getStopInstance(Profile profile, int stopId) {
        TimeTable timeTable = profile.timeTable();
        int stationId = timeTable.isStationId(stopId) ? stopId : timeTable.stationId(stopId);
        String stopName = timeTable.stations().name(stationId);
        double longitude = timeTable.stations().longitude(stationId);
        double latitude = timeTable.stations().latitude(stationId);
        // Utiliser le stopId initial pour obtenir le nom de plateforme
        String platformName = timeTable.platformName(stopId);
        return new Stop(stopName, platformName, longitude, latitude);
    }

    /**
     * Convertit les minutes depuis minuit en LocalDateTime.
     */
    private static LocalDateTime minutesToLocalDateTime(LocalDate date, int minutes) {
        return date.atStartOfDay().plusMinutes(minutes);
    }

    /**
     * Calcule l'heure de départ d'une connexion donnée.
     */
    private static LocalDateTime getLocalDateTime(Profile profile, int connectionId) {
        int minutesSinceMidnight = profile.connections().depMins(connectionId);
        return profile.date().atStartOfDay().plusMinutes(minutesSinceMidnight);
    }

    /**
     * Container pour un leg de transport et l'ID du stop d'arrivée final.
     */
    private record TransportLegResult(Journey.Leg.Transport leg, int endStopId) {}
}