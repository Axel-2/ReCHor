package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void exempleDuSite() {

        // TODO c'est pas un long ça mais en gros j'aimerai ajouter 32 0 en plus
        // pour un payload vide
        long expected = (long) 0b0110100101111001100001100000001;

        // 8H00 depuis miniuit
        int a = 8 * 60;

        // 9h depuis minuit
        int b = 9 * 60;

        int changements = 2;

        long actual = PackedCriteria.pack(a, 2, 0);

    }

}
