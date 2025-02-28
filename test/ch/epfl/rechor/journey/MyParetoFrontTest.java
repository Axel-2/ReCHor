package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyParetoFrontTest {
    // TODO faire des tests alors que constructeur privé..

    @Test
    void size() {

        // S'assure que EMPTY est bien de taille zero
        assertEquals(0, ParetoFront.EMPTY.size());

        // TODO Tests avec des Pareto non empty
        ParetoFront.Builder paretoBuilder = new ParetoFront.Builder();

        int arrMins = 450;
        int changes = 3;
        int payload = 0;

        long criteria = PackedCriteria.pack(arrMins, changes, 0);

        // On ajoute 4x le même critère
        paretoBuilder
                .add(criteria)
                .add(criteria)
                .add(criteria)
                .add(criteria)
        ;

        ParetoFront paretoFront = paretoBuilder.build();

        int expectedSize = 4;
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
}