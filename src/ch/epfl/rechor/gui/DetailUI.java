package ch.epfl.rechor.gui; // Mettre dans le bon package

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.Json; // Importer si nécessaire pour Journey plus tard
import ch.epfl.rechor.journey.Journey; // Importer Journey
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle; // Importer pour plus tard
import javafx.scene.shape.Line;   // Importer pour plus tard
import javafx.scene.text.Text;
import javafx.beans.property.SimpleObjectProperty; // Pour le test initial

import java.net.URI;
import java.util.Objects; // Pour gérer le chemin CSS

import static java.awt.Desktop.getDesktop;

/**
 * Classe qui représente la partie de l'interface
 * graphique qui montre les détails d'un voyage
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record DetailUI(Node rootNode) {

    // ID's
    private static final String NO_JOURNEY_ID = "no-journey";
    private static final String DETAIL_ID = "detail";
    private static final String HBOX_ID = "buttons";

    // Textes
    private static final String NO_JOURNEY_TEXT = "Aucun voyage";
    private static final String MAP_BUTTON_TEXT = "Carte";
    private static final String CALENDAR_BUTTON_TEXT = "Calendrier";


    /**
     * Fonction dont le but est de créer le graphe de
     * scène et de retourner une instance de DetailUI
     * @return une scène détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {

        // Récupération du voyage
        Journey journey = journeyObservableValue.getValue();


        // ---------- Création des composants ----------- (de bas en haut)


        // (5) GridPane
        GridPane gridPane = new GridPane();

        int row = 0;

        for (Journey.Leg leg : journey.legs()) {

            if (leg instanceof Journey.Leg.Foot) {
                String text = FormatterFr.formatLeg((Journey.Leg.Foot) leg);
                Text walkText = new Text(text);

                // occupe les colonnes 2 à 3 sur une seule ligne
                gridPane.add(walkText, 2, row);

            } else {

                // ligne 1 heure et nom
                Text depTime = new Text(FormatterFr.formatTime(leg.depTime()));
                gridPane.add(depTime, 0, row);

                Text depStation = new Text(leg.depStop().name());
                gridPane.add(depStation, 2, row);


                if (!leg.intermediateStops().isEmpty()) {
                    Accordion acc = new Accordion();

                    gridPane.add(acc, 2, row, 2, 1);
                }

            }

            ++row;
        }



        // colonne 0: heures de départ et d'arrivée, et icône du véhicule
        // GridPane.setRowIndex(,0 );


        // colonne 1: cercles de départ et d'arrivée
        // GridPane.setRowIndex(,1);

        // colonne 2: nom de la gare de départ et d'arrivée
        // GridPane.setRowIndex(,2);

        // colonne 3: voie/quai de départ et d'arrivée
        //GridPane.setRowIndex(,3);



        // Boutons map et calendrier
        Button mapButton = new Button(MAP_BUTTON_TEXT);
        Button calendarButton = new Button(CALENDAR_BUTTON_TEXT);

        mapButton.setOnAction(event -> {
            mapClick(journey);
        });




        // (4) Stack pane et HBox
        StackPane stackPane2 = new StackPane(gridPane);
        HBox buttonsBox = new HBox(mapButton, calendarButton);



        // (3.5) Text de la box "noJourney"
        Text noJourneyBoxText = new Text(NO_JOURNEY_TEXT);

        // (3) Deux box (enfants du stack pane)
        VBox noJourneyBox = new VBox(noJourneyBoxText);
        VBox journeyBox = new VBox(stackPane2, buttonsBox);

        // On affiche la bonne box en fonction de la présence d'un voyage ou non
        boolean isJourneyToDisplay = (journey != null);
        // isJourneyToDisplay = true;
        noJourneyBox.setVisible(!isJourneyToDisplay);
        journeyBox.setVisible(isJourneyToDisplay);

        // (2) Noeud secondaire
        StackPane stackPane = new StackPane(noJourneyBox, journeyBox); // On associe les box au stack pane

        // (1) Noeud principal
        ScrollPane scrollPane= new ScrollPane(stackPane);

        // CSS
        try {
            String cssPath = Objects.requireNonNull(
                    DetailUI.class.getResource("/detail.css") // Assure-toi que le chemin est correct
            ).toExternalForm();
            scrollPane.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Erreur chargement CSS detail.css: " + e.getMessage());
            // Gérer l'erreur ou l'ignorer si le CSS n'est pas critique pour ce test
        }


        // ID's
        scrollPane.setId(DETAIL_ID);
        noJourneyBox.setId(NO_JOURNEY_ID);
        buttonsBox.setId(HBOX_ID);

        return new DetailUI(scrollPane);
    }

    private static void mapClick(Journey j){
        try {
            URI url = new URI(
                    "https",
                    "umap.osm.ch",
                    "/fr/map",
                    "data=" + JourneyGeoJsonConverter.toGeoJson(j),
                    null
            );

            getDesktop().browse(url);
        } catch(Exception e){
            System.out.println("Erreur dans l'ouverture du browser");
        }
    }
}
