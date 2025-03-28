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
     * Crée la liste des arrêts intermédiaires pour un segment de transport.
     *
     * @param profile           le profil contenant les données
     * @param startConnId       l'ID de la connexion de départ
     * @param nbIntermediateStops  le nombre d'arrêts intermédiaires
     * @return                  un tableau avec la liste des arrêts et l'ID de la dernière connexion
     */
    private static Object[] createIntermediateStops(Profile profile, int startConnId, int nbIntermediateStops) {
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>(nbIntermediateStops);
        int currentConnId = startConnId;
        int remainingStops = nbIntermediateStops;

        while (remainingStops > 0) {
            // On prend le prochain stop
            int nextConnId = profile.connections().nextConnectionId(currentConnId);
            int stopId = profile.connections().depStopId(nextConnId);
            Stop intermediateStop = getStopInstance(profile, stopId);

            // Ainsi que la date de départ et d'arrivée à celui-ci
            LocalDateTime arrTime = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(currentConnId));
            LocalDateTime depTime = getLocalDateTime(profile, nextConnId);

            // Et on l'ajoute à la liste
            intermediateStops.add(new Journey.Leg.IntermediateStop(intermediateStop, arrTime, depTime));

            // Actualisation de la connexion qu'on étudie
            currentConnId = nextConnId;
            remainingStops--;
        }

        return new Object[] { intermediateStops, currentConnId };
    }

    /**
     * Crée un segment de transport avec tous ses paramètres.
     *
     * @param profile      le profil contenant les données
     * @param connId       l'ID de la connexion initiale
     * @param lastConnId   l'ID de la dernière connexion (pour l'arrivée)
     * @param intermediateStops liste des arrêts intermédiaires
     * @return             un segment de transport complet
     */
    private static Journey.Leg.Transport createTransportLeg(
            Profile profile, int connId, int lastConnId, List<Journey.Leg.IntermediateStop> intermediateStops) {

        TimeTable timeTable = profile.timeTable();

        // Arrêt de départ et heure de départ
        int depStopId = profile.connections().depStopId(connId);
        Stop depStop = getStopInstance(profile, depStopId);
        LocalDateTime depTime = getLocalDateTime(profile, connId);

        // Arrêt d'arrivée et heure d'arrivée
        int arrStopId = profile.connections().arrStopId(lastConnId);
        Stop arrStop = getStopInstance(profile, arrStopId);
        LocalDateTime arrTime = profile.date().atStartOfDay().plusMinutes(profile.connections().arrMins(lastConnId));

        // Informations sur le trajet
        int tripId = profile.connections().tripId(connId);
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

        // Création des arrêts intermédiaires
        Object[] result = createIntermediateStops(profile, firstConnectionIdOfThisJourney, nbOfIntermediateStopsOfFirstTransportLeg);
        List<Journey.Leg.IntermediateStop> intermediateStops = (List<Journey.Leg.IntermediateStop>) result[0];
        int currentConnectionId = (int) result[1];

        // Création du segment de transport
        Journey.Leg.Transport firstTransportLeg = createTransportLeg(
                profile, firstConnectionIdOfThisJourney, currentConnectionId, intermediateStops);
        legs.add(firstTransportLeg);

        // Récupérer l'ID du stop d'arrivée pour la prochaine étape
        int firstTransportLegArrStopId = profile.connections().arrStopId(currentConnectionId);

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

            // Gestion des arrêts intermédiaires
            result = createIntermediateStops(profile, firstConnectionOfCurrentLeg, nbOfIntermediateStopsOfCurrentLeg);
            List<Journey.Leg.IntermediateStop> nextIntermediateStops = (List<Journey.Leg.IntermediateStop>) result[0];
            currentConnectionId = (int) result[1];

            // Création du segment de transport
            Journey.Leg.Transport transportLeg = createTransportLeg(
                    profile, firstConnectionOfCurrentLeg, currentConnectionId, nextIntermediateStops);
            legs.add(transportLeg);

            // Mise à jour pour la prochaine itération
            currentStopId = profile.connections().arrStopId(currentConnectionId);
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