package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe qui représente un extracteur de voyage.
 * Publique et non instantiable
 *@author Yoann Salamin (390522)
 *@author Axel Verga (398787)
 */
public class JourneyExtractor {

    // Pour la rendre non instantiable
    private JourneyExtractor() {}

    /**
     *  Méthode qui  retourne la totalité des voyages optimaux correspondant au profil et à la gare de départ donnés,
     *  triés d'abord par heure de départ (croissante) puis par heure d'arrivée (croissante).
     * @param profile Profil
     * @param depStationId id de la station de départ
     * @return la totalité des voyages optimaux correspondants aux paramètres
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {

        // Initialisation de la liste des voyages qu'on va retourner
        List<Journey> journeys = new ArrayList<>();

        // On récupère le batisseur de la frontière de pareto de la gare d'index depStationId
        ParetoFront pf = profile.forStation(depStationId);

        // Pour chaque critères
        pf.forEach((long criteria) -> {

            // Création du voyage lié au critère
            Journey j = makeJourney(profile, criteria, depStationId);
            journeys.add(j);
        });

        // Tri de journey, par le code donné dans l'énoncé
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    private static Journey makeJourney(Profile profile, long firstCriteria, int depStationId){

        // Création de la liste d'étapes (legs) qui composeront notre voyage qui sera retourné
        List<Journey.Leg> currentLegsList = new ArrayList<>();

        // Extractions des toutes les données empaquetées du premier critère
        int numberOfChanges = PackedCriteria.changes(firstCriteria);
        int finalArrMins = PackedCriteria.arrMins(firstCriteria);
        int criteriaPayload = PackedCriteria.payload(firstCriteria);

        // Variables pour stocker l'état actuel
        TimeTable timeTable = profile.timeTable();
        LocalDate date = profile.date();
        Connections conn = profile.connections();
        LocalDateTime currentTime = PackedCriteria.hasDepMins(firstCriteria) ?
                minutesToLocalDateTime(date, PackedCriteria.depMins(firstCriteria)) : null;
        int currentStopId;

        // Première connexion du critère
        int currentConnectionId = Bits32_24_8.unpack24(criteriaPayload);
        // Nb d'arrêts intermédiaires
        int stopsBeforeLastStop = Bits32_24_8.unpack8(criteriaPayload);

        // Calculer le premier leg de transport
        TransportLegResult firstResult = getTransportLeg(currentConnectionId, stopsBeforeLastStop, conn, profile);
        Journey.Leg.Transport firstLeg = firstResult.leg();

        // Vérifier si un leg de marche initial est nécessaire
        int firstLegDepStopId = conn.depStopId(currentConnectionId);
        Stop firstLegDep = getStopInstance(profile, firstLegDepStopId);
        Stop chosenDeparture = getStopInstance(profile, depStationId);

        if (!chosenDeparture.name().equals(firstLegDep.name())) {
            int walkMins = timeTable.transfers().minutesBetween(
                    depStationId,
                    timeTable.stationId(firstLegDepStopId)
            );

            LocalDateTime footDepTime = (currentTime != null) ?
                    currentTime : firstLeg.depTime().minusMinutes(walkMins);

            LocalDateTime footArrTime = firstLeg.depTime();

            currentLegsList.add(new Journey.Leg.Foot(chosenDeparture, footDepTime, firstLegDep, footArrTime));
        }

        // Ajouter le premier leg de transport
        currentLegsList.add(firstLeg);
        currentStopId = firstResult.endStopId();
        currentTime = firstLeg.arrTime();

        // Changements restants (ou le nombre d'étapes (legs) qu'il nous reste à ajouter)
        int numberOfChangesRemaining = numberOfChanges - 1;

        // BOUCLE : Créons une leg pour chaque changement restant
        while (numberOfChangesRemaining >= 0) {
            // Obtenir la station actuelle et sa frontière de Pareto
            int currentStationId = timeTable.stationId(currentStopId);
            ParetoFront currentFront = profile.forStation(currentStationId);

            // Trouver le prochain critère
            long nextCriteria = currentFront.get(finalArrMins, numberOfChangesRemaining);
            criteriaPayload = PackedCriteria.payload(nextCriteria);

            int nextConnId = Bits32_24_8.unpack24(criteriaPayload);
            int nextSkips = Bits32_24_8.unpack8(criteriaPayload);
            int nextDepStopId = conn.depStopId(nextConnId);

            // Ajouter un leg à pied entre les transports
            Stop curStop = getStopInstance(profile, currentStopId);
            Stop nextStop = getStopInstance(profile, nextDepStopId);

            int walkMins = timeTable.transfers().minutesBetween(
                    timeTable.stationId(currentStopId),
                    timeTable.stationId(nextDepStopId)
            );

            LocalDateTime footArrTime = currentTime.plusMinutes(walkMins);
            currentLegsList.add(new Journey.Leg.Foot(curStop, currentTime, nextStop, footArrTime));

            // Calculer et ajouter le prochain leg de transport
            TransportLegResult nextResult = getTransportLeg(nextConnId, nextSkips, conn, profile);
            currentLegsList.add(nextResult.leg());

            // Mettre à jour les variables pour le prochain tour
            currentStopId = nextResult.endStopId();
            currentTime = nextResult.leg().arrTime();
            numberOfChangesRemaining--;
        }

        return new Journey(currentLegsList);
    }

    /**
     * Structure pour contenir un leg de transport et son ID d'arrêt final
     */
    private record TransportLegResult(Journey.Leg.Transport leg, int endStopId) {}

