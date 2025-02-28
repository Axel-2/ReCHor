package ch.epfl.rechor.timetable;

import ch.epfl.rechor.PackedRange;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyTransfersTest {


    // Fausse impl de Transfers pour les tests.
    private final Transfers transfers = new Transfers() {
        private final int[] depStationIds = {0, 1, 0, 1, 2};
        private final int[] arrStationIds = {0, 0, 1, 2, 2};
        private final int[] durations     = {5, 4, 7, 3, 10};

        // La taille correspond au nombre total de transferts.
        @Override
        public int size() {
            return durations.length;
        }

        @Override
        public int depStationId(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Indice de transfert invalide: " + id);
            return depStationIds[id];
        }

        @Override
        public int minutes(int id) {
            if (id < 0 || id >= size())
                throw new IndexOutOfBoundsException("Indice de transfert invalide: " + id);
            return durations[id];
        }

        @Override
        public int arrivingAt(int stationId) {

            // On définit les gares valides comme 0, 1 et 2.
            if (stationId < 0 || stationId >= 3)
                throw new IndexOutOfBoundsException("Identifiant de gare invalide: " + stationId);
            if (stationId == 0)
                return PackedRange.pack(0, 2);
            else if (stationId == 1)
                return PackedRange.pack(2, 3);
            else // stationId == 2
                return PackedRange.pack(3, 5);
        }

        @Override
        public int minutesBetween(int depStationId, int arrStationId) {
            // Vérification des identifiants de gare (valables : 0, 1 et 2).
            if (depStationId < 0 || depStationId >= 3)
                throw new IndexOutOfBoundsException("Identifiant de gare de départ invalide: " + depStationId);
            if (arrStationId < 0 || arrStationId >= 3)
                throw new IndexOutOfBoundsException("Identifiant de gare d'arrivée invalide: " + arrStationId);
            // Recherche du transfert correspondant.
            for (int i = 0; i < size(); i++) {
                if (depStationIds[i] == depStationId && arrStationIds[i] == arrStationId)
                    return durations[i];
            }
            throw new NoSuchElementException("Aucun transfert entre gare " + depStationId + " et gare " + arrStationId);
        }
    };

    @Test
    void depStationId() {
        // Tests pour les indices valides
        assertEquals(0, transfers.depStationId(0)); // transfert 0
        assertEquals(1, transfers.depStationId(1)); // transfert 1
        assertEquals(0, transfers.depStationId(2)); // transfert 2
        assertEquals(1, transfers.depStationId(3)); // transfert 3
        assertEquals(2, transfers.depStationId(4)); // transfert 4

        // Tests pour indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(5));
    }

    @Test
    void minutes() {
        // Tests pour les indices valides
        assertEquals(5, transfers.minutes(0));
        assertEquals(4, transfers.minutes(1));
        assertEquals(7, transfers.minutes(2));
        assertEquals(3, transfers.minutes(3));
        assertEquals(10, transfers.minutes(4));

        // Tests pour indices invalides
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(5));
    }

    @Test
    void arrivingAt() {
        // Pour la gare 0, on attend un intervalle de [0,2) → PackedRange.pack(0,2)
        int expected0 = PackedRange.pack(0, 2);
        assertEquals(expected0, transfers.arrivingAt(0));

        // Pour la gare 1, on attend un intervalle de [2,3) → PackedRange.pack(2,3)
        int expected1 = PackedRange.pack(2, 3);
        assertEquals(expected1, transfers.arrivingAt(1));

        // Pour la gare 2, on attend un intervalle de [3,5) → PackedRange.pack(3,5)
        int expected2 = PackedRange.pack(3, 5);
        assertEquals(expected2, transfers.arrivingAt(2));

        // Tests pour identifiants de gare invalides
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(3));
    }

    @Test
    void minutesBetween() {
        // Cas valides (on sait qu'il existe exactement un transfert pour chaque couple présent dans notre jeu de données) :
        // Transfert 0 : de 0 à 0, durée 5 minutes.
        assertEquals(5, transfers.minutesBetween(0, 0));
        // Transfert 1 : de 1 à 0, durée 4 minutes.
        assertEquals(4, transfers.minutesBetween(1, 0));
        // Transfert 2 : de 0 à 1, durée 7 minutes.
        assertEquals(7, transfers.minutesBetween(0, 1));
        // Transfert 3 : de 1 à 2, durée 3 minutes.
        assertEquals(3, transfers.minutesBetween(1, 2));
        // Transfert 4 : de 2 à 2, durée 10 minutes.
        assertEquals(10, transfers.minutesBetween(2, 2));

        // Cas où aucun transfert n'existe, par exemple de 0 à 2.
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(0, 2));

        // Vérification que des identifiants de gare invalides lèvent une IndexOutOfBoundsException.
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(0, 3));
    }
}