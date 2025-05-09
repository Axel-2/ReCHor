package ch.epfl.rechor.gui; // Mettre dans le bon package

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey; // Importer Journey
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.binding.Bindings;
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

// TODO 
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


    /**
     * Fonction dont le but est de créer le graphe de
     * scène et de retourner une instance de DetailUI
     * @return une scène détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {

        ScrollPane scroll = new ScrollPane();
        scroll.setId(DETAIL_ID);
        scroll.getStylesheets().add(loadCSS(DETAIL_CSS_PATH));

        // 1) initialisation
        scroll.setContent(buildContent(journeyObservableValue.getValue()));

        // 2) à chaque changement, on ré-affiche
        journeyObservableValue.addListener((obs, oldJ, newJ) -> {
            scroll.setContent(buildContent(newJ));
        });
        return new DetailUI(scroll);

    }


        // ---------- Création des composants ----------- (de bas en haut)
        private static Node buildContent(Journey journey) {
            List<Circle> circles = new ArrayList<>();

            Pane annotationsPane = new Pane();
            GridPane gridPane = createLegsGrid(journey, annotationsPane, circles);

            // Boutons
            Button mapBtn = new Button(MAP_BUTTON_TEXT);
            mapBtn.setOnAction(e -> mapClick(journey));
            Button calBtn = new Button(CALENDAR_BUTTON_TEXT);
            calBtn.setOnAction(e -> calendarClick(journey, calBtn));
            HBox btnBox = new HBox(mapBtn, calBtn);
            btnBox.setId(HBOX_ID);

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

    // Création de la grille contenant toutes les étapes
    private static GridPane createLegsGrid(Journey journey, Pane annotationsPane, List<Circle> circles) {
        GridPane gridPane = new LineGridPane(annotationsPane, circles);
        gridPane.setId(LEGS_ID);
        gridPane.getStylesheets().add("legs");

        // On parcourt toutes les étapes et on affiche ce qu'il faut en fonction du type
        int currentRow = 0;
        if (journey != null) {
            for (Journey.Leg leg : journey.legs()) {
                switch (leg) {
                    case Journey.Leg.Foot footLeg -> {
                        Text walkText = new Text(FormatterFr.formatLeg(footLeg));
                        gridPane.add(walkText, 2, currentRow);
                    }
                    case Journey.Leg.Transport transportLeg -> {
                        currentRow = addTransportLeg(gridPane, circles, transportLeg, currentRow);
                    }
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
        depTime.getStyleClass().add("departure");
        gridPane.add(depTime, 0, row);
        Circle startCircle = new Circle(CIRCLE_RADIUS);
        circles.add(startCircle);
        gridPane.add(startCircle, 1, row);
        gridPane.add(new Text(t.depStop().name()), 2, row);
        Text depPlat = new Text(FormatterFr.formatPlatformName(t.depStop()));
        depPlat.getStyleClass().add("departure");
        gridPane.add(depPlat, 3, row);
        ++row;

        // Trajet
        ImageView icon = new ImageView(VehicleIcons.iconFor(t.vehicle()));
        icon.setFitWidth(ICON_SIZE);
        icon.setFitHeight(ICON_SIZE);
        int span = t.intermediateStops().isEmpty() ? 1 : 2;
        gridPane.add(icon, 0, row, 1, span);
        gridPane.add(new Text(FormatterFr.formatRouteDestination(t)), 2, row, 2, 1);
        if (!t.intermediateStops().isEmpty()) {
            ++row;
            GridPane inner = new GridPane();
            inner.getStyleClass().add("intermediate-stops");
            int r = 0;
            for (Journey.Leg.IntermediateStop stop : t.intermediateStops()) {
                inner.add(new Text(FormatterFr.formatTime(stop.arrTime())), 0, r);
                inner.add(new Text(FormatterFr.formatTime(stop.depTime())), 1, r);
                inner.add(new Text(stop.stop().name()), 2, r++);
            }
            String title = t.intermediateStops().size() == 1 ?
                    "1 arrêt, " + FormatterFr.formatDuration(t.duration()) :
                    t.intermediateStops().size() + " arrêts, " + FormatterFr.formatDuration(t.duration());
            Accordion acc = new Accordion(new TitledPane(title, inner));
            gridPane.add(acc, 2, row, 2, 1);
        }
        ++row;

        // Arrivée
        Text arrTime = new Text(FormatterFr.formatTime(t.arrTime()));
        gridPane.add(arrTime, 0, row);
        Circle endCircle = new Circle(CIRCLE_RADIUS);
        circles.add(endCircle);
        gridPane.add(endCircle, 1, row);
        gridPane.add(new Text(t.arrStop().name()), 2, row);
        gridPane.add(new Text(FormatterFr.formatPlatformName(t.arrStop())), 3, row);

        return row;
    }

    // Méthode privée s'occupant du CSS
    private static String loadCSS(String cssPath) {
        try {
            return Objects.requireNonNull(DetailUI.class.getResource(cssPath)).toExternalForm();
        } catch (NullPointerException e) {
            System.err.println("Erreur de chargement CSS : " + cssPath);
            return "";
        }
    }

    // Méthode privée pour la gestion d'un clic sur le bouton carte
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


    // Méthode privée pour la gestion d'un clic sur le bouton calendrier
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
                        second.getBoundsInParent().getCenterY());

                // Modifications selon énoncé
                line.setStroke(javafx.scene.paint.Color.RED);
                line.setStrokeWidth(2);

                annotationsPane.getChildren().add(line);
            }
        }
    }
}

