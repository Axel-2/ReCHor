package ch.epfl.rechor.timetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyIndexedTest {

    @Test
    void size() {

        // Création d'une fausse implémentation de l'interface
        Indexed indexed = new Indexed() {
            @Override
            public int size() {
                return 3;
            }
        };

        // Vérifier que la méthode size() renvoie bien la valeur attendue
        assertEquals(3, indexed.size());
    }
}