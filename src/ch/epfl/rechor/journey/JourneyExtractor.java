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

            int numberOfChanges = PackedCriteria.changes(criteria);
            int finalArrMins = PackedCriteria.arrMins(criteria);

            List<Journey.Leg> currentLegsList = new ArrayList<>();

            // on boucle tant qu'il reste des changements dans le voyage complet
            while (numberOfChanges > 0) {

                int criterialPayload = PackedCriteria.payload(criteria);

                // première connection
                int currentConnectionId = Bits32_24_8.unpack24(criterialPayload);
                // nombre de d'arrêts intermédiaires
                int numberOfStopsBeforeChange = Bits32_24_8.unpack8(criterialPayload);

                // par défaut la prochaine connection est égale à la première
                int finalLegConnectionId = currentConnectionId;

                // -------- Création de l'arrêt de départ -------------------

                // id de la gare de départ
                int depStopId = profile.connections().depStopId(currentConnectionId);

                // création de l'instance de l'arrêt de départ
                Stop depStop = getStopInstance(profile, depStopId);
                // Création de l'arrêt d'arrivée

                // la première étape est de déteminer l'id de l'arret d'arrivé


                List<Journey.Leg.IntermediateStop> intermediateStopList= new ArrayList<>();

                // on s'arrête lorsque qu'on a parcouru le bon nombre de connections
                // la dernière connection est donc la connection finale avant l'arrêt
                // intermediaire
                while (numberOfStopsBeforeChange > 0) {

                    finalLegConnectionId = profile.connections().nextConnectionId(currentConnectionId);

                    new Journey.Leg.IntermediateStop();
                    numberOfStopsBeforeChange--;
                }


                // on récupère l'id de la gare d'arrivée
                int arrStationId = profile.connections().depStopId(finalLegConnectionId);

                // on crée notre instance de Stop
                Stop arrStop = getStopInstance(profile, arrStationId);

                // ------ Récupération des heures de départ et arrivée

                LocalDateTime departureDate = getLocalDateTime(profile, currentConnectionId);
                LocalDateTime arrivalDate = getLocalDateTime(profile, finalLegConnectionId);

                Journey.Leg currentLeg;

                // CAS FOOT
                if (arrStationId == depStopId) {
                    currentLeg = new Journey.Leg.Foot(
                            depStop,
                            departureDate,
                            arrStop,
                            arrivalDate
                    );



                } else {
                    // Cas transport leg

                    currentLeg = new Journey.Leg.Transport(
                            depStop,
                            departureDate,
                            arrStop,
                            arrivalDate,

                    )
                }

                // On finit par ajouter le leg à la liste initiale
                currentLegsList.add(currentLeg);


                // ------- Update du criteria pour le prochain leg ------

                // On update la variable puisqu'on sait que
                // un changement a été effectué dans la boucle
                --numberOfChanges;

                // Récupération de la frontière de Pareto de la gare d'arrivée
                ParetoFront nextFront = profile.forStation(arrStationId);
                // avec la nouvelle frontière, on peut update le criteria
                // en effet, on sait l'heure finale et le nombre de changements
                // restants
                criteria = nextFront.get(finalArrMins, numberOfChanges);
            }

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

        // Changement restants (ou le nombre d'étapes (legs) qu'il nous reste à ajouter)
        int numberOfChangesRemaining = numberOfChanges;


        // BOUCLE DE LA MORT : Créons une leg pour chaque changement restant (exemple : leg entre Epfl et Renens)
        while (numberOfChangesRemaining >= 0) {

            // Première connexion du critère (EPFL -> BASSENGES) (on peut accéder aux attributs avec les 4 méthodes)
            int currentConnectionId = Bits32_24_8.unpack24(criteriaPayload);
            // Nb d'arrêts intermédiaires (Pour aller à Renens depuis EPFL, il y en a 4)
            int stopsBeforeLastStop = Bits32_24_8.unpack8(criteriaPayload);

            // On ne sait pour l'instant pas s'l y a une connexion suivante, donc la finale = la première
            int nextConnexionId = currentConnectionId;


            // ---------- 1) Création de l'arrêt de départ -------------------
            // ID du stop de départ, qui est celui de notre première et actuelle connexion
            int depStopId = profile.connections().depStopId(currentConnectionId);
            Stop depStop = getStopInstance(profile, depStopId);

            // --------- 2) Création de la date de départ -------------
            LocalDateTime departureDate = getLocalDateTime(profile, currentConnectionId);

            // ---------- 3) Création des arrêts intermédiaires ------------------
            List<Journey.Leg.IntermediateStop> intermediateStopList = new LinkedList<>();

            // Tant qu'il reste des arrêts intermédiaires,
            while (stopsBeforeLastStop > 0) {

                // On continue de regarder la connexion d'après
                nextConnexionId = profile.connections().nextConnectionId(currentConnectionId);

                // On en profite pour créer les arrêts intermédiaires
                int stopId = profile.connections().depStopId(nextConnexionId);
                Stop intermediateStop = getStopInstance(profile, stopId);
                LocalDateTime arrTime = getLocalDateTime(profile, nextConnexionId);
                LocalDateTime depTime = getLocalDateTime(profile, nextConnexionId);

                // Ajout de l'arrêt intermédiaire à la liste
                intermediateStopList.add(new Journey.Leg.IntermediateStop(intermediateStop, arrTime, depTime));

                // Décrémente le nombre d'arrêts restants
                stopsBeforeLastStop--;
            }

            // ---------- 4) Création de l'arrêt d'arrivée -----------------
            //ID du stop d'arrivée qui est celui de notre dernière connexion
            int arrStationId = profile.connections().depStopId(nextConnexionId);
            Stop arrStop = getStopInstance(profile, arrStationId);

            // ---------- 5) Création de l'heure d'arrivée ------------------
            LocalDateTime arrivalDate = getLocalDateTime(profile, nextConnexionId);




            // ---------- ON A DE QUOI INSTANCIER UNE LEG, MAIS QUEL TYPE DE LEG ???? ------------

            // CAS FOOT
            if (arrStationId == depStopId) {
                currentLegsList.add(new Journey.Leg.Foot(
                        depStop,
                        departureDate,
                        arrStop,
                        arrivalDate)
                );
            }

            // CAS TRANSPORT
            else {
                // besoin de ces Ids pour instancier la leg
                int tripId = profile.connections().tripId(currentConnectionId); // Obtient l'ID de la course
                int routeId = profile.trips().routeId(tripId);

                currentLegsList.add(
                        new Journey.Leg.Transport(
                        depStop,
                        departureDate,
                        arrStop,
                        arrivalDate,
                        intermediateStopList,
                        profile.timeTable().routes().vehicle(routeId),
                        profile.timeTable().routes().name(routeId),
                        profile.trips().destination(tripId))
                );
            }


            // --------- FIN DE BOUCLE ---------

            // On décrémente le nombre de changement (ou le nombre de legs restants à étudier)
            --numberOfChanges;

            // On prépare les données pour la prochaine leg, en choppant le critère et son payload
            ParetoFront nextFront = profile.forStation(arrStationId);
            long futureCriteria = nextFront.get(finalArrMins, numberOfChanges);
            criteriaPayload = PackedCriteria.payload(futureCriteria);

        }

        return new Journey(currentLegsList);
    }

    // Fonction qui crée une instance de Stop à partir de l'id de l'arret
    private static Stop getStopInstance(Profile profile, int stopId) {

        // récupération des attributs nécessaires pour instancier
        // l'arrêt de départ
        // Todo erreur en bas, j'ai essayé de mettre la ligne commentée avec station id. c'est lié à
        // la remarque du prof dans la partie 3), il dit qu'il faut utiliser cette méthode, mais où

        // Convertir les quais en stations si nécessaire
        int stationId = profile.timeTable().stationId(stopId);

        String depStationName = profile.timeTable().stations().name(stationId);
        double depLongitude = profile.timeTable().stations().longitude(stationId);
        double depLatitude = profile.timeTable().stations().latitude(stationId);
        String depPlatformName = profile.timeTable().platformName(stationId);

        // création de l'instance de l'arrêt de départ

        return new Stop(
                depStationName,
                depPlatformName,
                depLongitude,
                depLatitude
        );
    }

    private static LocalDateTime getLocalDateTime(Profile profile, int currentConnectionId) {

        // minutes depuis miniuit
        int minutesTimes = profile.connections().depMins(currentConnectionId);

        int hours = minutesTimes / 60;
        int minutes = minutesTimes % 60;

        return profile.date().atTime(hours, minutes);
    }

}
