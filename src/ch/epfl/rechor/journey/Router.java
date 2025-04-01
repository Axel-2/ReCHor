package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.time.LocalDate;

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
        // 1) On parcourt la totalité des liaisons de l'horaire, dans l'ordre décroissant
        for(int i = timetable.connectionsFor(date).size() - 1; i >= 0; i--) {


        }

        // Va return le profil de tous les voyages optimaux permettant de se rendre
        // à arrStationId (commentaire à suppr mais pour l'instant je laisse
        return null;
    }
}
