package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyParetoFrontTest {
    // TODO faire des tests alors que constructeur privé..

    // ----------------------------------------------------------------------
    // Test de size()
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------

    @Test
    void testGetFunctionality() {

        // Ajout de plusieurs tuples et vérification que get() renvoie le bon tuple.
        // les 3 ne doivent pas se dominer entre eux
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t1 = PackedCriteria.pack(450, 3, 100); // tuple : arrivée 450, 3 changements, payload 100
        long t2 = PackedCriteria.pack(470, 2, 200); // tuple : arrivée 470, 2 changements, payload 200
        long t3 = PackedCriteria.pack(480, 1, 300); // tuple : arrivée 480, 4 changements, payload 300

        builder.add(t1).add(t2).add(t3);
        ParetoFront front = builder.build();

        // Vérification que chaque tuple peut être retrouvé via get(arrMins, changes)
        assertEquals(t1, front.get(450, 3), "Le tuple (450, 3) doit être présent et correspondre à t1");
        assertEquals(t2, front.get(470, 2), "Le tuple (470, 2) doit être présent et correspondre à t2");
        assertEquals(t3, front.get(480, 1), "Le tuple (480, 4) doit être présent et correspondre à t3");

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
    void forEach() {

        // ForEach de EMPTY donne une liste vide
        List<Long> collected = new ArrayList<>();
        ParetoFront.EMPTY.forEach(value -> {collected.add(value);});
        assertTrue(collected.isEmpty());

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
}