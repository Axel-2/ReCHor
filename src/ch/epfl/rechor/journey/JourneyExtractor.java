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
        // TODO
        // Initialisation de la liste des voyages qu'on va retourner
        List<Journey> journeys = new ArrayList<>();

        // On récupère le batisseur de la frontière de pareto de la gare d'index depStationId
        ParetoFront pf = profile.forStation(depStationId);

        // On itère sur tous les critères
        // de la frontière
        pf.forEach((long criteria) -> {

            int numberOfChanges = PackedCriteria.changes(criteria);
            List<Journey.Leg> currentLegsList = new ArrayList<>();

            // on boucle tant qu'il reste des changements
            while (numberOfChanges > 0) {

                int currentPayload = PackedCriteria.payload(criteria);

                int currentConnectionId = Bits32_24_8.unpack24(currentPayload);
                int numberOfStopsBeforeChange = Bits32_24_8.unpack8(currentPayload);

                int currentStationId = profile.connections().depStopId(currentConnectionId);

                int nextConnectionId = currentConnectionId;

                while (numberOfStopsBeforeChange > 0) {
                    nextConnectionId = profile.connections().nextConnectionId(currentConnectionId);
                }

                int nextStationId = profile.connections().depStopId(nextConnectionId);

                // CAS FOOT
                if (nextStationId == currentStationId) {

                } else {
                    // Cas transport leg
                }


            }


            // Ici faut faire un truc


            journeys.get(0).
        });

        // Tri de journey, par le code donné dans l'énoncé
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }
}
