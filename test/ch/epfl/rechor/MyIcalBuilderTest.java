package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static ch.epfl.rechor.IcalBuilder.Component.*;
import static ch.epfl.rechor.IcalBuilder.*;
import static ch.epfl.rechor.IcalBuilder.Name.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        // Ajout d'une ligne qui ne nécessite pas de pliage.
        builder.add(SUMMARY, "Départ du train à renens");

        // Ajout d'une ligne dont la valeur dépasse la limite et doit être pliée.
        // On crée une chaîne composée de 80 fois le caractère 'A'.
        String longValue = "A".repeat(80);
        builder.add(DESCRIPTION, longValue);

        /*
         * Pour la ligne DESCRIPTION :
         * - Le préfixe "DESCRIPTION:" fait 12 caractères (11 lettres + 1 pour le deux-points).
         * - Ainsi, la première ligne peut contenir 75 - 12 = 63 caractères de la valeur.
         * - Les 80 - 63 = 17 caractères restants seront placés sur la ligne suivante,
         *   préfixés par un espace (selon la spécification iCalendar).
         */
        String expectedDescription = "DESCRIPTION:" + "A".repeat(63)
                + "\r\n" + " " + "A".repeat(17);

        // Assemblage complet attendu des lignes.
        // Chaque appel à add ajoute une ligne terminée par un CRLF.
        String expected = "SUMMARY:Départ du train à renens" + "\r\n" + expectedDescription + "\r\n";;

        assertEquals(expected, builder.build());
    }


    @Test
    void testIcalBuilderAddLocalDateTime() {
        IcalBuilder builder = new IcalBuilder();

        // Création d'une date/heure correspondant au 18 février 2025 à 16h13 et 00 secondes
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 18, 16, 13, 0);

        // Ajout de la date sous l'attribut DTSTAMP
        builder.add(DTSTAMP, dateTime);

        // La représentation textuelle attendue est "20250218T161300"
        // La ligne complète, avec nom et séparateur, se termine par un CRLF ("\r\n")
        String expected = "DTSTAMP:20250218T161300\r\n";

        assertEquals(expected, builder.build());
    }


    @Test
    void testBeginAddsCorrectLine() {
        IcalBuilder builder = new IcalBuilder();
        // Démarrage du composant VCALENDAR
        builder.begin(VCALENDAR);

        // La ligne attendue est "BEGIN:VCALENDAR" suivie d'un CRLF
        String expected = "BEGIN:VCALENDAR\r\n";

        assertEquals(expected, builder.build());
    }

}