    /**
     * Crée un leg de transport avec tous les arrêts intermédiaires
     */
    private static TransportLegResult getTransportLeg(int connectionId, int stopsBeforeLastStop,
                                                      Connections conn, Profile profile) {
        // Récupération des données nécessaires
        TimeTable timeTable = profile.timeTable();
        LocalDate date = profile.date();

        // Obtenir les informations de départ
        int depStopId = conn.depStopId(connectionId);
        Stop depStop = getStopInstance(profile, depStopId);
        LocalDateTime depTime = minutesToLocalDateTime(date, conn.depMins(connectionId));

        // Liste pour les arrêts intermédiaires
        List<Journey.Leg.IntermediateStop> intermediateStopList = new ArrayList<>();

        // ID de connexion actuel pour la boucle
        int currentId = connectionId;
        int remainingStops = stopsBeforeLastStop;

        // Ajouter tous les arrêts intermédiaires
        while (remainingStops > 0) {
            int nextId = conn.nextConnectionId(currentId);

            // Créer un arrêt intermédiaire
            int stopId = conn.arrStopId(currentId);
            Stop intermediateStop = getStopInstance(profile, stopId);

            LocalDateTime arrTime = minutesToLocalDateTime(date, conn.arrMins(currentId));
            LocalDateTime nextDepTime = minutesToLocalDateTime(date, conn.depMins(nextId));

            intermediateStopList.add(new Journey.Leg.IntermediateStop(
                    intermediateStop,
                    arrTime,
                    nextDepTime
            ));

            currentId = nextId;
            remainingStops--;
        }

        // Obtenir les informations d'arrivée finale
        int finalArrStopId = conn.arrStopId(currentId);
        Stop arrStop = getStopInstance(profile, finalArrStopId);
        LocalDateTime arrTime = minutesToLocalDateTime(date, conn.arrMins(currentId));

        // Obtenir les informations sur le trip et la route
        int tripId = conn.tripId(connectionId);
        int routeId = profile.trips().routeId(tripId);

        // Créer le leg de transport
        Journey.Leg.Transport transportLeg = new Journey.Leg.Transport(
                depStop,
                depTime,
                arrStop,
                arrTime,
                intermediateStopList,
                profile.timeTable().routes().vehicle(routeId),
                profile.timeTable().routes().name(routeId),
                profile.trips().destination(tripId)
        );

        return new TransportLegResult(transportLeg, finalArrStopId);
    }

    // Fonction qui crée une instance de Stop à partir de l'id de l'arret
    private static Stop getStopInstance(Profile profile, int stopId) {
        TimeTable timeTable = profile.timeTable();

        // Convertir les quais en stations si nécessaire
        int stationId = timeTable.stationId(stopId);

        // Récupération des attributs de la station
        String stationName = timeTable.stations().name(stationId);
        double longitude = timeTable.stations().longitude(stationId);
        double latitude = timeTable.stations().latitude(stationId);

        // Récupération du nom du quai pour cet arrêt
        String platformName = timeTable.platformName(stopId);

        return new Stop(stationName, platformName, longitude, latitude);
    }

    private static LocalDateTime minutesToLocalDateTime(LocalDate date, int minutes) {
        return date.atStartOfDay().plusMinutes(minutes);
    }
}