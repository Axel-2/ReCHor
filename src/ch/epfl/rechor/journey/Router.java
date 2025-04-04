package ch.epfl.rechor.journey;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.TimeTable;

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
public record Router(TimeTable timetable) {


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

        // On parcourt la totalité des liaisons de l'horaire, dans l'ordre décroissant
        // comme "connectionsFor" retourne déjà les connections dans l'ordre décroissant,
        // il suffit de parcourir dans l'ordre croissant
        for (int i = 0; i < timetable.connectionsFor(date).size(); i++) {

            // Extraction des informations de notre liaison actuelle
            int currentConnDepStopID = timetable.connectionsFor(date).depStopId(i);
            int currentConnArrStopID = timetable.connectionsFor(date).arrStopId(i);
            int currentConnDepMins = timetable.connectionsFor(date).depMins(i);
            int currentConnArrMins = timetable.connectionsFor(date).arrMins(i);
            int currentConnTripId = timetable.connectionsFor(date).tripId(i);
            int currentConnTripPos = timetable.connectionsFor(date).tripPos(i);

            // 'f' est la frontière temporaire pour cette liaison 'l'
            ParetoFront.Builder f = new ParetoFront.Builder();


            // ------------------ Option 1) Marcher depuis arr(l) vers la destination finale ---------------
            // Si il existe un changement jusqu'à la gare
            // d'arrivée (depuis la fin de notre liaison), on y marche

            boolean changeToFinalDestinationExist;

            // on utilise le tableau calculé plus haut pour voir si un
            // changement existe entre les deux gares

            int arrStationIdForWalk = timetable.stationId(currentConnArrStopID);
            int walkDuration = minutesBetweenForEveryStation[arrStationIdForWalk];

            if (walkDuration != -1) {
                f.add(PackedCriteria.pack(currentConnArrMins + walkDuration, 0, 0));
            }


            // ------------------ Option 2) Rester sur la même course ---------------
            // On continue notre trajet normalement, et on
            // ajoute à la frontière tous les tuples de cette course

            // Vérification si non null
            if (profileBuilder.forTrip(currentConnTripId) != null) {
                f.addAll(profileBuilder.forTrip(currentConnTripId));
            }

            // ------------------ Option 3) Changer de véhicule à arr(l) ---------------
            // Gère le changement de véhicule, donc les transitions entre les routes

            // Préparons le flot pour effectuer méthodes
            List<Long> tuples = new ArrayList<>();

            if (profileBuilder.forStation(timetable.stationId(currentConnArrStopID)) != null) { // SEULEMENT si un builder existe pour cette gare

                profileBuilder.forStation(timetable.stationId(currentConnArrStopID)).forEach(tuples::add);

                tuples.stream().filter(criteria -> PackedCriteria.hasDepMins(criteria) // On garde seulement ceux qui n'ont pas
                                && PackedCriteria.depMins(criteria) >= currentConnArrMins) // d'anomalie temporelle
                        .forEach(criteria -> {
                            // Extraction des données
                            int criteriaArrMin = PackedCriteria.arrMins(criteria);
                            int criteriaChanges = PackedCriteria.changes(criteria);

                            // Ajout à la frontière en cours de construction
                            f.add(PackedCriteria.pack(criteriaArrMin, criteriaChanges + 1, 0)); // Payload 0

                        });

            }



            // ----------------- Dernière partie -------------------

            // Mise à jour de la frontière de la liaison

            if (profileBuilder.forTrip(currentConnTripId) != null) {
                profileBuilder.forTrip(currentConnTripId).addAll(f);
            } else {
                profileBuilder.setForTrip(currentConnTripId, f);
            }

            // Mise à jour des frontières des gares

            // Récupération des changements arrivant au départ de notre liaison
            int depStationIdForTransfer = timetable.stationId(currentConnDepStopID);
            int intervalOfTransfersArrivingToDep = timetable.transfers().arrivingAt(depStationIdForTransfer);
            int transferStart = PackedRange.startInclusive(intervalOfTransfersArrivingToDep);
            int transferEnd = PackedRange.endExclusive(intervalOfTransfersArrivingToDep);

            for (int transferId = transferStart; transferId < transferEnd; transferId++) {
                int transferDepStationID = timetable.transfers().depStationId(transferId);
                int transferDuration = timetable.transfers().minutes(transferId);

                // C'est l'heure (en minutes) où le transfert commence.
                // Ou, vu autrement, c'est aussi l'heure ou la course précédente arrive.
                int previousTripArrMins = currentConnDepMins - transferDuration;

                ParetoFront.Builder depStationFront = profileBuilder.forStation(transferDepStationID);

                if (depStationFront == null) {
                    profileBuilder.setForStation(transferDepStationID , new ParetoFront.Builder());
                }

                if (profileBuilder.forStation(transferDepStationID) == null) {
                    System.out.println("hhhhh");
                }

                 // Pour tous les tuples de la frontière
                f.forEach(tuple -> {

                    // Extraction des données du tuple
                    int arrMins = PackedCriteria.arrMins(tuple);
                    int changes = PackedCriteria.changes(tuple);

                    long tupleToAdd = PackedCriteria.pack(arrMins, changes, 0);
                    tupleToAdd = PackedCriteria.withDepMins(tupleToAdd, previousTripArrMins);

                    profileBuilder.forStation(transferDepStationID).add(tupleToAdd);
                });
            }


        }
        return profileBuilder.build();
    }
}
