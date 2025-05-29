package ch.epfl.rechor.journey;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Classe qui représente un routeur qui est un objet
 * capable de calculer le profil de tous les voyages optimaux
 * permettant de se rendre de n'importe quelle gare du réseau à une gare d'arrivée donnée,
 * un jour donné
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record Router(TimeTable timetable) {


    /**
     * Méthode qui retourne le profil de tous les voyages optimaux
     * permettant de se rendre de n'importe quelle gare du réseau à une gare d'arrivée donnée,
     * un jour donné
     * @param date la date du voyage
     * @param arrStationId l'identifiant de la gare d'arrivée
     * @return le profil des voyages optimaux
     */
    public Profile profile(LocalDate date, int arrStationId) {
        // La date ne doit pas être nulle
        Objects.requireNonNull(date);

        // On crée un profil vide à l'aide du Builder
        Profile.Builder p = new Profile.Builder(timetable, date, arrStationId);

        int[] minutesBetweenForEveryStation  = calculateMinuteBetween(arrStationId);

        // Algorithme CSA

        Connections connections = timetable.connectionsFor(date);

        // On parcourt la totalité des liaisons de l'horaire, dans l'ordre décroissant
        // comme "connectionsFor" retourne déjà les connections dans l'ordre décroissant,
        // il suffit de parcourir dans l'ordre croissant.
        for (int i = 0, n = connections.size(); i < n; i++) {
            // 'f' est la frontière temporaire pour cette liaison 'l'
            ParetoFront.Builder f = new ParetoFront.Builder();

            // Extraction des informations de notre liaison actuelle
            int currentConnDepStopID = connections.depStopId(i);
            int currentConnArrStopID = connections.arrStopId(i);
            int currentConnDepMins   = connections.depMins(i);
            int currentConnArrMins   = connections.arrMins(i);
            int currentConnTripId    = connections.tripId(i);
            int currentConnTripPos   = connections.tripPos(i);

            int currentConnArrStationId = timetable.stationId(currentConnArrStopID);
            int currentConnDepStationId = timetable.stationId(currentConnDepStopID);

            // Option 1
            checkOption1(f, minutesBetweenForEveryStation, currentConnArrStationId, currentConnArrMins, i);

            // Option 2 : continuer avec la liaison suivante
            checkOption2(p, f, currentConnTripId);

            // Option 3) Changer de véhicule à arr(l) ---------------
            checkOption3(p, f, currentConnArrStopID, currentConnArrMins, i);

            // On sort de la boucle si la frontière est vide
            if (f.isEmpty()) continue;

            // ----------------- Dernière partie -------------------

            // Mise à jour de la frontière de la course
            if (p.forTrip(currentConnTripId) != null) {
                p.forTrip(currentConnTripId).addAll(f);
            } else {
                p.setForTrip(currentConnTripId, f);
            }

            // OPTIMISATION :
            // Si la frontière de la gare de départ domine entièrement f avec l'heure de départ de la liaison actuelle,
            // alors on peut skip la dernière partie, car il n'y aura rien d'utile dans f aux gares de départ
            if (p.forStation(currentConnDepStationId) != null) {
                if (p.forStation(currentConnDepStationId).fullyDominates(f, currentConnDepMins)) {
                    continue;
                }
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

                // C'est l'heure de départ du petit trajet d'avant, pour rejoindre la gare actuelle
                int d = currentConnDepMins - transferDuration;

                // Si c'est le premier cas où l'on a affaire à cette gare,
                // ce sera null et il faut créer un builder de frontière
                if (p.forStation(transferDepStationID) == null) {
                    p.setForStation(transferDepStationID , new ParetoFront.Builder());
                }

                 // Pour tous les tuples de la frontière
                int connId = i;
                f.forEach(tuple -> {

                    // Extraction des données du tuple
                    int arrMins = PackedCriteria.arrMins(tuple);
                    int changes = PackedCriteria.changes(tuple);

                    // GESTION DU PAYLOAD
                    int lastConnexionOfTripID = PackedCriteria.payload(tuple);
                    int lastConnOfTripPos = connections.tripPos(lastConnexionOfTripID);
                    int intermediateStopsNumber = lastConnOfTripPos - currentConnTripPos;
                    // Le payload contient la liaison l dans les 24 bits de gauche,
                    // et le nombre d'arrêts intermédiaires dans les 8 bits de droite.

                    int payload = connId << 8 | intermediateStopsNumber;

                    long tupleToAdd = PackedCriteria.pack(arrMins, changes, payload);
                    tupleToAdd = PackedCriteria.withDepMins(tupleToAdd, d);

                    p.forStation(transferDepStationID).add(tupleToAdd);
                });
            }

        }

        return p.build();
    }


    /**
     * Fonction qui calcule les minutes de changement entre toutes les sations
     * dans un tableau
     * @param arrStationId l'id de la station à partir duquel on part
     * @return un tableau qui contient les minutes
     */
    private int[] calculateMinuteBetween(int arrStationId) {
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

        return minutesBetweenForEveryStation;
    }

    /**
     * Fonction qui vérifie l'option 1
     * @param f un bâtisseur de frontière
     * @param minutesBetweenForEveryStation tableau qui contient les minutes entre les stations
     * @param currentConnArrStationId l'id de la station d'arrivée
     * @param currentConnArrMins minutes minimum de la connection
     * @param i index courant
     */
    private void checkOption1(
            ParetoFront.Builder f,
            int[] minutesBetweenForEveryStation,
            int currentConnArrStationId,
            int currentConnArrMins,
            int i // l'id de la connection courante
    ) {
        // ------------------ Option 1) Marcher depuis arr(l) vers la destination finale ---------------
        // Si il existe un changement jusqu'à la gare
        // d'arrivée (depuis la fin de notre liaison), on y marche
        // on, utilise le tableau calculé plus haut pour voir si un
        // changement existe entre les deux gares
        int walkDuration = minutesBetweenForEveryStation[currentConnArrStationId];

        if (walkDuration != -1) {
            long t = PackedCriteria.pack(currentConnArrMins + walkDuration, 0, i);
            t = PackedCriteria.withDepMins(t, currentConnArrMins);
            f.add(t);
        }
    }

    /**
     * Fonction qui vérifie l'option 2
     * @param p un bâtisseur de profil
     * @param f un bâtisseur de frontière
     * @param currentConnTripId l'id de la connection courante
     */
    private void checkOption2(Profile.Builder p, ParetoFront.Builder f, int currentConnTripId) {
        // ------------------ Option 2) Rester sur la même course ---------------
        // On continue notre trajet normalement, et on
        // ajoute à la frontière tous les tuples de cette course

        // On vérifie que ce n'est pas null, sinon f.addAll(null) lèvera une exception
        if (p.forTrip(currentConnTripId) != null) {
            f.addAll(p.forTrip(currentConnTripId));
        }
    }

    /**
     * Fonction qui vérifie l'option 3 qui est
     * le changement de véhicule, donc les transitions
     * entre les routes
     * @param p un bâtisseur de profil
     * @param f un bâtisseur de frontière
     * @param currentConnArrStopID l'id de l'arrêt courant
     * @param currentConnArrMins minutes minimum de la connection
     */
    private void checkOption3(
            Profile.Builder p,
            ParetoFront.Builder f,
            int currentConnArrStopID,
            int currentConnArrMins,
            int connId
    ) {

        // liste qui va contenir les tuples à ajouter
        List<Long> tuples = new ArrayList<>();

        // On vérifie que ce n'est pas null, sinon null.forEach lèvera une exception
        if (p.forStation(timetable.stationId(currentConnArrStopID)) != null) { // SEULEMENT si un builder existe pour cette gare

            p.forStation(timetable.stationId(currentConnArrStopID)).forEach(tuples::add);

            tuples.stream()
                    .filter(criteria ->
                            PackedCriteria.hasDepMins(criteria) // On garde seulement ceux qui n'ont pas
                                    && PackedCriteria.depMins(criteria) >= currentConnArrMins
                    ) // d'anomalie temporelle
                    .forEach(criteria -> {
                        int arrMin    = PackedCriteria.arrMins(criteria);
                        int changes   = PackedCriteria.changes(criteria);
                        long t        = PackedCriteria.pack(arrMin, changes + 1, connId);
                        t             = PackedCriteria.withDepMins(t, currentConnArrMins);
                        f.add(t);
                    }
                    );
        }
    }

}
