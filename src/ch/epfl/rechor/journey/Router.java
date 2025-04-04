package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Classe qui représente un "Routeur" c.-à-d. un objet
 * capable de calculer le profil de tous les voyages optimaux
 * permettant de se rendre de n'importe quelle gare du réseau à un gare d'arrivée donnée,
 * un jour donné
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record Router(FileTimeTable timetable) {
    /**
     * Méthode qui retourne le profil de tous les voyages optimaux
     * permettant de se rendre de n'importe quelle gare du réseau à un gare d'arrivée donnée,
     * un jour donné
     * @param date la date du voyage
     * @param arrStationId l'identifiant de la gare d'arrivée
     * @return le profil des voyages optimaux
     */
    public Profile profile(LocalDate date, int arrStationId) {

        Profile.Builder profileBuilder = new Profile.Builder(timetable, date, arrStationId);

        // Algorithme CSA
        // On parcourt la totalité des liaisons de l'horaire, dans l'ordre décroissant
        for(int i = timetable.connectionsFor(date).size() - 1; i >= 0; i--) {
            // Extraction des informations de notre liaison actuelle
            int currentConnDepStopID = timetable.connectionsFor(date).depStopId(i);
            int currentConnArrStopID = timetable.connectionsFor(date).arrStopId(i);
            int currentConnDepMins = timetable.connectionsFor(date).depMins(i);
            int currentConnArrMins = timetable.connectionsFor(date).arrMins(i);
            int currentConnTripId = timetable.connectionsFor(date).tripId(i);
            int currentConnTripPos = timetable.connectionsFor(date).tripPos(i);

            ParetoFront.Builder f = new ParetoFront.Builder();

            // ------------------ Option 1) ---------------
            // Si il existe un changement jusqu'à la gare
            // d'arrivée (depuis la fin de notre liaison), on y marche

            boolean changeToFinalDestinationExist;
            int changeDuration = 0;

            try {
                changeDuration = timetable().transfers().minutesBetween(currentConnArrStopID, arrStationId);
                changeToFinalDestinationExist = true;
            } catch (NoSuchElementException e) {
                changeToFinalDestinationExist = false;
            }

            if (changeToFinalDestinationExist) {
                f.add(PackedCriteria.pack(currentConnArrMins + changeDuration, 0, 0)); // Payload 0
            }

            // ------------------ Option 2) ---------------
            // On continue notre trajet normalement, et on
            // ajoute à la frontière tous les tuples de ce trip (course)
            f.addAll(profileBuilder.forTrip(currentConnTripId));


            // ------------------ Option 3) ---------------
            // Gère le changement de véhicule, donc les transitions entre les routes

            // Préparons le flot pour effectuer méthodes
            ParetoFront.Builder pfb = profileBuilder.forStation(currentConnArrStopID).forEach();
            List<Long> tuples = new ArrayList<>();
            pfb.forEach(tuples::add);

            // Opérations sur le flot
            tuples.stream()
                    .filter(criteria -> PackedCriteria.hasDepMins(criteria) // On garde seulement ceux qui n'ont pas
                            && PackedCriteria.depMins(criteria) >= currentConnArrMins) // d'anomalie temporelle
                    .forEach(criteria -> {
                        // Extraction des données
                        int criteriaArrMin = PackedCriteria.arrMins(criteria);
                        int criteriaChanges = PackedCriteria.changes(criteria);

                        // Ajout à la frontière en cours de construction
                        f.add(PackedCriteria.pack(criteriaArrMin, criteriaChanges + 1, 0)); // Payload 0

                    });

            //

        }

        // Va return le profil de tous les voyages optimaux permettant de se rendre
        // à arrStationId (commentaire à suppr mais pour l'instant je laisse
        return null;
    }
}
