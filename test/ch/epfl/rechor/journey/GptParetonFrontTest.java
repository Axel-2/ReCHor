package ch.epfl.rechor.journey;

import static org.junit.jupiter.api.Assertions.*;

import ch.epfl.rechor.journey.PackedCriteria;
import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.timetable.*;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;



/* --- Tests unitaires pour les fonctions de la consigne --- */

public class GptParetonFrontTest {



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
        if(t1 < t2) {
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

//    @Test
//    void testFullyDominatesFunctionality() {
//        // Teste fullyDominates dans deux scénarios :
//        // 1. Un builder domine entièrement un autre.
//        // 2. Un builder ne domine pas entièrement l'autre.
//        ParetoFront.Builder builderDominating = new ParetoFront.Builder();
//        ParetoFront.Builder builderDominated = new ParetoFront.Builder();
//        ParetoFront.Builder builderNonDominating = new ParetoFront.Builder();
//
//        // Dans notre modèle, un critère avec une arrivée plus tôt et moins de changements est meilleur.
//        long good = PackedCriteria.pack(400, 2, 50); // Très performant
//        builderDominating.add(good);
//
//        long bad1 = PackedCriteria.pack(450, 3, 100); // Moins bon
//        long bad2 = PackedCriteria.pack(460, 4, 150); // Moins bon
//        builderDominated.add(bad1).add(bad2);
//
//        // Dans ce cas, good domine bad1 et bad2
//        boolean dominates = builderDominating.fullyDominates(builderDominated, 500);
//        assertTrue(dominates, "Le builderDominating devrait entièrement dominer builderDominated");
//
//        // Pour le cas négatif, on ajoute un tuple médiocre qui ne domine pas tous les tuples de builderDominated.
//        long mediocre = PackedCriteria.pack(420, 3, 80);
//        builderNonDominating.add(mediocre);
//        boolean dominates2 = builderNonDominating.fullyDominates(builderDominated, 500);
//        assertFalse(dominates2, "Le builderNonDominating ne doit pas entièrement dominer builderDominated");
//    }
}
