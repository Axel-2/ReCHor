package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Classe qui représente un extracteur de voyage.
 * Publique et non instantiable.
 */
public final class JourneyExtractor {

    // Rendre la classe non instantiable
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

        // La liste des journey que l'on va retourner à la fin
        List<Journey> journeys = new ArrayList<>();

        // Frontière de Pareto de la station de départ
        ParetoFront pf = profile.forStation(depStationId);

        // Pour chacun des critères de la frontière, on crée un voyage qu'on ajoute dans la liste
        pf.forEach(criteria -> journeys.add(makeJourney(profile, criteria, depStationId)));

        // On trie nos voyages
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Construit un voyage à partir du premier critère et enchaîne les legs (transport et pied)
     * en traitant séparément le premier leg puis les changements restants.
     */
    private static Journey makeJourney(Profile profile, long firstCriteria, int depStationId) {

        // Liste des étapes qui vont permettre de créer un voyage
        List<Journey.Leg> legs = new ArrayList<>();

        // Table des horaires
        TimeTable timeTable = profile.timeTable();

        // On extrait les données du critère et de son payload
        int changesOfFirstCriteria = PackedCriteria.changes(firstCriteria);
        int finalArrMinsOfFirstCriteria = PackedCriteria.arrMins(firstCriteria);
        int depMinsOfFirstCriteria = PackedCriteria.depMins(firstCriteria);
        int payloadOfFirstCriteria = PackedCriteria.payload(firstCriteria);
        int firstConnectionIdOfThisJourney = Bits32_24_8.unpack24(payloadOfFirstCriteria);

        // ------------------ PARTIE 1) AJOUT ÉVENTUEL D'UNE PREMIERE ÉTAPE A PIED ---------------------------

        // Récupération de l'ID du stop de départ la première connection (EPFL par exemple)
        int firstStopUsedId = profile.connections().depStopId(firstConnectionIdOfThisJourney);

        // On compare le premier stop fourni avec le stop de la première connection, donc le premier stop réellement
        // utilisé, et s'ils ne sont pas les mêmes, c'est qu'il y a un trajet à pied à faire, comme première étape
        Stop firstStopProvided = getStopInstance(profile, depStationId);
        Stop firstStopUsed = getStopInstance(profile, firstStopUsedId);
        if (!firstStopProvided.name().equals(firstStopUsed.name())) {


            int transferDuration = profile.timeTable().transfers().minutesBetween(depStationId,
                    profile.timeTable().stationId(firstStopUsedId));

            // PREMIER LEG = FOOTLEG
            LocalDateTime firstFootLegDepTime = profile.date().atStartOfDay().plusMinutes(depMinsOfFirstCriteria);
            LocalDateTime firstFootLegArrTime = firstFootLegDepTime.plusMinutes(transferDuration);

            legs.add(new Journey.Leg.Foot(firstStopProvided, firstFootLegDepTime, firstStopUsed, firstFootLegArrTime));
        }
        // ------------------ FIN PARTIE 1 ----------------------------------

        // ------------------ PARTIE 2) PREMIERE ÉTAPE EN TRANSPORT ---------------------------

        // Nombre d'arrêts intermédiaires à laisser passer avant de descendre du véhicule.
        int nbOfIntermediateStopsOfFirstTransportLeg = Bits32_24_8.unpack8(payloadOfFirstCriteria);

        // Liste des arrêts intermédiaires de cette première étape en transport
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();

        // Variable qui compte le nombre d'arrêts qu'il reste à ajouter
        int intermediateStopsRemaining = nbOfIntermediateStopsOfFirstTransportLeg;

        // Variable qui va incrémenter jusqu'à qu'on ait tous nos intermediates stops
        int currentConnectionId = firstConnectionIdOfThisJourney;


        // Boucle qui crée les arrêts intermédiaires
        while (intermediateStopsRemaining > 0) {
            // On prend le prochain stop
            int nextConnectionId = profile.connections().nextConnectionId(currentConnectionId);
            int stopId = profile.connections().depStopId(nextConnectionId);
            Stop intermediateStop = getStopInstance(profile, stopId);

            // Ainsi que la date de départ et d'arrivée à celui-ci
            LocalDateTime arrTime = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(currentConnectionId));
            LocalDateTime depTime = getLocalDateTime(profile, nextConnectionId);

            // Et on l'ajoute à la liste
            intermediateStops.add(new Journey.Leg.IntermediateStop(intermediateStop, arrTime, depTime));

            // Actualisation de la connexion qu'on étudie
            currentConnectionId = nextConnectionId;

            // On décrémente pour la prochaine boucle
            intermediateStopsRemaining--;
        }

