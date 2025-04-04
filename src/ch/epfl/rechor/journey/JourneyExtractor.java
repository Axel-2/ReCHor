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
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
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

        // Variable qui va incrémenter jusqu'à qu'on ait tous nos intermediates stops
        int currentConnectionId = firstConnectionIdOfThisJourney;

        // Boucle qui crée les arrêts intermédiaires
        currentConnectionId = createIntermediateStops(profile, intermediateStops, nbOfIntermediateStopsOfFirstTransportLeg, currentConnectionId);

        // Création du segment de transport
        Journey.Leg.Transport firstTransportLeg = createTransportLeg(
                profile, timeTable, firstConnectionIdOfThisJourney, currentConnectionId, firstStopUsed, intermediateStops);

        legs.add(firstTransportLeg);
        int remainingChangesOfJourney = changesOfFirstCriteria - 1;

        // ------------- FIN DE LA PARTIE 2) ---------------------

        // ------------- PARTIE 3) BOUCLE POUR FAIRE TOUS LES AUTRES LEGS --------------

        // Mise à jour des variables pour bien commencer la boucle
        int currentStopId = profile.connections().arrStopId(currentConnectionId);
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

            // Gestion des arrêts intermédiaires
            List<Journey.Leg.IntermediateStop> nextIntermediateStops = new ArrayList<>(nbOfIntermediateStopsOfCurrentLeg);
            currentConnectionId = createIntermediateStops(profile, nextIntermediateStops, nbOfIntermediateStopsOfCurrentLeg, firstConnectionOfCurrentLeg);

            // Création du segment de transport
            Stop depStopOfCurrentLeg = getStopInstance(profile, nextDepStopId);
            Journey.Leg.Transport transportLeg = createTransportLeg(
                    profile, timeTable, firstConnectionOfCurrentLeg, currentConnectionId, depStopOfCurrentLeg, nextIntermediateStops);

            legs.add(transportLeg);

            // Mise à jour pour la prochaine itération
            currentStopId = profile.connections().arrStopId(currentConnectionId);
            startingTimeOfCurrentLeg = transportLeg.arrTime();
            remainingChangesOfJourney--;
        }

        // Ajout du segment de marche final si nécessaire
        Stop finalStop = getStopInstance(profile, currentStopId);
        Stop destinationStop = getStopInstance(profile, profile.arrStationId());
        if (!finalStop.name().equals(destinationStop.name())) {
            int transferDuration = profile.timeTable().transfers().minutesBetween(profile.timeTable().stationId(currentStopId), profile.arrStationId());
            LocalDateTime finalFootLegDepTime = startingTimeOfCurrentLeg;
            LocalDateTime finalFootLegArrTime = finalFootLegDepTime.plusMinutes(transferDuration);
            legs.add(new Journey.Leg.Foot(finalStop, finalFootLegDepTime, destinationStop, finalFootLegArrTime));
        }

        return new Journey(legs);
    }


    /**
     * Création d'une étape de transport, pour éviter la duplication de code
     * @param profile profil
     * @param timeTable table d'horaire
     * @param initialConnId id de la connection de départ.
     * @param finalConnId id de la connection finale
     * @param depStop arrêt de départ
     * @param intermediateStops liste d'arrêts intermédiaire
     * @return la leg
     */
    private static Journey.Leg.Transport createTransportLeg(
            Profile profile, TimeTable timeTable, int initialConnId, int finalConnId, Stop depStop, List<Journey.Leg.IntermediateStop> intermediateStops) {

        // Date de départ
        LocalDateTime depTime = getLocalDateTime(profile, initialConnId);

        // Arrêt final et date d'arrivée
        int arrStopId = profile.connections().arrStopId(finalConnId);
        Stop arrStop = getStopInstance(profile, arrStopId);

        // Date d'arrivée
        LocalDateTime arrTime = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(finalConnId));

        // Trip et route ID, pour avoir le trip, la route et le véhicule
        int tripId = profile.connections().tripId(initialConnId);
        int routeId = profile.trips().routeId(tripId);

        return new Journey.Leg.Transport(
                depStop,
                depTime,
                arrStop,
                arrTime,
                intermediateStops,
                timeTable.routes().vehicle(routeId),
                timeTable.routes().name(routeId),
                profile.trips().destination(tripId)
        );
    }

    /**
     * Méthode qui remplis une liste d'arrêts intermédiaire et qui actualise l'id de la connexion en la retournant
     * Modularisation pour éviter la duplication
     * @param profile profil
     * @param intermediateStops liste d'arrêts intermédiaires à remplir
     * @param intermediateStopsRemaining nombre d'arrêt qu'il faut créer
     * @param currentConnectionId id de la connexion actuelle
     * @return l'id de la connexion incrémentée, après avoir ajouter les stops à la liste
     */
    private static int createIntermediateStops(Profile profile, List<Journey.Leg.IntermediateStop> intermediateStops, int intermediateStopsRemaining, int currentConnectionId) {
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
        return currentConnectionId;
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