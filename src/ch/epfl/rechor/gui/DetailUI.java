package ch.epfl.rechor.gui; // Mettre dans le bon package

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey; // Importer Journey
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private static final String LEGS_ID = "legs";
    private static final String ANNOTATIONS_ID = "annotations";



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


        // (6) Annotations
        List<Circle> circles = new ArrayList<>();
        Pane annotationsPane = new Pane();


        // (5) GridPane
        GridPane gridPane = new LineGridPane(annotationsPane, circles);
        gridPane.getStylesheets().add("legs");

        int currentRow = 0;

        for (Journey.Leg leg : journey.legs()) {

            switch (leg) {

                case Journey.Leg.Foot footLeg -> {

                    Text walkText = new Text(FormatterFr.formatLeg(footLeg));
                    // occupe les colonnes 2 à 3 sur une seule ligne
                    gridPane.add(walkText, 2, currentRow);

                }

                case Journey.Leg.Transport transportLeg -> {


                    // Partie 1 départ

                    // Heure de départ (col. 0)
                    Text depTime = new Text(FormatterFr.formatTime(transportLeg.depTime()));
                    depTime.getStyleClass().add("departure");
                    gridPane.add(depTime, 0, currentRow);


                    // Cercle de départ (Circle de rayon 3, col. 1)
                    Circle circle = new Circle();
                    circle.setRadius(3);
                    circles.add(circle);
                    gridPane.add(circle, 1, currentRow);

                    Text depStation = new Text(leg.depStop().name());
                    gridPane.add(depStation, 2, currentRow);


                    Text depPlatform = new Text(FormatterFr.formatPlatformName(transportLeg.depStop()));
                    depPlatform.getStyleClass().add("departure");
                    gridPane.add(depPlatform, 3, currentRow);

                    ++currentRow;

                    // Partie 2 Image et destination
                    // icône du véhicule (col. 0)
                    ImageView icon =  new ImageView(VehicleIcons.iconFor(transportLeg.vehicle()));
                    icon.setFitHeight(31);
                    icon.setFitWidth(31);
                    int iconRowSpan = leg.intermediateStops().isEmpty() ? 1 : 2;
                    gridPane.add(icon, 0, currentRow, 1, iconRowSpan);
                    Text routeDestinationText = new Text(FormatterFr.formatRouteDestination(transportLeg));
                    gridPane.add(routeDestinationText, 2, currentRow, 2,1);


                    if (!leg.intermediateStops().isEmpty()) {

                        // on oublie pas d'update le currentRow
                        ++currentRow;

                        GridPane intermediateGrid = new GridPane();
                        intermediateGrid.getStyleClass().add("intermediate-stops");

                        int rowIndexIntermediateSteps = 0;
                        for (Journey.Leg.IntermediateStop stop : leg.intermediateStops()) {
                            Text depTimeText = new Text(FormatterFr.formatTime(stop.depTime()));
                            Text arrTimeText = new Text(FormatterFr.formatTime(stop.arrTime()));
                            Text name = new Text(stop.stop().name());

                            intermediateGrid.add(arrTimeText, 0, rowIndexIntermediateSteps);
                            intermediateGrid.add(depTimeText, 1, rowIndexIntermediateSteps);
                            intermediateGrid.add(name, 2, rowIndexIntermediateSteps);

                            rowIndexIntermediateSteps++;
                        }

                        String title = new StringBuilder()
                                .append(leg.intermediateStops().size())
                                .append(" arrêts, ")
                                .append(FormatterFr.formatDuration(leg.duration()))
                                .toString();
                        TitledPane titledPane = new TitledPane(title, intermediateGrid);

                        Accordion accordion = new Accordion(titledPane);

                        gridPane.add(accordion, 2, currentRow, 2, 1);
                    }
                    ++currentRow;

                    // Partie 4 heure d'arrivée

                    // Heure d'arrivée (col. 0)
                    Text arrTime = new Text(FormatterFr.formatTime(transportLeg.arrTime()));
                    gridPane.add(arrTime, 0, currentRow);

                    // Cercle d'arrivée (col. 1)
                    Circle arrCircle = new Circle();
                    arrCircle.setRadius(3);
                    circles.add(arrCircle);
                    gridPane.add(arrCircle, 1, currentRow);

                    // Nom de la gare d'arrivée (col. 2)
                    Text arrStation = new Text(leg.arrStop().name());
                    gridPane.add(arrStation, 2, currentRow);

                    // Nom de la voie/quai d'arrivée (col. 3)
                    Text arrPlatform = new Text(FormatterFr.formatPlatformName(transportLeg.arrStop()));
                    gridPane.add(arrPlatform, 3, currentRow);
                }
            }

            ++currentRow;
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

        calendarButton.setOnAction(event -> {
            calendarClick(journey,calendarButton);
        });





        // (4) Stack pane et HBox
        StackPane stackPane2 = new StackPane(gridPane, annotationsPane);
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
        gridPane.setId(LEGS_ID);
        annotationsPane.setId(ANNOTATIONS_ID);

        return new DetailUI(scrollPane);
    }

    // méthode privée pour la gestion d'un clic sur le bouton carte
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
    // méthode privée pour la gestion d'un clic sur le bouton calendrier
    private static void calendarClick(Journey j, Node node){
        try {
            FileChooser fileChooser = new FileChooser();
            String calendar = JourneyIcalConverter.toIcalendar(j);
            String title = String.format("voyage_%s.ics", LocalDate.now());
            fileChooser.setInitialFileName(title);

            File file = fileChooser.showSaveDialog(node.getScene().getWindow());
            Files.writeString(file.toPath(), calendar, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("Erreur dans l'écriture du calendrier");
        }
    }

    private static class LineGridPane extends GridPane{
        private final Pane annotationsPane;
        private final List<Circle> circles;

        public LineGridPane(Pane annotationsPane, List<Circle> circles){
            this.annotationsPane = annotationsPane;
            this.circles = circles;
        }

        @Override
        protected void layoutChildren(){
            super.layoutChildren();

            if(circles.size() % 2 != 0){
                System.out.println("Nombre de cercles impair...");
                return;
            }

            // Pour chaque paire de cercle
            for (int i = 0; i < circles.size(); i += 2) {
                Circle first = circles.get(i);
                Circle second = circles.get(i+1);

                // Création de la ligne
                Line line = new Line(
                        first.getBoundsInParent().getCenterX(),
                        first.getBoundsInParent().getCenterY(),
                        second.getBoundsInParent().getCenterX(),
                        second.getBoundsInParent().getCenterY());

                // Modifications selon énoncé
                line.setStroke(javafx.scene.paint.Color.RED);
                line.setStrokeWidth(2);

                // On l'ajoute
                annotationsPane.getChildren().add(line);
            }
        }
    }
}