        // On en a fini avec les arrêts intermédiaires, il faut maintenant créer tout ce dont on a besoin
        // pour finaliser notre première étape de transport.

        // On a déjà : IntermediateStops et Arrêt de départ.
        // Il nous manque : Date de départ, Date d'arrivée, Arrêt final, Véhicule, Route, Trip

        // Date de départ
        LocalDateTime firstDepartureTime = getLocalDateTime(profile, firstConnectionIdOfThisJourney);

        // Arrêt final et date d'arrivée
        int firstTransportLegArrStopId = profile.connections().arrStopId(currentConnectionId);
        Stop firstTransportLegArrStop = getStopInstance(profile, firstTransportLegArrStopId);

        // Date d'arrivée
        LocalDateTime firstArrivalTime = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(currentConnectionId));

        // Trip et route ID, Pour avoir le trip la route et le véhicle
        int firstTripId = profile.connections().tripId(firstConnectionIdOfThisJourney);
        int firstRouteId = profile.trips().routeId(firstTripId);

        // Création
        Journey.Leg.Transport firstTransportLeg = new Journey.Leg.Transport(
                firstStopUsed,
                firstDepartureTime,
                firstTransportLegArrStop,
                firstArrivalTime,
                intermediateStops,
                timeTable.routes().vehicle(firstRouteId),
                timeTable.routes().name(firstRouteId),
                profile.trips().destination(firstTripId)
        );


        legs.add(firstTransportLeg);
        int remainingChangesOfJourney = changesOfFirstCriteria - 1;

        // ------------- FIN DE LA PARTIE 2) ---------------------

        // ------------- PARTIE 3) BOUCLE POUR FAIRE TOUS LES AUTRES LEGS --------------

        // Mise à jour des variables pour bien commencer la boucle
        int currentStopId = firstTransportLegArrStopId;
        LocalDateTime startingTimeOfCurrentLeg = firstTransportLeg.arrTime();

        // Tant qu'il reste des changements dans le voyage
        while (remainingChangesOfJourney >= 0) {

            // On choppe le critère de là où on en est
            ParetoFront currentParetoFront = profile.forStation(profile.timeTable().stationId(currentStopId));

            // Extraction des données
            long currentCriteria = currentParetoFront.get(finalArrMinsOfFirstCriteria, remainingChangesOfJourney);
            int currentPayload = PackedCriteria.payload(currentCriteria);
            int firstConnectionOfCurrentLeg = Bits32_24_8.unpack24(currentPayload);
            int nbOfIntermediateStopsOfCurrentLeg = Bits32_24_8.unpack8(currentPayload);

            // Connection suivante
            int nextDepStopId = profile.connections().depStopId(firstConnectionOfCurrentLeg);

            // L'étape d'avant était en transport, on doit donc maintenant en créer une à pied

            // Stops
            Stop currentStop = getStopInstance(profile, currentStopId);
            Stop nextDepStop = getStopInstance(profile, nextDepStopId);

            // Durée du changement
            int transferDuration = profile.timeTable().transfers()
                    .minutesBetween(profile.timeTable().stationId(currentStopId),
                            profile.timeTable().stationId(nextDepStopId));

            // Heures
            LocalDateTime footDepTime = startingTimeOfCurrentLeg;
            LocalDateTime footArrTime = startingTimeOfCurrentLeg.plusMinutes(transferDuration);

            legs.add(new Journey.Leg.Foot(currentStop, footDepTime, nextDepStop, footArrTime));
            // -------------- Fin de l'étape à Pied ---------------//

            // ------------- Début de l'étape en transport ------- //

            currentConnectionId = firstConnectionOfCurrentLeg;

            // Gestion des arrêts intermédiaires
            intermediateStopsRemaining = nbOfIntermediateStopsOfCurrentLeg;
            List<Journey.Leg.IntermediateStop> nextIntermediateStops = new ArrayList<>(nbOfIntermediateStopsOfCurrentLeg);

            while (intermediateStopsRemaining > 0) {
                // On prend le prochain stop
                int nextConnectionId = profile.connections().nextConnectionId(currentConnectionId);
                int stopId = profile.connections().depStopId(nextConnectionId);
                Stop intermediateStop = getStopInstance(profile, stopId);

                // Ainsi que la date de départ et d'arrivée à celui-ci
                LocalDateTime arrTime = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(currentConnectionId));
                LocalDateTime depTime = getLocalDateTime(profile, nextConnectionId);

                // Et on l'ajoute à la liste
                intermediateStops.add(new Journey.Leg.IntermediateStop(intermediateStop, arrTime, depTime));

                // Actualisation de la connexion qu'on étudie
                currentConnectionId = nextConnectionId;

                // On décrémente pour la prochaine boucle
                intermediateStopsRemaining--;
            }

            // Arrêts intermédiaires finis, on s'occupe des autres paramètres pour créer la leg
            // current connection a été incrémenté

            // Création de l'arrêt de départ et de l'heure de départ initiale
            int depStopIdOfCurrentLeg = profile.connections().depStopId(firstConnectionOfCurrentLeg);
            Stop depStopOfCurrentLeg = getStopInstance(profile, depStopIdOfCurrentLeg);
            LocalDateTime depTimeOfCurrentLeg = getLocalDateTime(profile, firstConnectionOfCurrentLeg);

            // Création de l'arrêt d'arrivée et de l'heure d'arrivée finale
            int endStopIdOfCurrentLeg = profile.connections().arrStopId(currentConnectionId);
            Stop endStopOfCurrentLeg = getStopInstance(profile, endStopIdOfCurrentLeg);
            LocalDateTime endTimeOfCurrentLeg = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(currentConnectionId));

            // Obtention des trip et route id pour connaître le véhicule, le trip et la route
            int tripId = profile.connections().tripId(firstConnectionOfCurrentLeg);
            int routeId = profile.trips().routeId(tripId);

            // Création de la leg
            Journey.Leg.Transport transportLeg = new Journey.Leg.Transport(
                    depStopOfCurrentLeg,
                    depTimeOfCurrentLeg,
                    endStopOfCurrentLeg,
                    endTimeOfCurrentLeg,
                    nextIntermediateStops,
                    timeTable.routes().vehicle(routeId),
                    timeTable.routes().name(routeId),
                    profile.trips().destination(tripId)
            );


            legs.add(transportLeg);

            // Mise à jour pour la prochaine itération
            currentStopId = endStopIdOfCurrentLeg;
            startingTimeOfCurrentLeg = transportLeg.arrTime();
            remainingChangesOfJourney--;
        }

        return new Journey(legs);
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
     * Calcule l'heure de départ d'une connexion donnée.
     */
    private static LocalDateTime getLocalDateTime(Profile profile, int connectionId) {
        int minutesSinceMidnight = profile.connections().depMins(connectionId);
        return profile.date().atStartOfDay().plusMinutes(minutesSinceMidnight);
    }
}