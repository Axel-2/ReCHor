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
        // TODO: ajouter des éléments si nécessaire avant de tester clear()
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
    public void testFullyDominatesTrue() {
        // Créer un builder contenant des tuples dominants
        ParetoFront.Builder dominantBuilder = new ParetoFront.Builder();
        // Créer des critères avec heure de départ
        long criteria1 = PackedCriteria.pack(10, 2, 100);
        criteria1 = PackedCriteria.withDepMins(criteria1, 8);
        dominantBuilder.add(criteria1);

        long criteria2 = PackedCriteria.pack(12, 1, 90);
        criteria2 = PackedCriteria.withDepMins(criteria2, 8);

        // 4 - 10 - 2
        // 5 - 12 - 1
        dominantBuilder.add(criteria1);
        dominantBuilder.add(criteria2);

        // Créer un builder qui est complètement dominé
        ParetoFront.Builder dominatedBuilder = new ParetoFront.Builder();
        // Ajouter des critères SANS heure de départ (ils recevront l'heure fixée par fullyDominates)

        // 8 - 15 - 3
        // 8 - 14 - 2
        dominatedBuilder.add(PackedCriteria.pack(150, 30, 120));
        dominatedBuilder.add(PackedCriteria.pack(140, 20, 110));

        // Vérifier que dominantBuilder domine complètement dominatedBuilder avec depMins=8
        assertTrue(dominantBuilder.fullyDominates(dominatedBuilder, 2),
                "Le builder dominant devrait dominer le builder dominé");
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

}
