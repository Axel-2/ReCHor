package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static ch.epfl.rechor.IcalBuilder.Component.*;
import static ch.epfl.rechor.IcalBuilder.*;
import static ch.epfl.rechor.IcalBuilder.Name.*;
import static org.junit.jupiter.api.Assertions.*;

public class MyIcalBuilderTest {

    @Test
    void ComponentEnumIsCorrectlyDefined() {
        var expectedValues = new Component[]{
                VCALENDAR, VEVENT
        };
        assertArrayEquals(expectedValues, Component.values());
    }

    @Test
    void NameEnumIsCorrectlyDefined() {
        var expectedValues = new Name[]{
                BEGIN, END, PRODID, VERSION, UID, DTSTAMP, DTSTART,
                DTEND, SUMMARY, DESCRIPTION
        };
        assertArrayEquals(expectedValues, Name.values());
    }


    @Test
    void icalBuilderAddWorks() {
        IcalBuilder builder = new IcalBuilder();

        // Test summary
        builder.add(SUMMARY, "Départ du train à renens");

        // Ajout d'une ligne dont la valeur dépasse la limite et doit être pliée.
        // On crée une chaîne composée de 80 fois le caractère 'A'.
        String longValue = "A".repeat(80);
        builder.add(DESCRIPTION, longValue);


        // Test de pliage
        String expectedDescription = "DESCRIPTION:" + "A".repeat(63)
                + "\r\n" + " " + "A".repeat(17);

        // Assemblage complet attendu des lignes.
        String expected = "SUMMARY:Départ du train à renens" + "\r\n" + expectedDescription + "\r\n";;

        assertEquals(expected, builder.build());
    }


    @Test
    void testIcalBuilderAddLocalDateTime() {
        IcalBuilder builder = new IcalBuilder();

        // Création d'une date
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 18, 16, 13, 0);

        // Ajout de la date
        builder.add(DTSTAMP, dateTime);

        String expected = "DTSTAMP:20250218T161300\r\n";

        assertEquals(expected, builder.build());

        // test 2 pour tester si l'année est bien sur 4 chiffres dans tous les cas
        builder = new IcalBuilder();
        dateTime = LocalDateTime.of(456, 2, 18, 16, 13, 0);
        builder.add(DTSTAMP, dateTime);
        expected = "DTSTAMP:04560218T161300\r\n";
        assertEquals(expected, builder.build());

    }


    @Test
    void testBeginAndEndCompletesComponent() {
        IcalBuilder builder = new IcalBuilder();

        // Démarrage du composant VCALENDAR
        builder.begin(VCALENDAR);

        // on ferme également
        builder.end();

        // Le résultat attendu comporte une ligne de début et une ligne de fin,
        String expected = "BEGIN:VCALENDAR\r\n" + "END:VCALENDAR\r\n";
        assertEquals(expected, builder.build());
    }

    @Test
    void testEndWithoutBeginThrowsException() {
        IcalBuilder builder = new IcalBuilder();
        // Sans aucun composant commencé, l'appel à end() doit lever une exception.
        assertThrows(IllegalArgumentException.class, () -> builder.end());
    }

    @Test
    void testMultipleBeginsAndEnds() {
        IcalBuilder builder = new IcalBuilder();
        // Commencer le composant VCALENDAR
        builder.begin(VCALENDAR);
        // Commencer ensuite un VEVENT à l'intérieur de VCALENDAR
        builder.begin(VEVENT);
        // Terminer le dernier composant ouvert (VEVENT)
        builder.end();
        // Terminer le composant VCALENDAR
        builder.end();

        String expected = "BEGIN:VCALENDAR\r\n" +
                "BEGIN:VEVENT\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";
        assertEquals(expected, builder.build());
    }

}
