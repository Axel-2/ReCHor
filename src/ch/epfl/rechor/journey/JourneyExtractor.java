package ch.epfl.rechor.journey;

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

        // On récupère le bâtisseur de la frontière de pareto de la gare d'index depStationId
        ParetoFront pf = profile.forStation(depStationId);

        // Pour chacun de ces critères (triplets), ...
        pf.forEach((long criteria) -> {

            // Création du journey qu'on va ajouter à la liste plus tard, 1 par critères
            List<Journey.Leg> legs = new ArrayList<Journey.Leg>();

            // Changements restants avant la fin de l'étude de ce critère
            int remainingChanges = PackedCriteria.changes(criteria);

            // Boucle qui va se répéter pour créer les legs une par unes
            while (remainingChanges > 0){

            }

        });

        // Tri de journey, par le code donné dans l'énoncé
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }
}
