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
 * Classe qui représente la partie de l'interface graphique qui montre les détails d'un voyage.
 * Affiche les étapes du voyage avec leurs horaires, gares, et permet l'export carte/calendrier.
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record DetailUI(Node rootNode) {

    // Constantes CSS et identifiants
    private static final String
            DETAIL_CSS_PATH = "/detail.css",
            NO_JOURNEY_ID = "no-journey",
            DETAIL_ID = "detail",
            BUTTONS_ID = "buttons",
            LEGS_ID = "legs",
            ANNOTATIONS_ID = "annotations",
            DEPARTURE_LABEL = "departure",
            INTERMEDIATE_STOP_STYLE = "intermediate-stops";

    // Constantes d'interface
    private static final String
            NO_JOURNEY_TEXT = "Aucun voyage",
            MAP_BUTTON_TEXT = "Carte",
            CALENDAR_BUTTON_TEXT = "Calendrier";

    // Nombres magiques
    private static final int
            CIRCLE_RADIUS  = 3,
            COL_INDEX = 2,
            C_SPAN = 2,
            R_SPAN = 1,
            ICON_SIZE      = 31,
            STROKE_WIDTH   = 2;


    /**
     * Crée une instance de DetailUI avec mise à jour automatique selon le voyage observable
     * @param journeyObservableValue voyage observable à afficher
     * @return instance de DetailUI
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setId(DETAIL_ID);
        scrollPane.getStylesheets().add(loadCSS());

        // Configuration initiale et mise à jour automatique
        scrollPane.setContent(buildContent(journeyObservableValue.getValue()));
        journeyObservableValue.subscribe(() ->
                scrollPane.setContent(buildContent(journeyObservableValue.getValue())));

        return new DetailUI(scrollPane);
    }

    /**
     * Construit le contenu principal : soit "Aucun voyage" soit la vue détaillée du voyage.
     * @param journey voyage a afficher
     * @return le noeud correspondant
     */
    private static Node buildContent(Journey journey) {
        if (journey == null) {
            VBox noJourneyBox = new VBox(new Text(NO_JOURNEY_TEXT));
            noJourneyBox.setId(NO_JOURNEY_ID);
            return noJourneyBox;
        }

        // Construction de la vue complète avec étapes et boutons
        List<Circle> circles = new ArrayList<>();
        Pane annotationsPane = new Pane();
        annotationsPane.setId(ANNOTATIONS_ID);

        GridPane legsGrid = createLegsGrid(journey, annotationsPane, circles);
        HBox buttonsBox = createButtonsBox(journey);

        return new VBox(new StackPane(annotationsPane, legsGrid), buttonsBox);
    }

    /**
     * Crée la grille des étapes du voyage avec gestion des cercles pour les lignes de liaison.
     * Parcourt toutes les étapes et les ajoute selon leur type (pied/transport).
     */
    private static GridPane createLegsGrid(Journey journey, Pane annotationsPane, List<Circle> circles) {
        LineGridPane gridPane = new LineGridPane(annotationsPane, circles);
        gridPane.setId(LEGS_ID);

        int currentRow = 0;
        for (Journey.Leg leg : journey.legs()) {
            currentRow = switch (leg) {
                case Journey.Leg.Foot footLeg -> {
                    // Étape à pied : simple texte sur colonnes 2-3
                    Text walkText = new Text(FormatterFr.formatLeg(footLeg));
                    gridPane.add(
                            walkText,
                            COL_INDEX,
                            currentRow,
                            C_SPAN,
                            R_SPAN
                    );
                    yield currentRow;
                }
                case Journey.Leg.Transport transportLeg ->
                        addTransportLeg(gridPane, circles, transportLeg, currentRow);
            };
            currentRow++;
        }
        return gridPane;
    }

    /**
     * Ajoute une étape de transport complète à la grille (départ, icône, arrêts intermédiaires, arrivée).
     * Gère l'alignement sur 4 colonnes et la création des cercles pour les lignes de liaison.
     */
    private static int addTransportLeg(GridPane gridPane, List<Circle> circles,
                                       Journey.Leg.Transport transport, int row) {
        // Ligne de départ avec heure, cercle, gare, voie/quai
        Text depTime = new Text(FormatterFr.formatTime(transport.depTime()));
        depTime.getStyleClass().add(DEPARTURE_LABEL);

        Circle startCircle = new Circle(CIRCLE_RADIUS);
        circles.add(startCircle);

        Text depStopName = new Text(transport.depStop().name());
        Text depPlatform = new Text(FormatterFr.formatPlatformName(transport.depStop()));

        depPlatform.getStyleClass().add(DEPARTURE_LABEL);
        gridPane.addRow(row++, depTime, startCircle, depStopName, depPlatform);

        // Ligne avec icône véhicule (peut s'étendre sur 2 lignes) et destination
        ImageView icon = new ImageView(VehicleIcons.iconFor(transport.vehicle()));
        icon.setFitWidth(ICON_SIZE);
        icon.setFitHeight(ICON_SIZE);
        Text routeDestination = new Text(FormatterFr.formatRouteDestination(transport));

        int iconRowSpan = transport.intermediateStops().isEmpty() ? 1 : 2;
        gridPane.add(icon, COL_INDEX-2, row, C_SPAN-1, iconRowSpan);
        gridPane.add(routeDestination, COL_INDEX, row++, C_SPAN, R_SPAN);

        // Arrêts intermédiaires optionnels dans un accordéon
        if (!transport.intermediateStops().isEmpty()) {
            GridPane innerGrid = new GridPane();
            innerGrid.getStyleClass().add(INTERMEDIATE_STOP_STYLE);

            int innerRow = 0;
            for (Journey.Leg.IntermediateStop stop : transport.intermediateStops()) {
                innerGrid.addRow(innerRow++,
                        new Text(FormatterFr.formatTime(stop.arrTime())),
                        new Text(FormatterFr.formatTime(stop.depTime())),
                        new Text(stop.stop().name()));
            }

            // Titre de l'accordéon avec nombre d'arrêts et durée
            int stopCount = transport.intermediateStops().size();
            String title = String.format("%d %s, %s", stopCount,
                    stopCount == 1 ? "arrêt" : "arrêts",
                    FormatterFr.formatDuration(transport.duration()));

            Accordion accordion = new Accordion(new TitledPane(title, innerGrid));
            gridPane.add(accordion, COL_INDEX, row++, C_SPAN, R_SPAN);
        }

        // Ligne d'arrivée avec heure, cercle, gare, voie/quai
        Circle endCircle = new Circle(CIRCLE_RADIUS);
        circles.add(endCircle);
        gridPane.addRow(row,
                new Text(FormatterFr.formatTime(transport.arrTime())),
                endCircle,
                new Text(transport.arrStop().name()),
                new Text(FormatterFr.formatPlatformName(transport.arrStop())));

        return row;
    }

    /**
     * Crée la boîte des boutons avec gestionnaires d'événements pour carte et calendrier.
     * Configure les actions d'ouverture de navigateur et sauvegarde de fichier.
     */
    private static HBox createButtonsBox(Journey journey) {
        Button mapButton = new Button(MAP_BUTTON_TEXT);
        Button calendarButton = new Button(CALENDAR_BUTTON_TEXT);

        mapButton.setOnAction(e -> openMap(journey));
        calendarButton.setOnAction(e -> saveCalendar(journey, calendarButton));

        HBox buttonsBox = new HBox(mapButton, calendarButton);
        buttonsBox.setId(BUTTONS_ID);
        return buttonsBox;
    }

    /**
     * Charge la feuille de style CSS depuis les ressources.
     * Gère les erreurs de chargement avec message d'erreur approprié.
     */
    private static String loadCSS() {
        try {
            return Objects.requireNonNull(DetailUI.class.getResource(DETAIL_CSS_PATH)).toExternalForm();
        } catch (NullPointerException e) {
            System.err.printf("Erreur de chargement CSS : %s%n", DETAIL_CSS_PATH);
            return "";
        }
    }

    /**
     * Ouvre la carte du voyage dans le navigateur via uMap avec données GeoJSON.
     * Construit l'URL avec paramètre de requête contenant les coordonnées du trajet.
     */
    private static void openMap(Journey journey) {
        try {
            URI url = new URI(
                    "https", "umap.osm.ch", "/fr/map",
                    "data=" + JourneyGeoJsonConverter.toGeoJson(journey), null);
            getDesktop().browse(url);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du navigateur : " + e.getMessage());
        }
    }

    /**
     * Sauvegarde le voyage au format iCalendar
     * Propose un nom par défaut basé sur la date et gère l'annulation utilisateur
     */
    private static void saveCalendar(Journey journey, Node parentNode) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(String.format("voyage_%s.ics", LocalDate.now()));

            File file = fileChooser.showSaveDialog(parentNode.getScene().getWindow());
            if (file != null) {
                String calendarContent = JourneyIcalConverter.toIcalendar(journey);
                Files.writeString(file.toPath(), calendarContent, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du calendrier : " + e.getMessage());
        }
    }

    /**
     * GridPane personnalisée qui dessine automatiquement les lignes rouges entre paires de cercles.
     * Redéfinit layoutChildren pour calculer les positions après mise en page et créer les lignes.
     */
    private static class LineGridPane extends GridPane {
        private final Pane annotationsPane;
        private final List<Circle> circles;

        public LineGridPane(Pane annotationsPane, List<Circle> circles) {
            this.annotationsPane = annotationsPane;
            this.circles = circles;
        }

        /**
         * Effectue la mise en page puis dessine les lignes entre paires de cercles.
         * Nettoie d'abord les anciennes lignes puis crée les nouvelles selon les positions calculées.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            annotationsPane.getChildren().clear();

            // Création des lignes rouges reliant les paires de cercles (départ-arrivée)
            for (int i = 0; i < circles.size(); i += 2) {
                if (i + 1 < circles.size()) {
                    Circle start = circles.get(i), end = circles.get(i + 1);

                    Line line = new Line(
                            start.getBoundsInParent().getCenterX(), start.getBoundsInParent().getCenterY(),
                            end.getBoundsInParent().getCenterX(), end.getBoundsInParent().getCenterY());

                    line.setStroke(javafx.scene.paint.Color.RED);
                    line.setStrokeWidth(STROKE_WIDTH);
                    annotationsPane.getChildren().add(line);
                }
            }
        }
    }
}