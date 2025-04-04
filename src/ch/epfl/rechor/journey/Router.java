package ch.epfl.rechor.journey;

import ch.epfl.rechor.PackedRange;
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
    // TODO : gérer le payload
    public Profile profile(LocalDate date, int arrStationId) {

        // On crée un profil vide à l'aide du Builder
        Profile.Builder profileBuilder = new Profile.Builder(timetable, date, arrStationId);

        int[] minutesBetweenForEveryStation  = new int[timetable.stations().size()];

        for (int i = 0; i < timetable.stations().size(); ++i) {
            int currentMinutesBetween;
            try {
                // On essaie d'obtenir le temps de transfer pour chaque gare
                currentMinutesBetween = timetable.transfers().minutesBetween(i, arrStationId);
            } catch (NoSuchElementException e) {
                // On retourne -1 si le trajet n'est pas faisable à pied
                currentMinutesBetween = -1;
            }

            // Et on le met dans notre tableau
            minutesBetweenForEveryStation[i] = currentMinutesBetween;
        }


        // Algorithme CSA
        // On parcourt la totalité des liaisons de l'horaire
        for(int i = 0; i < timetable.connectionsFor(date).size(); i++) {
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

            // on utilise le tableau calculé plus haut pour voir si un
            // changement existe entre les deux gares
            changeToFinalDestinationExist = minutesBetweenForEveryStation[currentConnArrStopID] != -1;

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
            ParetoFront.Builder pfb = profileBuilder.forStation(currentConnArrStopID);
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

            // Mise à jour de la frontière de la liaison
            profileBuilder.forTrip(currentConnTripId).addAll(f);


            // ----------------- Dernière partie -------------------

            // Récupération des changements arrivant au départ de notre liaison
            int intervalOfTransfersArrivingToDep = timetable.transfers().arrivingAt(currentConnDepStopID);
            int transferStart = PackedRange.startInclusive(intervalOfTransfersArrivingToDep);
            int transferEnd = PackedRange.endExclusive(intervalOfTransfersArrivingToDep);

            for (int transferId = transferStart; transferId < transferEnd; transferId++) {
                int transferDepStationID = timetable.transfers().depStationId(transferId);
                int transferDuration = timetable.transfers().minutes(transferId);

                // C'est l'heure (en minutes) où le transfert commence.
                // Ou, vu autrement, c'est aussi l'heure ou la course précédente arrive.
                int previousTripArrMins = currentConnDepMins - transferDuration;

                ParetoFront.Builder transferPf = profileBuilder.forStation(transferDepStationID);

                 // Pour tous les tuples de la frontière
                f.forEach(tuple -> {

                    // Extraction des données du tuple
                    int arrMins = PackedCriteria.arrMins(tuple);
                    int changes = PackedCriteria.changes(tuple);

                    long tupleToAdd = PackedCriteria.pack(arrMins, changes, 0);
                    tupleToAdd = PackedCriteria.withDepMins(tupleToAdd, previousTripArrMins);

                    transferPf.add(tupleToAdd);
                });
            }


        }
        return profileBuilder.build();
    }
}
