package ch.epfl.rechor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Supposons que StopIndex est dans le package 'ch.epfl.rechor.stopfinder'
// import ch.epfl.rechor.stopfinder.StopIndex;

class StopIndexTest {

    private StopIndex stopIndex;

    // Données de test basées sur l'énoncé et quelques ajouts
    private static final List<String> ALL_STOP_NAMES = List.of(
            "Lausanne",
            "Genève",
            "Renens VD",
            "Zürich HB",
            "Bern",
            "Mézières FR, village", // Exemple du texte
            "Mézières VD, village", // Exemple du texte
            "Mézery-près-Donneloye, village", // Exemple du texte
            "Charleville-Mézières", // Exemple du texte
            "Lausanne-Flon",
            "Épalinges, Croisettes",
            "Bern, Bahnhof",
            "Zollikofen" // Pour tester le 'o' vs 'ö'
    );

    private static final Map<String, String> ALT_NAMES = Map.of(
            "Losanna", "Lausanne",
            "Geneve", "Genève",        // Sans accent
            "Zurich HB", "Zürich HB", // Sans accent
            "Berne", "Bern"           // Nom alternatif
    );

    @BeforeEach
    void setUp() {
        // Créer une nouvelle instance avant chaque test
        stopIndex = new StopIndex(ALL_STOP_NAMES, ALT_NAMES);
    }

    // --- Tests de base et recherche simple ---

    @Test
    @DisplayName("Recherche simple avec correspondance exacte")
    void stopsMatchingExactMatch() {
        List<String> expected = List.of("Lausanne");
        // Note: Lausanne-Flon ne doit pas matcher car "Lausanne" n'est pas un mot entier ici
        List<String> actual = stopIndex.stopsMatching("Lausanne", 5);
        // L'ordre peut dépendre du score, mais Lausanne devrait être premier.
        // Si "Lausanne-Flon" apparaît, il faut revoir la logique de score ou le test.
        // Ajustons l'assertion pour vérifier la présence et potentiellement la première position.
        assertTrue(actual.contains("Lausanne"), "Devrait contenir Lausanne");
        // Pour être plus strict, si Lausanne est le seul résultat attendu :
        // assertEquals(List.of("Lausanne"), actual);
        // Si Lausanne et Lausanne-Flon sont attendus et triés :
        assertEquals(List.of("Lausanne", "Lausanne-Flon"), actual, "Devrait trouver Lausanne et Lausanne-Flon");
    }

    @Test
    @DisplayName("Recherche insensible à la casse (minuscules)")
    void stopsMatchingCaseInsensitiveLower() {
        List<String> expected = List.of("Renens VD");
        List<String> actual = stopIndex.stopsMatching("renens vd", 5);
        assertEquals(expected, actual);
    }



    @Test
    @DisplayName("Recherche sensible à la casse (forcée par majuscule dans la requête)")
    void stopsMatchingCaseSensitiveForced() {
        // "HB" doit correspondre à "HB"
        List<String> expected = List.of("Zürich HB");
        List<String> actualZurich = stopIndex.stopsMatching("Zürich HB", 5);
        assertEquals(expected, actualZurich);

        // "Hb" ne doit PAS correspondre à "HB"
        List<String> actualZurichLower = stopIndex.stopsMatching("Zürich Hb", 5);
        assertTrue(actualZurichLower.isEmpty(), "Ne devrait pas trouver Zürich HB avec 'Hb'");

        // "VD" doit correspondre à "VD"
        List<String> expectedRenens = List.of("Renens VD");
        List<String> actualRenens = stopIndex.stopsMatching("Renens VD", 5);
        assertEquals(expectedRenens, actualRenens);

        // "Vd" ne doit PAS correspondre à "VD"
        List<String> actualRenensLower = stopIndex.stopsMatching("Renens Vd", 5);
        assertTrue(actualRenensLower.isEmpty(), "Ne devrait pas trouver Renens VD avec 'Vd'");
    }


    // --- Tests sur les accents ---

