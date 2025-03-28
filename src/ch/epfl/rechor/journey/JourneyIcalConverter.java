package ch.epfl.rechor.journey;


import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Classe qui offre une méthode permettant de convertir un voyage en un événement
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class JourneyIcalConverter {

    // Pour rendre la classe non instantiable
    private JourneyIcalConverter() {}

    /**
     * Convertir un voyage en string, qui constituera le contenu du fichier ics
     * @param journey voyage commplets, ayant une liste d'étapes
     * @return la string immuable
     */
    public static String toIcalendar(Journey journey) {

        // Création du builder
        IcalBuilder builder = new IcalBuilder()

                // Begin
                .begin(IcalBuilder.Component.VCALENDAR)

                // Version
                .add(IcalBuilder.Name.VERSION, "2.0")

                // Prodid
                .add(IcalBuilder.Name.PRODID, "ReCHor")

                // Begin 2
                .begin(IcalBuilder.Component.VEVENT)

                // UID
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())

                // DTSTAMP
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now())

                // DTSTART
                .add(IcalBuilder.Name.DTSTART, journey.depTime())

                // DTEND
                .add(IcalBuilder.Name.DTEND, journey.arrTime())

                // SUMMARY
                .add(
                        IcalBuilder.Name.SUMMARY,
                        new StringBuilder()
                                .append(journey.depStop().name())
                                .append(" → ")
                                .append(journey.arrStop().name())
                                .toString()
                );


        // DESCRIPTION

        // Création du joiner,
        // il faut faire bien attention à escape le \n
        StringJoiner j = new StringJoiner("\\n");

        // Formatage selon le type d'étape
        for (Journey.Leg leg : journey.legs()){
            switch (leg) {
                case Journey.Leg.Foot f ->
                        j.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t ->
                        j.add(FormatterFr.formatLeg(t));
            }
        }

        // Conversion en string immuable, puis ajout au builder
        String descriptionContent = j.toString();
        builder.add(IcalBuilder.Name.DESCRIPTION, descriptionContent);

        // END 1
        builder.end()

        // END 2
        .end();

        return builder.build();
    }
}
