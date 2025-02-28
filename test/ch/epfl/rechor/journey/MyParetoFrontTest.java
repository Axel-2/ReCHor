package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyParetoFrontTest {
    // TODO faire des tests alors que constructeur privé..

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
    void testClearBuilder() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long crit1 = PackedCriteria.pack(450, 3, 0);
        long crit2 = PackedCriteria.pack(470, 2, 0);
        builder.add(crit1).add(crit2);
        assertFalse(builder.isEmpty(), "Le bâtisseur ne doit pas être vide après ajout d'éléments");
        builder.clear();
        ParetoFront front = builder.build();
        assertEquals(0, front.size(), "Après clear, la frontière de Pareto doit être vide");
    }

    @Test
    void testAddAllMethod() {
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        ParetoFront.Builder builder2 = new ParetoFront.Builder();

        long crit1 = PackedCriteria.pack(450, 3, 0);
        long crit2 = PackedCriteria.pack(470, 2, 0);

        builder1.add(crit1);
        builder2.add(crit2);

        // Ajoute tous les éléments de builder2 dans builder1
        builder1.addAll(builder2);
        ParetoFront front = builder1.build();
        assertEquals(2, front.size(), "Après addAll, la frontière doit contenir la réunion des tuples non dominés");
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

    @Test
    void size() {

        ParetoFront.Builder paretoBuilder = new ParetoFront.Builder();

        int arrMins = 450, arrMins2 =  470, arrMins3 = 500;
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
                .add(dominatedCriteria)
        ;

        ParetoFront paretoFront = paretoBuilder.build();

        // on doit donc normalement en avoir 2
        int expectedSize = 2;

        int currentSize = paretoFront.size();

        assertEquals(expectedSize, currentSize);
    }

    @Test
    void get() {

        // 1. Emptys
        // Get un tableau EMPTY lève l'exception
        assertThrows(NoSuchElementException.class, () -> ParetoFront.EMPTY.get(600, 2));

        // 2. Tester avec des Pareto non empty :

        ParetoFront.Builder paretoBuilder = new ParetoFront.Builder();

        int arrMins = 1500;
        int changes = 7;
        int payload = 0;

        long criteria = PackedCriteria.pack(arrMins, changes, payload);

        paretoBuilder.add(criteria);

        ParetoFront paretoFront = paretoBuilder.build();
        long expectedGotCriteria = paretoFront.get(arrMins, changes);

        assertEquals(criteria, expectedGotCriteria);
    }

    @Test
    void forEach() {

        // ForEach de EMPTY donne une liste vide
        List<Long> collected = new ArrayList<>();
        ParetoFront.EMPTY.forEach(value -> {collected.add(value);});
        assertTrue(collected.isEmpty());

        // TODO
    }

    @Test
    void testToString() {
        // TODO
    }

    @Test
    void builder_defaultConstructor_createsEmptyBuilder() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty());

        // TODO
    }

    @Test
    void builder_clear_makesBuilderEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        //TODO add des trucs ici
        builder.clear();
        assertTrue(builder.isEmpty());
    }

    @Test
    public void testFullyDominatesTrue() {
        // Créer un builder contenant des tuples dominants
        ParetoFront.Builder dominantBuilder = new ParetoFront.Builder();
        long criteria1 = PackedCriteria.pack(10, 2, 100);
        criteria1 = PackedCriteria.withDepMins(criteria1, 4);
        dominantBuilder.add(criteria1);

        long criteria2 = PackedCriteria.pack(12, 1, 90);
        criteria2 = PackedCriteria.withDepMins(criteria2, 5);
        dominantBuilder.add(criteria2);
        dominantBuilder.add(12, 1, 90);

        // Créer un builder qui est complètement dominé
        ParetoFront.Builder dominatedBuilder = new ParetoFront.Builder();
        dominatedBuilder.add(15, 3, 120);
        dominatedBuilder.add(14, 2, 110);

        // Vérifier que dominantBuilder domine complètement dominatedBuilder avec un depMins donné
        assertTrue(dominantBuilder.fullyDominates(dominatedBuilder, 8),
                "Le builder dominant devrait dominer le builder dominé");
    }

    @Test
    public void testFullyDominatesFalse() {
        // Créer un builder contenant des tuples dominants
        ParetoFront.Builder dominantBuilder = new ParetoFront.Builder();
        dominantBuilder.add(10, 2, 100);
        dominantBuilder.add(12, 1, 90);

        // Créer un builder qui n'est pas totalement dominé
        ParetoFront.Builder nonDominatedBuilder = new ParetoFront.Builder();
        nonDominatedBuilder.add(8, 3, 80);  // Ce tuple ne devrait pas être dominé

        // Vérifier que dominantBuilder ne domine pas totalement nonDominatedBuilder
        assertFalse(dominantBuilder.fullyDominates(nonDominatedBuilder, 5),
                "Le builder dominant ne devrait pas totalement dominer nonDominatedBuilder");
    }

    @Test
    public void testFullyDominatesWithEmptyBuilder() {
        // Créer un builder contenant des tuples
        ParetoFront.Builder dominantBuilder = new ParetoFront.Builder();
        dominantBuilder.add(10, 2, 100);
        dominantBuilder.add(12, 1, 90);

        // Créer un builder vide
        ParetoFront.Builder emptyBuilder = new ParetoFront.Builder();

        // Un builder vide est toujours dominé, donc fullyDominates doit retourner true
        assertTrue(dominantBuilder.fullyDominates(emptyBuilder, 0),
                "Un builder vide devrait toujours être dominé");
    }

    @Test
    public void testFullyDominatesWithSelf() {
        // Créer un builder contenant des tuples
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(10, 2, 100);
        builder.add(12, 1, 90);

        // Un builder ne peut pas se dominer lui-même totalement
        assertFalse(builder.fullyDominates(builder, 10),
                "Un builder ne devrait pas totalement se dominer lui-même");
    }
}