package ch.epfl.rechor.journey;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Connections; // Nécessaire pour accéder aux infos des connexions

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.function.Predicate; // Pour l'optimisation 2

// Supposons l'existence de méthodes statiques pour gérer le payload
// import static ch.epfl.rechor.journey.PackedPayload.*; // A adapter selon ta classe de payload

/**
 * Classe qui représente un "Routeur" c.-à-d. un objet
 * capable de calculer le profil de tous les voyages optimaux
 * permettant de se rendre de n'importe quelle gare du réseau à un gare d'arrivée donnée,
 * un jour donné.
 * VERSION CORRIGÉE AVEC PAYLOAD ET OPTIMISATIONS
 * (Suppose que Profile.Builder est corrigé et ne retourne jamais null)
 *
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 * @author Gemini (Corrections)
 */
public record Router(TimeTable timetable) {

    // --- Méthodes utilitaires pour le Payload (à implémenter/adapter) ---
    // Ces méthodes dépendent de comment tu as défini l'empaquetage du payload.
    // Ceci est un EXEMPLE.

    private static final int PAYLOAD_CONN_ID_SHIFT = 8;
    private static final int PAYLOAD_STOPS_MASK = 0xFF;

    // Pour les tuples de Course/Liaison (stocke l'ID de la connexion où descendre)
    private static long packTripPayload(int exitConnectionId) {
        // Exemple simple : on stocke juste l'ID de la connexion
        // (pourrait être plus complexe si le payload du tuple de base est utilisé)
        return (long) exitConnectionId; // Suppose que le payload du tuple est juste l'ID
    }

    private static int getExitConnectionIdFromPayload(long payload) {
        // Exemple simple : le payload EST l'ID
        return (int) payload;
    }

    // Pour les tuples de Gare (stocke ID 1ere connexion + nb arrêts intermédiaires)
    private static long packStationPayload(int firstConnectionId, int intermediateStops) {
        // Exemple basé sur l'énoncé : 24 bits pour connId, 8 bits pour stops
        if (intermediateStops < 0 || intermediateStops > PAYLOAD_STOPS_MASK) {
            // Gérer l'erreur ou plafonner, ici on plafonne pour l'exemple
            intermediateStops = Math.max(0, Math.min(PAYLOAD_STOPS_MASK, intermediateStops));
            System.err.println("WARN: Nombre d'arrêts intermédiaire (" + intermediateStops + ") hors limites [0, 255] pour connexion " + firstConnectionId);
        }
        return (((long) firstConnectionId) << PAYLOAD_CONN_ID_SHIFT) | intermediateStops;
    }

    // --- Fin Méthodes Payload ---


