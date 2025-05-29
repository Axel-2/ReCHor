package ch.epfl.rechor.gui;
import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;


/**
 * Représente la partie de l'interface graphique
 * qui montre la vue d'ensemble des voyages.
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    /**
     * Fonction dont le but est de créer le graphe de scène et de retourner
     * une instance de SummaryUI contenant sa racine ainsi que la valeur
     * observable contenant le voyage sélectionné
     * @param journeyList une liste de voyages
     * @param time le temps désiré
     * @return une instance de SummaryUI
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeyList, ObservableValue<LocalTime> time){

        // 2) --------------- Initialisation ---------------------
        // Création de notre listview à partir d'une liste observable
        ObservableList<Journey> buffer = FXCollections.observableArrayList();
        ListView<Journey> trueList = new ListView<>(buffer);

        journeyList.addListener((obs, oldList, newList) -> {
            buffer.setAll(newList == null ? List.of() : newList);
        });

        // 2) initialise le buffer une première fois
        if (journeyList.getValue() != null) {
            buffer.setAll(journeyList.getValue());
        }
        // On définit la formes de nos cells
        trueList.setCellFactory(JourneyCell::new);

        // 2) --------------- Sélections ---------------------
        // On sélectionne le bon quand la journey change
        journeyList.addListener((obs, oldJ, nJ) -> {
            Journey selectedJourney = selectedJourney(nJ, time.getValue());
            trueList.getSelectionModel().select(selectedJourney);
            trueList.scrollTo(selectedJourney);
        });

        // On sélectionne le bon quand le time change
        time.addListener((obs, oldT, nT) -> {
            Journey selectedJourney = selectedJourney(journeyList.getValue(), nT);
            trueList.getSelectionModel().select(selectedJourney);
            trueList.scrollTo(selectedJourney);
        });

        ObservableValue<Journey> userSelection = trueList
                .getSelectionModel()
                .selectedItemProperty();


        // 3) --------------- CSS ---------------------
        try {
            String cssPath = Objects.requireNonNull(
                    DetailUI.class.getResource("/summary.css") // Assure-toi que le chemin est correct
            ).toExternalForm();
            trueList.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Erreur chargement CSS detail.css: " + e.getMessage());
            // Gérer l'erreur ou l'ignorer si le CSS n'est pas critique pour ce test
        }

        // 4) --------------- Return ---------------------
        return new SummaryUI(trueList, userSelection);

    }

    private static Journey selectedJourney(List<Journey> jList, LocalTime t) {
        return jList.stream()
                .filter(j -> !j.depTime().toLocalTime().isBefore(t))
                .findFirst()
                .orElse(jList.getLast());
    }
}


/**
 * Classe qui représente une cellule affichant un voyage
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
class JourneyCell extends ListCell<Journey> {

    // Style class names
    private static final String STYLE_ROUTE = "route";
    private static final String STYLE_JOURNEY = "journey";
    private static final String STYLE_DEPARTURE = "departure";
    private static final String STYLE_ARRIVAL = "arrival";
    private static final String STYLE_DURATION = "duration";
    private static final String STYLE_DEP_ARR = "dep-arr";
    private static final String STYLE_TRANSFER = "transfer";

    // Magic numbers
    private final static int ICON_SIZE = 20;
    private final static int LINE_MARGIN = 5;
    private final static int CIRCLE_RADIUS = 3;
    private final static int GREY_LINE_STROKE = 2;

    // ---- Elements du graphe de scène ----
    // Haut
    private final HBox topBox = new HBox();
    private final Text directionText = new Text();
    private final ImageView icon = new ImageView();
    
    // Gauche
    private final Text depTimeText = new Text();

    // Droite
    private final Text arrTimeText = new Text(); 

    // Bas
    private final Text durationText = new Text(); // Bas
    private final HBox bottomBox = new HBox(); //
    private final Group circles = new Group();

    // Centre
    private final Pane centerPane = new Pane() {

         // "Collection" qui va contenir les cercles
        private final Line line = new Line();      // Ligne modifiée par la suite

        {
            int prefWidth = 0;
            int prefHeight = 0;
            setPrefSize(prefWidth, prefHeight);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(GREY_LINE_STROKE);
            getChildren().addAll(line, circles);
        }

        @Override
        protected void layoutChildren () {
            super.layoutChildren();

            // on récupère le voyage
            Journey journey = (Journey) getUserData();
            if (journey == null) return;

            // Display des lignes
            double lineStart_x = LINE_MARGIN;
            double lineEnd_x = getWidth() - LINE_MARGIN;
            double lineLength_x = lineEnd_x - lineStart_x;
            double y = getHeight() / 2;

            line.setStartX(lineStart_x);
            line.setEndX(lineEnd_x);
            line.setStartY(y);
            line.setEndY(y); // Pareil car ligne fine

            // Display des cercles
            double totalDuration = journey.duration().toMinutes(); // utile pour proportion

            circles.getChildren()
                    .forEach(node -> {
                        // Transtypages nécessaires à la récupération des données
                        if (node instanceof Circle) {
                            Circle circle = (Circle) node;
                            Object circleData = circle.getUserData();
                            if (circleData instanceof LocalDateTime) {
                                // Extraction des données temporelles
                                LocalDateTime circleTime = (LocalDateTime) circleData;
                                LocalDateTime startingTime = journey.depTime();

                                // Affichage du cercle à une position proportionelle à son apparition dans le voyage
                                double apparitionTime = Duration.between(startingTime, circleTime).toMinutes();
                                double proportion = apparitionTime / totalDuration;
                                double positionX = lineStart_x + lineLength_x * proportion;

                                // Centrages aux bonnes positions
                                circle.setCenterX(positionX);
                                circle.setCenterY(y);
                            }
                        }
                    });
        }
    };


    // Root
    private final BorderPane root = new BorderPane(centerPane, topBox, arrTimeText, bottomBox, depTimeText);

        // Constructeur
        public JourneyCell(ListView<Journey> journeyCellListView) {
            // Contenu de top Box (Ligne / Direction)
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);
            topBox.getChildren().addAll(icon, directionText);

            //Contenu de bottom Box (Durée)
            bottomBox.getChildren().add(durationText);

            // Styles
            topBox.getStyleClass().add(STYLE_ROUTE);
            root.getStyleClass().add(STYLE_JOURNEY);
            depTimeText.getStyleClass().add(STYLE_DEPARTURE);
            arrTimeText.getStyleClass().add(STYLE_ARRIVAL);
            bottomBox.getStyleClass().add(STYLE_DURATION);
    }

    @Override
    protected void updateItem(Journey journey, boolean empty) {
        super.updateItem(journey, empty);

        // si il n y a aucun journey on affiche rien
        if (journey == null || empty) {
            setText(null);
            setGraphic(null);
        }

        else {

            // Mise à jour des textes et des images
            updateData(journey);

            // Créations des cercles
            populateCircles(journey);

            // On stocke le voyage dans le pane, car on en a besoin à l'intérieur
            centerPane.setUserData(journey);

            // On affiche
            setGraphic(root);
        }
    }

    /**
     * Fonction qui sert à mettre à jour les noeuds qui existent déjà
     * @param journey un voyage
     */
    private void updateData(Journey journey) {

        // 1) On met à jour tous les texts
        depTimeText.setText(FormatterFr.formatTime(journey.depTime()));
        arrTimeText.setText(FormatterFr.formatTime(journey.arrTime()));
        durationText.setText(FormatterFr.formatDuration(journey.duration()));

        // 2) On cherche la première étape en véhicule et on sélectionne ses infos pour le texte
        // et l'icone.
        journey.legs().stream()
                // Ne garder que les étapes Transport
                .filter(leg -> leg instanceof Journey.Leg.Transport)
                // Caster en Transport
                .map(leg -> (Journey.Leg.Transport) leg)
                // Première occurrence
                .findFirst()
                // Si elle existe, faire les updates
                .ifPresent(firstTransportLeg -> {
                    directionText.setText(
                            FormatterFr.formatRouteDestination(firstTransportLeg)
                    );
                    icon.setImage(
                            VehicleIcons.iconFor(firstTransportLeg.vehicle())
                    );
                });
    }

    /**
     * Fonction qui sert à remplir la liste avec tous
     * les cercles nécessaires pour la ligne
     * @param journey un voyage
     */
    private void populateCircles(Journey journey) {
        circles.getChildren().clear(); // On efface l'affichage précédent

        // Cercle de départ
        Circle depCircle = new Circle(CIRCLE_RADIUS);
        depCircle.getStyleClass().add(STYLE_DEPARTURE);
        depCircle.setUserData(journey.depTime());
        circles.getChildren().add(depCircle);

        // Cercles intermédiaires
        journey.legs().stream()
                .filter(leg -> leg instanceof Journey.Leg.Foot)
                .map(leg -> (Journey.Leg.Foot) leg)
                .filter(footLeg -> !journey.legs().getFirst().equals(footLeg))
                .forEach(leg -> {
                    Circle intermediateCircle = new Circle(CIRCLE_RADIUS);
                    intermediateCircle.getStyleClass().add(STYLE_TRANSFER);
                    intermediateCircle.setUserData(leg.depTime());
                    circles.getChildren().add(intermediateCircle);
                });

        // Cercle d'arrivée
        Circle arrCircle = new Circle(CIRCLE_RADIUS);
        arrCircle.getStyleClass().add(STYLE_DEP_ARR);
        arrCircle.setUserData(journey.arrTime());
        circles.getChildren().add(arrCircle);

    }
}
