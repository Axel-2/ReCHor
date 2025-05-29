package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
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
import java.util.Objects;

import static java.awt.Desktop.getDesktop;

/**
 * Classe qui représente la partie de l'interface
 * graphique qui montre les détails d'un voyage
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record DetailUI(Node rootNode) {

    private static final String DETAIL_CSS_PATH = "/detail.css";

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

    // Cercles
    private final static int CIRCLE_RADIUS = 3;
    private final static int ICON_SIZE = 31;

    private final static int COLUMN_INDEX_LEGS_GRID = 2;
    private final static String DEPARTURE_LABEL = "departure";

    // Accordéon
    private final static int ACC_COL_INDEX = 2;
    private final static int ACC_COL_SPAN = 2;
    private final static int ACC_ROW_SPAN = 1;

    private final static int STROKE_WIDTH = 2;

    private final static String INTERMEDIATE_STOP_STYLE = "intermediate-stops";


    /**
     * Fonction dont le but est de créer le graphe de
     * scène et de retourner une instance de DetailUI
     * @return une scène détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {

        // Récupération du voyage

        ScrollPane scroll = new ScrollPane(); // Noeud racine
        scroll.setId(DETAIL_ID);
        scroll.getStylesheets().add(loadCSS());

        // 1) Contenu initial :
        scroll.setContent(buildContent(journeyObservableValue.getValue()));

        // 2) Dès que le voyage change, on change également le contenu
        journeyObservableValue.subscribe(
                () -> {
                    scroll.setContent(buildContent(journeyObservableValue.getValue())); // Ajout du contenu
                }
        );

        return new DetailUI(scroll);
    }


        // ---------- Création des composants à ajouter dans le scrollPane-----------

    /**
     * Fonction qui sert à construire un nœud contenant
     * la description détaillée d'un voyage
     * @param journey un voyage donné
     * @return un noeud
     */
    private static Node buildContent(Journey journey) {
            List<Circle> circles = new ArrayList<>();

            Pane annotationsPane = new Pane();
            annotationsPane.setId(ANNOTATIONS_ID);
            GridPane gridPane = createLegsGrid(journey, annotationsPane, circles);

            // Boutons
            Button mapBtn = new Button(MAP_BUTTON_TEXT);
            Button calBtn = new Button(CALENDAR_BUTTON_TEXT);
            HBox btnBox = new HBox(mapBtn, calBtn);
            btnBox.setId(HBOX_ID);

            mapBtn.setOnAction(e -> mapClick(journey));
            calBtn.setOnAction(e -> calendarClick(journey, calBtn));

            // À afficher s'il n'y a aucun voyage à charger
            Text noTxt = new Text(NO_JOURNEY_TEXT);
            VBox noBox = new VBox(noTxt);
            noBox.setId(NO_JOURNEY_ID);

            // À afficher s'il y a un voyage à charger
            VBox yesBox = new VBox(new StackPane(annotationsPane, gridPane), btnBox);

            boolean hasJourney = journey != null;
            noBox.setVisible(!hasJourney);
            yesBox.setVisible(hasJourney);

            return new StackPane(noBox, yesBox); // Enfant direct du nœud racine
        }


    /**
     * Fonction qui s'occupe de la création de la grille contenant toutes les étapes
     * @param journey un voyage
     * @param annotationsPane une pane
     * @param circles la liste des cercles
     * @return la grille
     */
    private static GridPane createLegsGrid(Journey journey, Pane annotationsPane, List<Circle> circles) {
        GridPane gridPane = new LineGridPane(annotationsPane, circles);
        gridPane.setId(LEGS_ID);

        // On parcourt toutes les étapes et on affiche ce qu'il faut en fonction du type
        int currentRow = 0;

        if (journey != null) {
            for (Journey.Leg leg : journey.legs()) {
                switch (leg) {
                    case Journey.Leg.Foot footLeg -> {
                        Text walkText = new Text(FormatterFr.formatLeg(footLeg));
                        gridPane.add(walkText, COLUMN_INDEX_LEGS_GRID, currentRow);
                    }

                    case Journey.Leg.Transport transportLeg ->
                            currentRow = addTransportLeg(gridPane, circles, transportLeg, currentRow);
                }
                ++currentRow;
            }
        }
        return gridPane;
    }

    private static int addTransportLeg(GridPane gridPane, List<Circle> circles,
                                       Journey.Leg.Transport t, int row) {


        // Départ
        Text depTime = new Text(FormatterFr.formatTime(t.depTime()));
        depTime.getStyleClass().add(DEPARTURE_LABEL);

        Circle startCircle = new Circle(CIRCLE_RADIUS);
        circles.add(startCircle);

        Text DEPSTOP_NAME_TEXT = new Text(t.depStop().name());
        Text PLATFORM_NAME_TEXT = new Text(FormatterFr.formatPlatformName(t.depStop()));
        Text ROUTE_DESTINATION_TEXT = new Text(FormatterFr.formatRouteDestination(t));
        PLATFORM_NAME_TEXT.getStyleClass().add(DEPARTURE_LABEL);

        gridPane.addRow(row++, depTime, startCircle, DEPSTOP_NAME_TEXT, PLATFORM_NAME_TEXT);


        // Gestion de l'icone
        ImageView icon = new ImageView(VehicleIcons.iconFor(t.vehicle()));
        icon.setFitWidth(ICON_SIZE);
        icon.setFitHeight(ICON_SIZE);
        int iconSpan = t.intermediateStops().isEmpty() ? 1 : 2;
        int colSpan = 1;


        gridPane.add(icon, 0, row, colSpan++, iconSpan);
        gridPane.add(ROUTE_DESTINATION_TEXT, 2, row, colSpan, 1);


        if (!t.intermediateStops().isEmpty()) {
            // On passe au row suivant
            ++row;

            GridPane inner = new GridPane();
            inner.getStyleClass().add(INTERMEDIATE_STOP_STYLE);

            int rowIndex = 0;
            for (Journey.Leg.IntermediateStop stop : t.intermediateStops()) {
                final Text ARR_TIME_TEXT = new Text(FormatterFr.formatTime(stop.arrTime()));
                final Text DEP_TIME_TEXT = new Text(FormatterFr.formatTime(stop.depTime()));
                final Text STOP_NAME_TEXT = new Text(stop.stop().name());

                inner.addRow(rowIndex++, ARR_TIME_TEXT, DEP_TIME_TEXT, STOP_NAME_TEXT);
            }

            // Création du titre
            int stopCount = t.intermediateStops().size();
            String stopLabel = stopCount == 1 ? "arrêt" : "arrêts";
            String duration   = FormatterFr.formatDuration(t.duration());
            final String title = String.format("%d %s, %s", stopCount, stopLabel, duration);

            Accordion acc = new Accordion(new TitledPane(title, inner));
            gridPane.add(acc, ACC_COL_INDEX, row, ACC_COL_SPAN, ACC_ROW_SPAN);
        }

        ++row;

        // Arrivée
        Text arrTime = new Text(FormatterFr.formatTime(t.arrTime()));
        Text arrStopName = new Text(t.arrStop().name());
        Text platformName = new Text(FormatterFr.formatPlatformName(t.arrStop()));

        // Cercle d'arrivée
        Circle endCircle = new Circle(CIRCLE_RADIUS);
        circles.add(endCircle);

        // Ajout de la ligne
        gridPane.addRow(
                row,
                arrTime,
                endCircle,
                arrStopName,
                platformName
        );

        return row;
    }

    // Méthode privée s'occupant du CSS
    private static String loadCSS() {
        try {
            return Objects.requireNonNull(DetailUI.class.getResource(DetailUI.DETAIL_CSS_PATH)).toExternalForm();
        } catch (NullPointerException e) {
            System.err.printf("Erreur de chargement CSS : %s%n", DetailUI.DETAIL_CSS_PATH);
            return "";
        }
    }

    // Méthode privée pour la gestion d'un clic sur le bouton carte
    private static void mapClick(Journey j) {
        try {
            final URI url = new URI(
                    "https",
                    "umap.osm.ch",
                    "/fr/map",
                    "data=%s".formatted(JourneyGeoJsonConverter.toGeoJson(j)),
                    null
            );

            getDesktop().browse(url);
        } catch(Exception e) {
            System.out.println("Erreur dans l'ouverture du browser");
        }
    }

    /**
     * Méthode privée pour la gestion d'un clic sur le bouton calendrier
     * @param j un voyage
     * @param node un noeud
     */
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

    // Redéfinition d'un gridPane pour afficher les lignes / cercles comme on veut
    private static class LineGridPane extends GridPane {
        private final Pane annotationsPane;
        private final List<Circle> circles;

        public LineGridPane(Pane annotationsPane, List<Circle> circles){
            this.annotationsPane = annotationsPane;
            this.circles = circles;
        }

        @Override
        protected void layoutChildren() {

            super.layoutChildren();

            // Vider l’ancien contenu
            annotationsPane.getChildren().clear();

            for (int i = 0; i < circles.size(); i += 2) {
                Circle first = circles.get(i);
                Circle second = circles.get(i+1);

                // Création de la ligne, qui relie les coordonnées de la paire de cercle
                Line line = new Line(
                        first.getBoundsInParent().getCenterX(),
                        first.getBoundsInParent().getCenterY(),
                        second.getBoundsInParent().getCenterX(),
                        second.getBoundsInParent().getCenterY()
                );

                // Modifications selon énoncé
                line.setStroke(javafx.scene.paint.Color.RED);
                line.setStrokeWidth(STROKE_WIDTH);

                annotationsPane.getChildren().add(line);
            }
        }
    }
}

