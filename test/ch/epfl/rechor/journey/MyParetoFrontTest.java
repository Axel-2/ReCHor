package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyParetoFrontTest {

    // ==========================================================================
    // SECTION 1 : Tests liés au constructeur par défaut, à clear() et addAll()
    // ==========================================================================

    @Test
    void builder_defaultConstructor_createsEmptyBuilder() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty());
    }

    @Test
    void builder_clear_makesBuilderEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.clear();
        assertTrue(builder.isEmpty());
    }

    @Test
    void testAddAllMethod() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        // deux critères qui ne se dominent pas
        long crit1 = PackedCriteria.pack(450, 3, 0);
        long crit2 = PackedCriteria.pack(470, 2, 0);

        builder1.add(crit1);
        builder2.add(crit2);

        // Ajoute tous les éléments de builder2 dans builder1
        builder1.addAll(builder2);

        ParetoFront front = builder1.build();
        assertEquals(2, front.size(), "Après addAll, la frontière doit contenir la réunion des tuples non dominés");
    }


    // ==========================================================================
    // SECTION 2 : Tests sur la taille (size) de la frontière
    // ==========================================================================

    @Test
    void testEmptyFront() {
        // 1. Vérifie que ParetoFront.EMPTY est vide
        assertEquals(0, ParetoFront.EMPTY.size(), "ParetoFront.EMPTY doit avoir une taille de 0");
    }

    @Test
    void testSingleElement() {
        // Ajout d'un seul élément
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long crit = PackedCriteria.pack(450, 3, 0);
        builder.add(crit);
        ParetoFront front = builder.build();
        assertEquals(1, front.size(), "Un seul élément ajouté doit donner une taille de 1");
    }

    @Test
    void testDuplicatesNotCounted() {
        // Les doublons ne doivent pas être comptabilisés plusieurs fois
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long crit = PackedCriteria.pack(450, 3, 0);
        builder.add(crit)
                .add(crit)  // doublon
                .add(crit); // doublon
        ParetoFront front = builder.build();
        assertEquals(1, front.size(), "Les doublons ne doivent être comptés qu'une seule fois");
    }

    @Test
    void testNonDominatedElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();

        // Deux critères qui ne se dominent pas l'un l'autre
        int arrMins1 = 450, arrMins2 = 470;
        int changes1 = 3, changes2 = 2;
        int payload = 0;
        long crit1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long crit2 = PackedCriteria.pack(arrMins2, changes2, payload);

        builder.add(crit1).add(crit2);
        ParetoFront front = builder.build();
        assertEquals(2, front.size(), "Deux critères non dominés doivent être conservés");
    }

    @Test
    void testDominatedElementNotAdded() {
        ParetoFront.Builder builder = new ParetoFront.Builder();

        // Critère de base
        int arrMins1 = 450, changes1 = 3;
        long crit1 = PackedCriteria.pack(arrMins1, changes1, 0);

        // Un autre critère non dominant par rapport à crit1
        int arrMins2 = 470, changes2 = 2;
        long crit2 = PackedCriteria.pack(arrMins2, changes2, 0);

        // Un critère clairement dominé (heure d'arrivée plus tard et plus de changements)
        int arrMinsDom = 500, changesDom = 8;
        long critDominated = PackedCriteria.pack(arrMinsDom, changesDom, 0);

        builder.add(crit1)
                .add(crit2)
                .add(critDominated);

        ParetoFront front = builder.build();
        assertEquals(2, front.size(), "Le critère dominé ne doit pas être ajouté à la frontière de Pareto");
    }

    @Test
    void testOrderIndependence() {
        // La taille de la frontière doit être identique quelle que soit l'ordre d'ajout
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        long crit1 = PackedCriteria.pack(450, 3, 0);
        long crit2 = PackedCriteria.pack(470, 2, 0);
        long critDominated = PackedCriteria.pack(500, 8, 0);

        // Ajout dans l'ordre "naturel"
        builder1.add(crit1).add(crit2).add(critDominated);
        // Ajout dans l'ordre inversé
        builder2.add(critDominated).add(crit2).add(crit1);

        ParetoFront front1 = builder1.build();
        ParetoFront front2 = builder2.build();

        assertEquals(front1.size(), front2.size(), "L'ordre d'ajout ne doit pas affecter la taille finale de la frontière");
    }

    @Test
    void size() {
        ParetoFront.Builder paretoBuilder = new ParetoFront.Builder();

        int arrMins = 450, arrMins2 = 470, arrMins3 = 500;
        int changes = 3, changes2 = 2, changes3 = 8;
        int payload = 0;

        long baseExampleCriteria = PackedCriteria.pack(arrMins, changes, payload);
        long equalCriteria = PackedCriteria.pack(arrMins2, changes2, payload);
        long dominatedCriteria = PackedCriteria.pack(arrMins3, changes3, payload);

        paretoBuilder
                .add(baseExampleCriteria)
                // ajoute à double donc ne doit pas être compté
                .add(baseExampleCriteria)
                // on ajoute un critère qui n'est pas dominé par l'autre (égal)
                .add(equalCriteria)
                // on ajoute un critère dominé que ne doit pas être compté
                .add(dominatedCriteria);

        ParetoFront paretoFront = paretoBuilder.build();

        // on doit donc normalement en avoir 2
        int expectedSize = 2;
        int currentSize = paretoFront.size();

        assertEquals(expectedSize, currentSize);
    }

    void testAddM() {

    }


    // ==========================================================================
    // SECTION 3 : Tests de la fonctionnalité get() et de forEach()
    // ==========================================================================

    @Test
    void testGetFunctionality() {
        // Ajout de plusieurs tuples et vérification que get() renvoie le bon tuple.
        // les 3 ne doivent pas se dominer entre eux
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t1 = PackedCriteria.pack(450, 3, 100); // tuple : arrivée 450, 3 changements, payload 100
        long t2 = PackedCriteria.pack(470, 2, 200); // tuple : arrivée 470, 2 changements, payload 200
        long t3 = PackedCriteria.pack(480, 1, 300); // tuple : arrivée 480, 1 changement, payload 300

        builder.add(t1).add(t2).add(t3);
        ParetoFront front = builder.build();

        // Vérification que chaque tuple peut être retrouvé via get(arrMins, changes)
        assertEquals(t1, front.get(450, 3), "Le tuple (450, 3) doit être présent et correspondre à t1");
        assertEquals(t2, front.get(470, 2), "Le tuple (470, 2) doit être présent et correspondre à t2");
        assertEquals(t3, front.get(480, 1), "Le tuple (480, 1) doit être présent et correspondre à t3");

        // Vérification qu'un tuple non ajouté lève une exception
        assertThrows(NoSuchElementException.class, () -> front.get(500, 5),
                "get() doit lancer une exception pour un tuple inexistant");
    }

    @Test
    void testForEachIteration() {
        // Vérifie que forEach parcourt bien tous les éléments de la frontière
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long crit1 = PackedCriteria.pack(450, 3, 0);
        long crit2 = PackedCriteria.pack(470, 2, 0);
        builder.add(crit1).add(crit2);
        ParetoFront front = builder.build();

        final int[] count = {0};
        front.forEach(value -> count[0]++);
        assertEquals(front.size(), count[0], "La méthode forEach doit parcourir exactement size() éléments");
    }

    @Test
    void forEachEmpty() {
        // ForEach de EMPTY donne une liste vide
        List<Long> collected = new ArrayList<>();
        ParetoFront.EMPTY.forEach(value -> collected.add(value));
        assertTrue(collected.isEmpty());
    }

    @Test
    void testForEachFunctionality() {
        // Ajout de tuples dans un ordre aléatoire.
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t1 = PackedCriteria.pack(480, 1, 300); // plus "élevé"
        long t2 = PackedCriteria.pack(450, 3, 100); // plus "faible"
        long t3 = PackedCriteria.pack(470, 2, 200); // intermédiaire

        builder.add(t1).add(t2).add(t3);
        ParetoFront front = builder.build();

        // Collecte des tuples via forEach.
        List<Long> collected = new ArrayList<>();
        front.forEach(collected::add);

        // L'ordre d'itération doit être lexicographique, c'est-à-dire équivalent à l'ordre naturel
        // des valeurs empaquetées. Ici, on s'attend à obtenir t2, t3, puis t1 (si t2 < t3 < t1).
        List<Long> expected = new ArrayList<>();
        expected.add(t2);
        expected.add(t3);
        expected.add(t1);
        assertEquals(expected, collected, "La méthode forEach doit itérer les tuples en ordre lexicographique");
    }

    @Test
    void testGetMethodThrowsExceptionForMissingTuple() {
        // Vérifie que get(...) lance une exception quand le tuple demandé n'existe pas
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long crit1 = PackedCriteria.pack(450, 3, 0);
        builder.add(crit1);
        ParetoFront front = builder.build();
        // On suppose ici que (470,2) n'est pas présent dans la frontière
        assertThrows(NoSuchElementException.class, () -> front.get(470, 2),
                "get(arrMins, changes) doit lancer NoSuchElementException si le tuple n'existe pas");
    }


    // ==========================================================================
    // SECTION 4 : Tests concernant l'ajout de critères dominants ou dominés
    // ==========================================================================

    @Test
    void testAddBetterCriterionRemovesWorse() {
        // Ajout d'un critère initial, puis d'un meilleur critère qui le domine.
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long tWorse = PackedCriteria.pack(460, 4, 0);
        long tBetter = PackedCriteria.pack(450, 3, 0);

        builder.add(tWorse);
        ParetoFront front1 = builder.build();
        assertEquals(tWorse, front1.get(460, 4), "Avant ajout du meilleur, tWorse doit être présent.");

        builder.add(tBetter);
        ParetoFront front2 = builder.build();
        assertEquals(1, front2.size(), "Après ajout d'un meilleur critère, la frontière doit contenir uniquement le meilleur.");
        assertEquals(tBetter, front2.get(450, 3), "Le critère meilleur doit être accessible via get().");
        assertThrows(NoSuchElementException.class, () -> front2.get(460, 4),
                "Le critère dominé (tWorse) ne doit plus être accessible.");
    }

    @Test
    public void fullyDominateEmptyBuilder() {

        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        assertEquals(true, builder1.fullyDominates(builder2, 0));

    }


    @Test
    public void testFullyDominatesFalse() {
        // Créer un builder contenant des tuples dominants avec heures de départ
        ParetoFront.Builder dominantBuilder = new ParetoFront.Builder();
        long criteria1 = PackedCriteria.pack(10, 2, 100);
        criteria1 = PackedCriteria.withDepMins(criteria1, 4);
        dominantBuilder.add(criteria1);

        long criteria2 = PackedCriteria.pack(12, 1, 90);
        criteria2 = PackedCriteria.withDepMins(criteria2, 5);
        dominantBuilder.add(criteria2);

        // Créer un builder qui n'est pas totalement dominé
        ParetoFront.Builder nonDominatedBuilder = new ParetoFront.Builder();
        // Ce tuple ne devrait pas être dominé car son heure d'arrivée est très tôt
        nonDominatedBuilder.add(PackedCriteria.pack(8, 3, 80));

        // Vérifier que dominantBuilder ne domine pas totalement nonDominatedBuilder
        assertFalse(dominantBuilder.fullyDominates(nonDominatedBuilder, 5),
                "Le builder dominant ne devrait pas totalement dominer nonDominatedBuilder");
    }

    @Test
    public void testFullyDominatesWithSelf() {
        // Créer un builder contenant des tuples avec heures de départ
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long criteria1 = PackedCriteria.pack(10, 2, 100);
        criteria1 = PackedCriteria.withDepMins(criteria1, 4);
        builder.add(criteria1);

        long criteria2 = PackedCriteria.pack(12, 1, 90);
        criteria2 = PackedCriteria.withDepMins(criteria2, 5);
        builder.add(criteria2);

        // Un builder ne peut pas se dominer lui-même totalement
        assertFalse(builder.fullyDominates(builder, 10),
                "Un builder ne devrait pas totalement se dominer lui-même");
    }

    @Test
    void testDominanceBehavior() {
        // On teste que l'ajout d'un tuple dominé n'affecte pas la frontière
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t1 = PackedCriteria.pack(450, 3, 100); // bon critère
        long t2 = PackedCriteria.pack(470, 2, 200); // également bon et non dominant par rapport à t1
        // Ce tuple est clairement moins performant : arrivée plus tard et plus de changements.
        long tDominated = PackedCriteria.pack(500, 8, 300);

        builder.add(t1).add(t2).add(tDominated);
        ParetoFront front = builder.build();

        // Vérification que le tuple dominé n'est pas présent.
        assertThrows(NoSuchElementException.class, () -> front.get(500, 8),
                "Le tuple dominé ne doit pas être inclus dans la frontière");

        // Vérification que la frontière contient les deux tuples non dominés.
        List<Long> collected = new ArrayList<>();
        front.forEach(collected::add);
        // L'ordre attendu dépend de l'ordre lexicographique (supposons ici t1 < t2)
        List<Long> expected = new ArrayList<>();
        if (t1 < t2) {
            expected.add(t1);
            expected.add(t2);
        } else {
            expected.add(t2);
            expected.add(t1);
        }
        assertEquals(expected, collected, "La frontière doit contenir uniquement les tuples non dominés, dans l'ordre attendu");
    }

    @Test
    void testClearMethod() {
        // On ajoute un tuple, puis on vide le builder et on vérifie que la frontière résultante est vide.
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t = PackedCriteria.pack(450, 3, 100);
        builder.add(t);
        // Avant le clear, get() doit fonctionner.
        assertEquals(t, builder.build().get(450, 3));
        // On vide le builder.
        builder.clear();
        ParetoFront front = builder.build();
        // La frontière doit être vide : size() == 0 et get() lève une exception.
        assertEquals(0, front.size(), "Après clear, la frontière doit être vide");
        assertThrows(NoSuchElementException.class, () -> front.get(450, 3),
                "Après clear, aucun tuple ne doit être accessible");
    }

    @Test
    void testAddAllFunctionality() {
        // On crée deux builders, on y ajoute des tuples, puis on fusionne via addAll.
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        long t1 = PackedCriteria.pack(450, 3, 100);
        long t2 = PackedCriteria.pack(470, 2, 200);
        long t3 = PackedCriteria.pack(480, 1, 300);

        builder1.add(t1);
        builder2.add(t2).add(t3);

        builder1.addAll(builder2);
        ParetoFront front = builder1.build();

        // Vérifie que la frontière contient tous les tuples non dominés des deux builders.
        List<Long> collected = new ArrayList<>();
        front.forEach(collected::add);
        // Supposons ici que l'ordre lexicographique naturel des tuples est t1, t2, t3.
        List<Long> expected = new ArrayList<>();
        expected.add(t1);
        expected.add(t2);
        expected.add(t3);
        assertEquals(expected, collected, "Après addAll, la frontière doit contenir tous les tuples non dominés, dans l'ordre correct");
    }

    // --------------------------------------------------- TESTS SUPPL ------------------------------
    private ParetoFront.Builder createBuilderWithData() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        // We choose departure minutes so that they are sensible:
        // • Tuple 1: arrival=480, changes=2, payload=111, departure=400.
        // • Tuple 2: arrival=480, changes=3, payload=222, departure=410.
        // • Tuple 3: arrival=481, changes=1, payload=333, departure=395.
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(480, 2, 111), 400))
                .add(PackedCriteria.withDepMins(PackedCriteria.pack(480, 3, 222), 410))
                .add(PackedCriteria.withDepMins(PackedCriteria.pack(481, 1, 333), 395));
        return builder;
    }


    @Test
    void testEmptyConstant() {
        ParetoFront empty = ParetoFront.EMPTY;
        assertEquals(0, empty.size(), "EMPTY should have size 0");
        assertThrows(NoSuchElementException.class,
                () -> empty.get(480, 2),
                "Getting anything from EMPTY must throw NoSuchElementException");
    }

    @Test
    void testParetoFrontSizeAndGet() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        ParetoFront pf = builderWithData.build();
        // According to our dominance rule (later departure is better),
        // so the frontier should contain (480,3) and (481,1).
        long found1 = pf.get(480, 3);
        assertEquals(480, PackedCriteria.arrMins(found1));
        assertEquals(3,   PackedCriteria.changes(found1));
        assertEquals(222, PackedCriteria.payload(found1));

        long found2 = pf.get(481, 1);
        assertEquals(481, PackedCriteria.arrMins(found2));
        assertEquals(1,   PackedCriteria.changes(found2));
        assertEquals(333, PackedCriteria.payload(found2));

        // A non-existent tuple should throw.
        assertThrows(NoSuchElementException.class,
                () -> pf.get(999, 9));
    }


    @Test
    void testBuilderDefaultIsEmpty() {
        ParetoFront.Builder emptyBuilder = new ParetoFront.Builder();
        assertTrue(emptyBuilder.isEmpty());
        assertEquals(0, emptyBuilder.build().size());
    }

    @Test
    void testBuilderCopyConstructor() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        ParetoFront.Builder copy = new ParetoFront.Builder(builderWithData);
        int originalSize = builderWithData.build().size();

        // Add a new tuple that should definitely be added.
        // New tuple: arrival=482, changes=2, payload=555, departure=420.
        // This tuple is non-dominated with respect to the current frontier.
        builderWithData.add(PackedCriteria.withDepMins(PackedCriteria.pack(482, 2, 555), 420));

        int newSize = builderWithData.build().size();
        assertEquals(originalSize + 1, newSize,
                "The frontier should increase by one after adding a non-dominated tuple");

        // The copy should remain unchanged.
        assertEquals(originalSize, copy.build().size(),
                "Copy must remain unchanged when the original is modified");
    }

    @Test
    void testBuilderClear() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        assertFalse(builderWithData.isEmpty());
        builderWithData.clear();
        assertTrue(builderWithData.isEmpty());
        assertEquals(0, builderWithData.build().size(), "After clear, build() must produce an empty ParetoFront");
    }

    @Test
    void testBuilderAddDominatedTuple() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        // Add a tuple (482,5,777) with departure 410, which should be dominated.
        long dominated = PackedCriteria.withDepMins(PackedCriteria.pack(482, 5, 777), 410);
        builderWithData.add(dominated);
        ParetoFront pf = builderWithData.build();
        // The frontier size should remain unchanged.
        int sizeBefore = pf.size();
        assertEquals(sizeBefore, builderWithData.build().size(),
                "Adding a dominated tuple should not increase the frontier size");
        assertThrows(NoSuchElementException.class,
                () -> pf.get(480, 5),
                "Dominated tuple must not be retrievable from the frontier");
    }

    @Test
    void testBuilderAddNonDominatedTuple() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        // Add a strictly better tuple (479,1,666) with departure 390.
        long bestSoFar = PackedCriteria.withDepMins(PackedCriteria.pack(479, 1, 666), 390);
        builderWithData.add(bestSoFar);
        ParetoFront pf = builderWithData.build();
        assertDoesNotThrow(() -> pf.get(479, 1), "The newly added better tuple must be found in the frontier");
    }

    @Test
    void testBuilderAddAtFrontierBoundary() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        // Add a tuple that is equal to an existing tuple (480,2,111) with departure 400.
        long equalToExisting = PackedCriteria.withDepMins(PackedCriteria.pack(480, 2, 999), 400);
        builderWithData.add(equalToExisting);
        ParetoFront pf = builderWithData.build();
        // The frontier size should not increase.
        int sizeBefore = pf.size();
        assertEquals(sizeBefore, builderWithData.build().size(),
                "Adding an equal tuple does not increase size");
    }

    @Test
    void testBuilderAddAll() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        ParetoFront.Builder other = new ParetoFront.Builder();
        other.add(PackedCriteria.withDepMins(PackedCriteria.pack(480, 2, 111), 400))  // identical to one in builderWithData.
                .add(PackedCriteria.withDepMins(PackedCriteria.pack(1000, 10, 999), 410)); // within bounds; likely dominated.
        int oldSize = builderWithData.build().size();
        builderWithData.addAll(other);
        ParetoFront pf = builderWithData.build();
        // The final frontier should be at least as large as before.
        assertTrue(pf.size() >= oldSize, "Final frontier size must be at least the old size");
    }

    @Test
    void testFullyDominatesMethod() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        ParetoFront.Builder second = new ParetoFront.Builder();
        second.add(PackedCriteria.withDepMins(PackedCriteria.pack(500, 10, 999), 490))
                .add(PackedCriteria.withDepMins(PackedCriteria.pack(501, 9, 888), 480));
        // Test fullyDominates with departure=480.
        boolean doesDominate = builderWithData.fullyDominates(second, 480);
        assertFalse(doesDominate, "Expected builderWithData not to fully dominate the second frontier for depMins=480");
    }
    // 480, 2, 400              500, 10, 480
    // 480, 3, 410         vs
    // 481, 1, 395              501, 9, 480

    @Test
    void testBuilderBuildImmutability() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        ParetoFront built = builderWithData.build();
        int oldSize = built.size();
        builderWithData.add(PackedCriteria.withDepMins(PackedCriteria.pack(1000, 1, 7777), 480));
        // The previously built ParetoFront should remain unchanged.
        assertEquals(oldSize, built.size(), "Previously built ParetoFront must remain unchanged after modifying the builder");
    }

    @Test
    void testGetThrowsForNonExisting() {
        ParetoFront.Builder builderWithData = createBuilderWithData();
        ParetoFront pf = builderWithData.build();
        assertThrows(NoSuchElementException.class,
                () -> pf.get(9999, 99));
    }

    @Test
    void testExtremeValuesInEmptyBuilder() {
        // Create a fresh, empty builder.
        ParetoFront.Builder builder = new ParetoFront.Builder();

        // Extreme value A: maximum arrival (2879) and maximum changes (127).
        // Use departure = 480.
        long extremeHigh = PackedCriteria.withDepMins(PackedCriteria.pack(2879, 127, 123456), 480);

        // Extreme value B: minimum arrival (-240) and 0 changes.
        // Use the same departure so that neither dominates the other.
        long extremeLow = PackedCriteria.withDepMins(PackedCriteria.pack(-240, 0, 654321), -240);

        // Add both extreme values.
        builder.add(extremeHigh);
        builder.add(extremeLow);
        // Build the ParetoFront.
        ParetoFront pf = builder.build();

        // Now, verify that both extremes are present.
        // If the packing works correctly, pf.get(2879,127) should return extremeHigh,
        // and pf.get(-240,0) should return extremeLow.
//        System.out.println(PackedCriteria.arrMins(extremeHigh) + " " + PackedCriteria.changes(extremeHigh) + " " +PackedCriteria.depMins(extremeHigh));
//        System.out.println(PackedCriteria.arrMins(extremeLow) + " " + PackedCriteria.changes(extremeLow) + " " + PackedCriteria.depMins(extremeLow));
//        System.out.println(builder);


        long retHigh = pf.get(2879, 127);
        System.out.println(builder);
        long retLow = pf.get(-240, 0);

        assertEquals(2879, PackedCriteria.arrMins(retHigh), "Extreme high should have arrival 2879");
        assertEquals(127, PackedCriteria.changes(retHigh), "Extreme high should have 127 changes");
        assertEquals(654321, PackedCriteria.payload(retLow), "Extreme low should have payload 654321");
        assertEquals(-240, PackedCriteria.arrMins(retLow), "Extreme low should have arrival -240");
        assertEquals(0, PackedCriteria.changes(retLow), "Extreme low should have 0 changes");
    }

    @Test
    void testAddFunction_AddInMiddleOneRemoval(){
        ParetoFront.Builder builder = new ParetoFront.Builder();

        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(500, 10, 0), 490));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(501, 9, 0), 490));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(502, 9, 0), 490));

        ParetoFront paretoFront = builder.build();

        assertEquals(2, paretoFront.size());

    }

    @Test
    void testAddFunction_AddInMiddleAndRemovetwoDominated(){
        ParetoFront.Builder builder = new ParetoFront.Builder();

        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(700, 10, 0), 690));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(710, 9, 0), 700));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(720, 8, 0), 710)); // dominated
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(730, 7, 0), 710)); // dominated

        // dominates 3 and 4
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(720, 6, 0), 710));

        ParetoFront paretoFront = builder.build();

        assertThrows(NoSuchElementException.class, () -> paretoFront.get(720, 8)); // Has been deleted
        assertThrows(NoSuchElementException.class, () -> paretoFront.get(730, 7)); // Same
    }


    @Test
    void testAddFunction_AddDominatedTuple(){
        ParetoFront.Builder builder = new ParetoFront.Builder();

        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(800, 5, 0), 790));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(810, 4, 0), 800));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(820, 3, 0), 810));

        // dominated tupple, not adding to list
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(815, 6, 0), 800));

        ParetoFront paretoFront = builder.build();

        assertThrows(NoSuchElementException.class, () -> paretoFront.get(815, 6)); // Vérifie que 815,6 n'est pas présent
    }

    @Test
    void testAddFunction_AddDominatingEverythingTuple(){
        ParetoFront.Builder builder = new ParetoFront.Builder();

        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(805, 5, 0), 800));
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(810, 4, 0), 800));

        // dominated tupple, not adding to list
        builder.add(PackedCriteria.withDepMins(PackedCriteria.pack(804, 4, 0), 800));

        ParetoFront paretoFront = builder.build();

        assertThrows(NoSuchElementException.class, () -> paretoFront.get(805, 5));
        assertThrows(NoSuchElementException.class, () -> paretoFront.get(810, 4));
        assertEquals(1, paretoFront.size()); // why :((((((((((((
    }

    @Test
    void TestAddFromCourseExample(){

        long p1 = PackedCriteria.pack(8 * 60,3,1);
        long p2 = PackedCriteria.pack(8 * 60,4,1);
        long p3 = PackedCriteria.pack(8 * 60 + 1,2,1);
        long p4 = PackedCriteria.pack(8 * 60 + 2,1,1);
        long p5 = PackedCriteria.pack(8 * 60 + 3,0,1);
        long p6 = PackedCriteria.pack(8 * 60 + 4,1,1);

        ParetoFront.Builder front = new ParetoFront.Builder();

        front.add(p1);
        front.add(p2);
        front.add(p6);
        front.add(p3);
        front.add(p4);
        front.add(p5);
        assertEquals("480|3  481|2  482|1  483|0  ", front.toString());

    }

}
