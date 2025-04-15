package ch.epfl.rechor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList; // Import pour trier
import java.util.Collections; // Import pour trier
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MyStopIndexTest {

    private StopIndex stopIndex;

    // Données de test simples
    private final List<String> sampleStops = List.of(
            "Lausanne",
            "Renens VD",
            "Mézières FR, village", // Score 120 pour "mez vil"
            "Mézières VD, village", // Score 120 pour "mez vil"
            "Mézery-près-Donneloye, village", // Score 80 pour "mez vil"
            "Charleville-Mézières", // Score 75 pour "mez vil"
            "Genève",
            "BERN" // Pour test de casse
    );
    private final Map<String, String> sampleSynonyms = Map.of(
            "Losanna", "Lausanne",
            "Renens", "Renens VD",
            "Mezieres FR", "Mézières FR, village" // Synonyme partiel
    );

    @BeforeEach
    void setUp() {
        // Crée une nouvelle instance avant chaque test
        stopIndex = new StopIndex(sampleStops, sampleSynonyms);
    }

    // --- Tests de Filtrage ---

    @Test
    void stopsMatchingFindsExactMatch() {
        List<String> expected = List.of("Mézières FR, village" , "Mézières VD, village", "Mézery-près-Donneloye, village"
                , "Charleville-Mézières");
        List<String> actual = stopIndex.stopsMatching("mez vil", 5);
        assertArrayEquals(expected.toArray(), actual.toArray()); // Vérifie contenu et ordre
    }

    @Test
    void stopsMatchingIsCaseInsensitive() {
        List<String> expectedLausanne = List.of("Lausanne");
        List<String> actualLausanne = stopIndex.stopsMatching("lausanne", 5);
        assertEquals(expectedLausanne, actualLausanne);

        List<String> expectedBern = List.of("BERN");
        List<String> actualBern = stopIndex.stopsMatching("bern", 5);
        assertEquals(expectedBern, actualBern);
    }

    @Test
    void stopsMatchingIsAccentInsensitive() {
        List<String> expected = List.of(
                "Mézières FR, village",
                "Mézières VD, village",
                "Charleville-Mézières"
        );
        List<String> actual = stopIndex.stopsMatching("Mezieres", 5);

        // Pour vérifier l'égalité sans tenir compte de l'ordre : trier les deux listes
        assertEquals(expected, actual, "Accent matching failed or elements differ");
    }

    @Test
    void stopsMatchingWithMultipleSubQueries() {
        List<String> expected = List.of(
                "Mézières FR, village"
        );
        List<String> actual = stopIndex.stopsMatching("mez age", 1);

        // Trier pour comparer indépendamment de l'ordre
        assertEquals(expected, actual, "Multi-subquery match failed or elements differ");
    }

    @Test
    void stopsMatchingFindsViaSynonym() {
        List<String> expected = List.of("Lausanne");
        List<String> actual = stopIndex.stopsMatching("Losanna", 5);
        // WARNING: La logique de filtrage des synonymes (anyMatch vs allMatch) dans StopIndex peut être revue.
        assertEquals(expected, actual);
    }

    @Test
    void stopsMatchingFindsViaPartialSynonym() {
        List<String> expected = List.of("Mézières FR, village");
        List<String> actual = stopIndex.stopsMatching("Mezieres FR", 5);
        // WARNING: La logique de filtrage des synonymes (anyMatch vs allMatch) dans StopIndex peut être revue.
        assertEquals(expected, actual);
    }


    @Test
    void stopsMatchingReturnsEmptyWhenNoMatch() {
        List<String> actual = stopIndex.stopsMatching("DoesNotExistXYZ", 5);
        assertTrue(actual.isEmpty(), "Expected empty list for no match");
    }

    @Test
    void stopsMatchingHandlesEmptyQuery() {
        List<String> actual = stopIndex.stopsMatching("", 5);
        // Comportement actuel (split(" ") donne [""]) devrait matcher tous les arrêts
        // car le Pattern pour "" matchera partout. À confirmer si c'est le comportement désiré.
        assertNotNull(actual);
        // Par exemple, vérifier que tous les arrêts sont retournés si query="" (ou aucun si on filtre le empty string)
        // assertEquals(sampleStops.size(), actual.size(), "Empty query might return all stops depending on implementation");
        // Pour l'instant, on vérifie juste qu'il ne crashe pas.
    }

}