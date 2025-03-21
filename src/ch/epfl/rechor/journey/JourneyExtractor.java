package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.TimeTable;

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

        // On itère sur tous les critères
        // de la frontière
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

                // récupération des attributs nécessaires pour instancier
                // l'arrêt de départ
                String depStationName = profile.timeTable().stations().name(depStopId);
                double depLongitude = profile.timeTable().stations().longitude(depStopId);
                double depLatitude = profile.timeTable().stations().latitude(depStopId);
                String deplPlatformName = profile.timeTable().platformName(depStopId);


                // création de l'instance de l'arrêt de départ
                Stop depStop = new Stop(
                        depStationName,
                        deplPlatformName,
                        depLongitude,
                        depLatitude
                );

                // Création de l'arrêt d'arrivée

                // la première étape est de déteminer l'id de l'arret d'arrivé


                List<Journey.Leg.IntermediateStop> intermediateStopList= new ArrayList<>();

                // on s'arrête lorsque qu'on a parcouru le bon nombre de connections
                // la dernière connection est donc la connection finale avant l'arrêt
                // intermediaire
                while (numberOfStopsBeforeChange > 0) {

                    finalLegConnectionId = profile.connections().nextConnectionId(currentConnectionId);

                    new Journey.Leg.IntermediateStop()
                    numberOfStopsBeforeChange--;
                }


                // on récupère l'id de la gare d'arrivée
                int arrStationId = profile.connections().depStopId(finalLegConnectionId);

                // récupération des attributs nécessaires pour instancier
                // l'arrêt de départ
                String arrStationName = profile.timeTable().stations().name(arrStationId);
                double arrLongitude = profile.timeTable().stations().longitude(arrStationId);
                double arrLatitude = profile.timeTable().stations().latitude(arrStationId);
                String arrPlatformName = profile.timeTable().platformName(arrStationId);


                Stop arrStop = new Stop(
                        arrStationName,
                        arrPlatformName,
                        arrLongitude,
                        arrLatitude
                );

                // ------ Récupération des heures de départ et arrivée

                // L'heure de départ du leg est l'heure de départ de la première connection
                int depLegTime = profile.connections().depMins(currentConnectionId);
                // L'heure d'arrivée du leg est l'heure d'arrivée de la dernière connection
                int arrLegTime = profile.connections().arrMins(finalLegConnectionId);

                int depLegHours = depLegTime / 60;
                int depLegMinutes = depLegTime % 60;
                LocalDateTime departureDate = profile.date().atTime(depLegHours, depLegMinutes);

                int arrLegHours = arrLegTime / 60;
                int arrLegMinutes = arrLegTime % 60;
                LocalDateTime arrivalDate = profile.date().atTime(arrLegHours, arrLegMinutes);


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

        });

        // Tri de journey, par le code donné dans l'énoncé
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    private Stop getStop(depStopId)
}