    /**
     * Méthode qui retourne le profil de tous les voyages optimaux
     * permettant de se rendre de n'importe quelle gare du réseau à un gare d'arrivée donnée,
     * un jour donné. Version corrigée incluant Payload et Optimisations.
     *
     * @param date la date du voyage
     * @param arrStationId l'identifiant de la gare d'arrivée (destination finale)
     * @return le profil des voyages optimaux
     */
    public Profile profile(LocalDate date, int arrStationId) {

        // Utilisation du Profile.Builder (supposé corrigé pour ne jamais retourner null)
        Profile.Builder profileBuilder = new Profile.Builder(timetable, date, arrStationId);
        Connections connections = timetable.connectionsFor(date); // Obtenir les connexions une seule fois

        // Précalcul minutesBetweenForEveryStation (correct)
        int[] minutesBetweenForEveryStation = new int[timetable.stations().size()];
        for (int stationIdx = 0; stationIdx < timetable.stations().size(); ++stationIdx) {
            try {
                minutesBetweenForEveryStation[stationIdx] = timetable.transfers().minutesBetween(stationIdx, arrStationId);
            } catch (NoSuchElementException e) {
                minutesBetweenForEveryStation[stationIdx] = -1;
            }
        }

        // Algorithme CSA
        for (int i = 0; i < connections.size(); i++) { // i est l'ID de la connexion courante 'l'
            int currentConnDepStopID = connections.depStopId(i);
            int currentConnArrStopID = connections.arrStopId(i);
            int currentConnDepMins = connections.depMins(i);
            int currentConnArrMins = connections.arrMins(i);
            int currentConnTripId = connections.tripId(i);
            int currentConnTripPos = connections.tripPos(i); // Position de la connexion 'l' dans sa course

            // Obtenir les ID de STATION correspondants
            int currentDepStationId = timetable.stations().stationId(currentConnDepStopID);
            int currentArrStationId = timetable.stations().stationId(currentConnArrStopID);

            // Front temporaire 'f' pour la connexion 'l'
            ParetoFront.Builder fBuilder = new ParetoFront.Builder();

            // --- Option 1: Marcher de arr(l) à la destination finale ---
            int walkDuration = minutesBetweenForEveryStation[currentArrStationId];
            if (walkDuration != -1) {
                int arrivalTimeAtDest = currentConnArrMins + walkDuration;
                long payload = packTripPayload(i); // La sortie se fait à la fin de la connexion 'i'
                // Ajoute (temps_arrivée, nb_changements=0, payload_sortie_connexion_i)
                fBuilder.add(PackedCriteria.pack(arrivalTimeAtDest, 0, payload));
            }

            // --- Option 2: Continuer avec la course suivante ---
            // Récupère le builder pour la course (garanti non-nul par Profile.Builder corrigé)
            ParetoFront.Builder existingTripBuilder = profileBuilder.forTrip(currentConnTripId);
            // Ajoute tous les tuples (avec leur payload existant) du profil de la course à 'f'
            fBuilder.addAll(existingTripBuilder);

            // --- Option 3: Changer de véhicule à arr(l) ---
            // Récupère le builder pour la station d'arrivée (garanti non-nul)
            ParetoFront.Builder arrStationBuilder = profileBuilder.forStation(currentArrStationId);

            arrStationBuilder.forEach(stationTuple -> {
                // stationTuple contient (depTime, arrTime, changes, stationPayload)
                // On ne peut changer que si on arrive à la gare AVANT le départ du prochain trajet
                if (PackedCriteria.depMins(stationTuple) >= currentConnArrMins) {
                    int nextLegArrTime = PackedCriteria.arrMins(stationTuple);
                    int nextLegChanges = PackedCriteria.changes(stationTuple); // Changements APRES la connexion 'l'

                    // Le payload pour 'f' indique qu'on sort à la fin de la connexion 'i'
                    long payload = packTripPayload(i);
                    // Ajoute (temps_arrivée_final, nb_changements_total = 1 + chg_suivants, payload_sortie_connexion_i)
                    fBuilder.add(PackedCriteria.pack(nextLegArrTime, nextLegChanges + 1, payload));
                }
            });

            // --- Optimisation 1: Si f est vide, passer à la liaison suivante ---
            if (fBuilder.isEmpty()) {
                continue;
            }

            // --- Mise à jour de la frontière de la course p[trp(l)] ---
            // Récupère le builder de la course (garanti non-nul)
            ParetoFront.Builder tripBuilderToUpdate = profileBuilder.forTrip(currentConnTripId);
            // Ajoute tous les tuples de 'f' (calculés ci-dessus) au profil de la course
            tripBuilderToUpdate.addAll(fBuilder);

            // --- Optimisation 2: Vérifier dominance avant màj gares ---
            // Récupère le builder de la station de départ (garanti non-nul)
            ParetoFront.Builder depStationBuilder = profileBuilder.forStation(currentDepStationId);
            // Si tous les voyages via 'f' sont dominés par ceux déjà connus depuis dep(l), on saute la màj
            if (isDominated(fBuilder, currentConnDepMins, depStationBuilder)) {
                continue;
            }

            // --- Mise à jour des frontières des gares p[dep(c)] ---
            // Pour tous les changements 'c' arrivant à la station de départ dep(l)
            int intervalOfTransfersArrivingToDep = timetable.transfers().arrivingAt(currentDepStationId);
            int transferStart = PackedRange.startInclusive(intervalOfTransfersArrivingToDep);
            int transferEnd = PackedRange.endExclusive(intervalOfTransfersArrivingToDep);

            for (int transferId = transferStart; transferId < transferEnd; transferId++) {
                int transferOriginStationID = timetable.transfers().depStationId(transferId); // Gare d'où part le transfert à pied 'c'
                int transferDuration = timetable.transfers().minutes(transferId);

                // Heure de départ depuis la gare d'origine du transfert 'c' pour attraper la connexion 'l'
                int departureTimeFromOrigin = currentConnDepMins - transferDuration;

                // Récupère le builder pour la gare d'origine du transfert (garanti non-nul)
                ParetoFront.Builder originStationBuilder = profileBuilder.forStation(transferOriginStationID);

                // Pour chaque tuple 't' dans la frontière temporaire 'f' calculée pour la connexion 'l'
                fBuilder.forEach(fTuple -> {
                    // fTuple contient (arrTimeFinal, changesTotal, tripPayload)
                    int finalArrTime = PackedCriteria.arrMins(fTuple);
                    int totalChanges = PackedCriteria.changes(fTuple);
                    long tripPayload = PackedCriteria.payload(fTuple); // Payload indiquant où descendre

                    // Dépaqueter le payload de 'f' pour trouver où on est descendu
                    int exitConnectionId = getExitConnectionIdFromPayload(tripPayload);

                    // Calculer le payload pour le profil de la gare
                    int exitConnectionPos;
                    try {
                        // Il faut pouvoir trouver la position d'une connexion par son ID.
                        // Cette info est dans connections mais nécessite potentiellement une map ou une recherche.
                        // Supposons une méthode qui fait ça (à implémenter si besoin).
                        // Si l'ID est l'index, c'est facile, mais pas garanti.
                        // Pour l'exemple, si l'ID EST l'index : exitConnectionPos = connections.tripPos(exitConnectionId);
                        // Si ce n'est pas le cas, il faut un moyen de retrouver la position.
                        // Ici, on va supposer que getExitConnectionIdFromPayload nous donne l'index 'i' si
                        // le payload a été créé avec packTripPayload(i).
                        exitConnectionPos = connections.tripPos(exitConnectionId); // Suppose que exitConnectionId peut être utilisé ici
                    } catch (/*Exception si ID non trouvé ou pas un index*/) {
                        System.err.println("ERREUR: Impossible de trouver la position de la connexion de sortie " + exitConnectionId);
                        return; // Ne pas ajouter ce tuple au profil de gare
                    }

                    int intermediateStops = exitConnectionPos - currentConnTripPos;
                    if (intermediateStops < 0) {
                        System.err.println("WARN: Nombre d'arrêts intermédiaires négatif (" + intermediateStops + ") pour première connexion " + i + " et sortie " + exitConnectionId);
                        intermediateStops = 0; // ou gérer l'erreur autrement
                    }

                    // La connexion 'i' est la première de cette partie du voyage depuis la gare d'origine
                    long stationPayload = packStationPayload(i, intermediateStops);

                    // Créer le tuple final pour le profil de la gare d'origine
                    // (depTime=departureTimeFromOrigin, arrTime=finalArrTime, changes=totalChanges, payload=stationPayload)
                    long stationTupleToAdd = PackedCriteria.pack(finalArrTime, totalChanges, stationPayload);
                    stationTupleToAdd = PackedCriteria.withDepMins(stationTupleToAdd, departureTimeFromOrigin);

                    // Ajouter au builder de la gare d'origine
                    originStationBuilder.add(stationTupleToAdd);
                });
            } // Fin boucle sur les transferts
        } // Fin boucle principale sur les connexions

        return profileBuilder.build();
    }

