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

    }

    @Test
    void get() {
        // Get un tableau EMPTY lève l'exception
        assertThrows(NoSuchElementException.class, () -> {
            ParetoFront.EMPTY.get(600, 2);
        });

        // TODO tester avec des Pareto non empty
    }

    @Test
    void forEach() {

        // ForEach de EMPTY donne une liste vide
        List<Long> collected = new ArrayList<>();
        ParetoFront.EMPTY.forEach(value -> {collected.add(value);});
        assertTrue(collected.isEmpty());
    }

    @Test
    void testToString() {
    }
}