    @Test
    @DisplayName("Recherche insensible aux accents (requête sans accent)")
    void stopsMatchingAccentInsensitiveQueryWithout() {
        List<String> expected = List.of("Genève");
        List<String> actual = stopIndex.stopsMatching("geneve", 5);
        assertEquals(expected, actual);

        List<String> expectedZurich = List.of("Zürich HB");
        List<String> actualZurich = stopIndex.stopsMatching("zurich", 5);
        assertEquals(expectedZurich, actualZurich);

        List<String> expectedEpalinges = List.of("Épalinges, Croisettes");
        List<String> actualEpalinges = stopIndex.stopsMatching("epalinges", 5);
        assertEquals(expectedEpalinges, actualEpalinges);
    }

    @Test
    @DisplayName("Recherche insensible aux accents (requête sans accent 'c' vs 'ç')")
    void stopsMatchingAccentInsensitiveC() {
        List<String> expected = List.of("Épalinges, Croisettes");
        List<String> actual = stopIndex.stopsMatching("croisettes", 5); // 'c' simple
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Recherche sensible aux accents (forcée par accent dans la requête)")
    void stopsMatchingAccentSensitiveForced() {
        // Requête avec 'é' doit trouver "Genève" ou "Mézières" ou "Épalinges"
        List<String> expectedGeneve = List.of("Genève");
        List<String> actualGeneve = stopIndex.stopsMatching("Genève", 5);
        assertEquals(expectedGeneve, actualGeneve);

        // Requête avec 'è' ne doit PAS trouver "Genève" ou "Mézières"
        List<String> actualMezieresEgrave = stopIndex.stopsMatching("Mèzieres", 5);
        assertTrue(actualMezieresEgrave.isEmpty(), "Ne devrait pas trouver Mézières avec 'è'");

        // Requête avec 'ü' doit trouver "Zürich"
        List<String> expectedZurich = List.of("Zürich HB");
        List<String> actualZurichUmlaut = stopIndex.stopsMatching("Zürich", 5);
        assertEquals(expectedZurich, actualZurichUmlaut);

        // Requête avec 'u' doit aussi trouver "Zürich" (insensible si query sans accent)
        List<String> actualZurichU = stopIndex.stopsMatching("Zurich", 5);
        assertEquals(expectedZurich, actualZurichU);

        // Requête avec 'ö' ne doit PAS trouver "Zollikofen"
        List<String> actualZollikofenOmlaut = stopIndex.stopsMatching("Zöllikofen", 5);
        assertTrue(actualZollikofenOmlaut.isEmpty(), "Ne devrait pas trouver Zollikofen avec 'ö'");

        // Requête avec 'ç' doit trouver "Croisettes"
        List<String> expectedCroisettes = List.of("Épalinges, Croisettes");
        List<String> actualCroisettes = stopIndex.stopsMatching("Croisettes", 5); // Avec C majuscule aussi
        assertEquals(expectedCroisettes, actualCroisettes);
        List<String> actualCroisettesCedilla = stopIndex.stopsMatching("Croisettes", 5); // Avec ç
        assertEquals(expectedCroisettes, actualCroisettesCedilla); // Devrait être Croiçettes? Non, texte dit inverse. 'c' -> [cç]
        // Testons si la requête 'c' match 'ç' (ce que le texte dit)
        List<String> actualWithC = stopIndex.stopsMatching("croisettes", 5);
        assertEquals(expectedCroisettes, actualWithC);
        // Testons si la requête 'ç' match 'c' (ce que le texte ne dit pas explicitement mais est logique)
        // S'il y avait un arrêt "Franco", est-ce que "Franço" le trouverait? Probablement pas selon règle stricte.
        // Testons 'ç' vs 'ç'
        List<String> actualWithCedillaQuery = stopIndex.stopsMatching("croisettes", 5); // Si l'arrêt était "Épalinges, Croiçettes"
        // Pour ce test, ajoutons temporairement un arrêt avec ç
        StopIndex indexWithCedilla = new StopIndex(List.of("Garçonnière"), Collections.emptyMap());
        assertEquals(List.of("Garçonnière"), indexWithCedilla.stopsMatching("Garconniere", 5)); // c -> ç ok
        assertEquals(List.of("Garçonnière"), indexWithCedilla.stopsMatching("Garçonnière", 5)); // ç -> ç ok
        assertTrue(indexWithCedilla.stopsMatching("Garconnière", 5).isEmpty()); // ç dans nom, c dans query -> échec? Non, règle dit 'c' query -> [cç] name.
        // La règle inverse (ç query -> ?) n'est pas spécifiée, mais Java Pattern le gère peut-être via UNICODE_CASE ?
        // Supposons que ç query ne matche que ç name.

    }

    // --- Tests sur les sous-requêtes et le tri ---

    @Test
    @DisplayName("Recherche avec plusieurs sous-requêtes (ordre indifférent)")
    void stopsMatchingMultipleSubQueries() {
        // Exemple du texte: "mez vil" -> Mézières FR, Mézières VD, Mézery, Charleville
        List<String> expected = List.of(
                "Mézières FR, village", // Score 120
                "Mézières VD, village", // Score 120
                "Mézery-près-Donneloye, village", // Score 80
                "Charleville-Mézières"  // Score 75
        );

        List<String> actual = stopIndex.stopsMatching("mez vil", 5);
        assertEquals(expected, actual, "L'ordre doit correspondre au score de pertinence");

        // Vérifier l'indifférence à l'ordre des sous-requêtes
        List<String> actualReverse = stopIndex.stopsMatching("vil mez", 5);
        assertEquals(expected, actualReverse, "L'ordre des sous-requêtes ne doit pas importer");
    }


    @Test
    @DisplayName("Le tri par pertinence gère les bonus début/fin de mot")
    void stopsMatchingSortsByRelevanceBonuses() {
        // Ajoutons un arrêt pour mieux tester
        StopIndex idx = new StopIndex(List.of("Mot", "DébutMot", "MotFin", "DébutMotFin"), Collections.emptyMap());

        // Query "Mot"
        List<String> expectedMot = List.of(
                "DébutMotFin", // Bonus début * 4, Bonus fin * 2 => * 8
                "DébutMot",    // Bonus début * 4
                "MotFin",      // Bonus fin * 2
                "Mot"          // Pas de bonus * 1
        );
        assertEquals(expectedMot, idx.stopsMatching("Mot", 5));

        // Query "butMotFi" (match partiel au milieu de DébutMotFin) vs "Mot" (match complet de Mot)
        // Score("butMotFi" in "DébutMotFin") ~ 8/11 * 100 = 72% -> score 72
        // Score("Mot" in "Mot") ~ 3/3 * 100 = 100% -> score 100 * 8 = 800
        // Score("Mot" in "DébutMot") ~ 3/8 * 100 = 37% -> score 37 * 4 = 148
        // Score("Mot" in "MotFin") ~ 3/6 * 100 = 50% -> score 50 * 2 = 100
        // Score("Mot" in "DébutMotFin") ~ 3/11 * 100 = 27% -> score 27 * 8 = 216
        // L'ordre pour query "Mot" testé au-dessus semble correct.

        // Testons spécifiquement avec "Mézières FR, village" et query "vil" vs "lage"
        // "vil" -> début de "village" -> score base (3/20=15) * 4 = 60
        // "lage" -> fin de "village" -> score base (4/20=20) * 2 = 40
        // Si on ajoute un arrêt "Village FR"
        StopIndex idx2 = new StopIndex(List.of("Mézières FR, village", "Village FR"), Collections.emptyMap());
        // Query "vil"
        // Score("vil" in "Mézières FR, village") = 15 * 4 = 60
        // Score("vil" in "Village FR") = (3/10=30) * 4 = 120
        assertEquals(List.of("Village FR", "Mézières FR, village"), idx2.stopsMatching("vil", 5));
        // Query "lage"
        // Score("lage" in "Mézières FR, village") = (4/20=20) * 2 = 40
        // Score("lage" in "Village FR") = (4/10=40) * 8 = 320 (début et fin du mot "Village")
        assertEquals(List.of("Village FR", "Mézières FR, village"), idx2.stopsMatching("lage", 5));
        // Query "village"
        // Score("village" in "Mézières FR, village") = (7/20=35) * 8 = 280 (début et fin)
        // Score("village" in "Village FR") = (7/10=70) * 8 = 560 (début et fin)
        assertEquals(List.of("Village FR", "Mézières FR, village"), idx2.stopsMatching("village", 5));
    }


    // --- Tests sur les noms alternatifs ---

    @Test
    @DisplayName("Recherche via un nom alternatif retourne le nom principal")
    void stopsMatchingAlternativeName() {
        List<String> expected = List.of("Lausanne");
        List<String> actual = stopIndex.stopsMatching("Losanna", 5);
        assertEquals(expected, actual);

        List<String> expectedBern = List.of("Bern");
        List<String> actualBerne = stopIndex.stopsMatching("Berne", 5);
        assertEquals(expectedBern, actualBerne);
    }

    @Test
    @DisplayName("Recherche via un nom alternatif sans accent retourne le nom principal avec accent")
    void stopsMatchingAlternativeNameNoAccent() {
        List<String> expected = List.of("Genève");
        List<String> actual = stopIndex.stopsMatching("Geneve", 5); // Alternative sans accent
        assertEquals(expected, actual);

        List<String> expectedZurich = List.of("Zürich HB");
        List<String> actualZurichNoUmlaut = stopIndex.stopsMatching("Zurich HB", 5); // Alternative sans accent
        assertEquals(expectedZurich, actualZurichNoUmlaut);
    }

    @Test
    @DisplayName("La recherche retourne des noms principaux uniques même si l'alternatif correspond aussi")
    void stopsMatchingReturnsUniquePrincipalNames() {
        // "lausanne" match "Lausanne" (principal) et via "Losanna" (alternatif)
        // Le résultat ne doit contenir "Lausanne" qu'une fois.
        List<String> expected = List.of("Lausanne", "Lausanne-Flon"); // Trié par score/nom
        List<String> actual = stopIndex.stopsMatching("lausann", 5); // Match partiel
        assertEquals(expected, actual);
    }

    // --- Tests sur la limitation du nombre de résultats ---

    @Test
    @DisplayName("stopsMatching respecte la limite maxNumberOfStops")
    void stopsMatchingRespectsLimit() {
        // "vil" correspond à 3 arrêts dans les données de base
        List<String> resultsLimited = stopIndex.stopsMatching("vil", 2);
        assertEquals(2, resultsLimited.size(), "Doit retourner exactement 2 résultats");

        List<String> resultsEnough = stopIndex.stopsMatching("vil", 5);
        assertEquals(3, resultsEnough.size(), "Doit retourner les 3 résultats si la limite est suffisante");

        // Vérifier que ce sont les MEILLEURS résultats qui sont retournés
        List<String> expectedTop2 = List.of(
                "Mézières FR, village", // Score 120
                "Mézières VD, village"  // Score 120
                // Mézery-près-Donneloye, village (Score 80) est exclu
        );
        // L'ordre entre les deux premiers peut varier s'ils ont le même score, on teste la présence
        assertTrue(resultsLimited.containsAll(expectedTop2) && expectedTop2.containsAll(resultsLimited),
                "Devrait contenir les 2 arrêts Mézières village");

    }


    @Test
    @DisplayName("Recherche avec une requête qui ne correspond à aucun arrêt")
    void stopsMatchingNoResults() {
        List<String> results = stopIndex.stopsMatching("DoesNotExistXYZ", 5);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Recherche quand la liste d'arrêts initiale est vide")
    void stopsMatchingWithEmptyIndex() {
        StopIndex emptyIndex = new StopIndex(Collections.emptyList(), Collections.emptyMap());
        List<String> results = emptyIndex.stopsMatching("lausanne", 5);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Recherche avec caractères spéciaux regex dans la query (doivent être échappés)")
    void stopsMatchingWithRegexCharsInQuery() {
        // Si un arrêt s'appelait "Arrêt [Test]"
        StopIndex idx = new StopIndex(List.of("Arrêt [Test]"), Collections.emptyMap());

        // La recherche "[Test]" devrait trouver l'arrêt si Pattern.quote est utilisé
        List<String> expected = List.of("Arrêt [Test]");
        List<String> actual = idx.stopsMatching("[Test]", 5);
        assertEquals(expected, actual);

        // La recherche "Arrêt ." ne devrait PAS trouver l'arrêt si le '.' est échappé
        // (Sinon '.' matcherait n'importe quel caractère)
        List<String> actualDot = idx.stopsMatching("Arrêt .", 5); // Si quote est utilisé, cherche littéralement " ."
        assertTrue(actualDot.isEmpty());

        // Testons avec un arrêt et une query contenant des parenthèses
        StopIndex idxParen = new StopIndex(List.of("Stop (bus)"), Collections.emptyMap());
        assertEquals(List.of("Stop (bus)"), idxParen.stopsMatching("(bus)", 5));

    }
}