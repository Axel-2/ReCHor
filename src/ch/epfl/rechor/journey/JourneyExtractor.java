package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;

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
                int nextConnectionId = currentConnectionId;

                // -------- Création de l'arrêt de départ -------------------

                // id de la gare de départ
                int depStopId = profile.connections().depStopId(currentConnectionId);

                // récupération des attributs nécessaires pour instancier
                // l'arrêt de départ
                String depStationName = profile.timeTable().stations().name(depStopId);
                double depLongitude = profile.timeTable().stations().longitude(depStopId);
                double depLatitude = profile.timeTable().stations().latitude(depStopId);
                String deplPlatformName = profile.timeTable().platformName(depStopId);

                int depTime = profile.connections().depMins(currentConnectionId);
                int arrTime = profile.connections().depStopId(nextConnectionId);

                // création de l'instance de l'arrêt de départ
                Stop depStop = new Stop(
                        depStationName,
                        deplPlatformName,
                        depLongitude,
                        depLatitude
                );

                // Création de l'arrêt d'arrivée

                // la première étape est de déteminer l'id de l'arret d'arrivé

                // on s'arrête lorsque qu'on a parcouru le bon nombre de connections
                // la dernière connection est donc la connection finale avant l'arrêt
                // intermediaire
                while (numberOfStopsBeforeChange > 0) {
                    nextConnectionId = profile.connections().nextConnectionId(currentConnectionId);
                    numberOfStopsBeforeChange--;
                }

                // on récupère l'id de la gare d'arrivée
                int arrStationId = profile.connections().depStopId(nextConnectionId);

                // récupération des attributs nécessaires pour instancier
                // l'arrêt de départ
                String arrStationName = profile.timeTable().stations().name(depStopId);
                double arrLongitude = profile.timeTable().stations().longitude(depStopId);
                double arrLatitude = profile.timeTable().stations().latitude(depStopId);
                String arrPlatformName = profile.timeTable().platformName(depStopId);


                Stop arrStop = new Stop(
                        arrStationName,
                        arrPlatformName,
                        arrLongitude,
                        arrLatitude
                );

                Journey.Leg currentLeg;

                // CAS FOOT
                if (arrStationId == depStopId) {
                    currentLeg = new Journey.Leg.Foot(
                            depStop,
                            depTime,
                            arrStop,
                            arrTime
                    );



                } else {
                    // Cas transport leg
                }

                // On finit par ajouter le leg à la liste initiale
                currentLegsList.add(currentLeg);


                // ---- Update du criteria pour le prochain leg ------

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
}