    /**
     * Vérifie si tous les voyages possibles via la frontière temporaire 'f',
     * en partant à 'depTime', sont dominés par des voyages déjà connus
     * depuis la station de départ (contenus dans 'depStationFrontBuilder').
     * (Optimisation 2)
     * @param fBuilder Le builder de la frontière temporaire pour la connexion courante.
     * @param depTime L'heure de départ de la connexion courante (h_dep(l)).
     * @param depStationFrontBuilder Le builder de la frontière de Pareto de la gare de départ.
     * @return true si tous les tuples de f sont dominés, false sinon.
     */
    private boolean isDominated(ParetoFront.Builder fBuilder, int depTime, ParetoFront.Builder depStationFrontBuilder) {
        // Si le front de la gare de départ est vide, rien ne peut dominer f.
        if (depStationFrontBuilder.isEmpty()) {
            return false;
        }

        // Il faut construire le front de la gare de départ pour pouvoir vérifier la dominance.
        // Note: C'est potentiellement inefficace de le construire à chaque fois.
        // Une meilleure approche pourrait être de passer ParetoFront directement si possible,
        // ou d'implémenter la dominance directement sur les builders.
        // Pour cet exemple, on construit le front.
        ParetoFront depStationFront = depStationFrontBuilder.build();

        // On doit vérifier si CHAQUE tuple dans fBuilder est dominé par AU MOINS UN tuple dans depStationFront.
        // La méthode allMatch de Stream serait idéale, mais ParetoFront.Builder n'est pas un Stream.
        // On utilise une approche par itération. On suppose qu'un tuple n'est PAS dominé,
        // et si on en trouve un, on retourne false. Si on finit la boucle, c'est que tous sont dominés.

        // Prédicat pour vérifier si un tuple de 'f' (ajusté avec depTime) est dominé par le front de la gare.
        Predicate<Long> isTupleDominated = fTuple -> {
            int finalArrTime = PackedCriteria.arrMins(fTuple);
            int totalChanges = PackedCriteria.changes(fTuple);
            // Le payload n'intervient pas dans la dominance des critères principaux.
            // On crée un tuple "hypothétique" pour la gare : (depTime, finalArrTime, totalChanges)
            // Note: On n'a pas besoin de créer le long empaqueté, juste les valeurs.
            return depStationFront.isDominated(depTime, finalArrTime, totalChanges);
        };

        // Pour que l'optimisation s'applique, TOUS les tuples de f doivent être dominés.
        // On ne peut pas utiliser fBuilder.build().stream().allMatch(...) car build() peut être coûteux.
        // Il faut une méthode sur le Builder ou itérer.
        // Supposons une méthode (hypothétique) fBuilder.allMatch(predicate)
        // return fBuilder.allMatch(isTupleDominated);

        // Solution alternative sans allMatch sur Builder : itérer et vérifier.
        // C'est plus complexe car il faut gérer l'itération sur le builder.
        // Pour simplifier l'exemple, on va construire le front 'f' ici,
        // même si ce n'est pas optimal pour la performance.
        ParetoFront fFront = fBuilder.build();
        if (fFront.isEmpty()) {
            return false; // Un front vide n'est pas dominé (ou l'optimisation 1 s'est appliquée)
        }
        for (long fTuple : fFront) { // Itère sur les long empaquetés du front f
            if (!isTupleDominated.test(fTuple)) {
                return false; // On a trouvé un tuple dans f qui N'EST PAS dominé
            }
        }
        // Si on arrive ici, tous les tuples de f sont dominés
        return true;
    }
}