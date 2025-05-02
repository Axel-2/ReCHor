package ch.epfl.rechor.gui;
import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

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
public record SummaryUI(Node rootNode, ObservableValue<String> selectedJourneyO) {

    /**
     * Fonction dont le but est de créer le graphe de scène et de retourner
     * une instance de SummaryUI contenant sa racine ainsi que la valeur
     * observable contenant le voyage sélectionné
     * @param journeyList
     * @param time
     * @return
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeyList, ObservableValue<LocalTime> time){

        // Création d'une liste observable de journey vide
        ObservableList<Journey> buffer = FXCollections.observableArrayList();

        // La liste view contient les données du buffer
        ListView<Journey> trueList = new ListView<>(buffer);

        // On met à jour le buffer automatiquement à chaque fois qu'un nouveau voyage est cherché
        // donc quand journeyList change
        journeyList.subscribe((newList) -> buffer.setAll(newList));

        // On définit la formes de nos cells
        trueList.setCellFactory(JourneyCell::new);

        // On utilise un ObjectBinding car le voyage seléctionné dépend
        // d'un changement du temps et/ouu d'un changement de la liste de voyages.
        // Donc la lambda à l'intérieur du lien est executée à chaque changement
        // de la variable journeyList ou de time ce qui nous garanti d'avoir
        // tout le temps le bon voyage séléctionné
        ObjectBinding<Journey> currentSelectedJourney = Bindings.createObjectBinding(
                () -> {
                    // Lorsque l'heure de voyage désirée change, le premier voyage
                    // partant à cette heure-là, ou plus tard, est sélectionné dans la liste.
                    // S'il n'y en a aucun, alors le dernier voyage de la liste est sélectionné.
                    List<Journey> currentJourneyList = journeyList.getValue();
                    LocalTime currentTime = time.getValue();
                    if (currentJourneyList == null || currentJourneyList.isEmpty() || currentTime == null) {
                        // on ne peut sélectionner un voyage que si les arguments sont valides
                        return null;
                    }

                    return currentJourneyList.stream()
                            // on ne garde que les yoages qui sont après le temps sélectionnés
                            .filter(journey -> !journey.depTime().toLocalTime().isBefore(currentTime))
                            // on prend le premier s'il y en a un
                            .findFirst()
                            // sinon, on prend le dernier
                            .orElse(currentJourneyList.getLast());
                },
                journeyList,
                time
        );

        // Ensuite, dès que le voyage séléctionné change,
        // on change la séléction dans l'interface
        currentSelectedJourney.subscribe(
                currentJourney -> trueList.getSelectionModel().select(currentJourney)
        );


        // Ajout du fichier CSS
        try {
            String cssPath = Objects.requireNonNull(
                    DetailUI.class.getResource("/summary.css") // Assure-toi que le chemin est correct
            ).toExternalForm();
            trueList.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Erreur chargement CSS detail.css: " + e.getMessage());
            // Gérer l'erreur ou l'ignorer si le CSS n'est pas critique pour ce test
        }

        return new SummaryUI(trueList, currentSelectedJourney);

    }
}

class JourneyCell extends ListCell<Journey> {

    private final static int ICON_SIZE = 20;

    // Elements du graphe de scène

    private final Text depTimeText = new Text();
    private final Text arrTimeText = new Text();

    // Ligne / direction
    private final HBox topBox = new HBox();
    private final Text directionText = new Text();
    private final ImageView icon = new ImageView();

    // Durée
    private final Text durationText = new Text();
    private final HBox bottomBox = new HBox();

    // Cercles
    private final static int CIRCLE_RADIUS = 3;
    private final Group circles = new Group();

    // Notre Pane customisée
    private final Pane centerPane = new Pane() {
        private final Line line = new Line();

        {
            setPrefSize(0, 0);
            getChildren().addAll(line, circles); // il contient les lignes et cercles
        }

        @Override
        protected void layoutChildren () {
            super.layoutChildren();

            // on récupère le voyage
            Journey journey = (Journey) getUserData();
            if (journey == null) return;

            // Display des lignesl
            double lineStart_x = 5.0; // Todo magic numbers
            double lineEnd_x = getWidth() - 5.0;
            double lineLength_x = lineEnd_x - lineStart_x;
            double y = getHeight() / 2;

            line.setStartX(lineStart_x);
            line.setEndX(lineEnd_x);
            line.setStartY(y);
            line.setEndY(y); // Pareil car ligne fine

            // Display des cercles
            double totalDuration = journey.duration().toMinutes(); //utile pour proportion

            circles.getChildren()
                    .forEach(node -> {

                                if (node instanceof Circle circle && circle.getUserData() instanceof LocalDateTime time) {
                                    System.out.println("test");
                                    double apparitionTime = java.time.Duration.between(journey.depTime().toLocalTime(), time).toMinutes();
                                    double proportion = apparitionTime / totalDuration;
                                    double positionX = lineStart_x + lineLength_x * proportion;
                                    circle.setCenterX(positionX);
                                    circle.setCenterY(y);
                                }
                            }
                    );

        }

    };


    // Changements
    private final BorderPane root = new BorderPane(centerPane, topBox, arrTimeText, bottomBox, depTimeText);

    public JourneyCell(ListView<Journey> journeyCellListView) {
        // Contenu de top Box (Ligne / Direction)
        icon.setFitWidth(ICON_SIZE);
        icon.setFitHeight(ICON_SIZE);
        topBox.getChildren().addAll(icon, directionText);

        //Contenu de bottom Box (Durée)
        bottomBox.getChildren().add(durationText);

        // Styles
        topBox.getStyleClass().add("route");
        root.getStyleClass().add("journey");
        depTimeText.getStyleClass().add("departure");
        arrTimeText.getStyleClass().add("arrival");
        bottomBox.getStyleClass().add("duration");
    }

    @Override
    protected void updateItem(Journey journey, boolean empty) {
        // doit impérativement être appelée
        super.updateItem(journey, empty);
        // si il n y a aucun journey on affiche rien
        if (journey == null || empty) {
            setText(null);
            setGraphic(null);
        }
        else {

            // On remplit nos noeuds avec les données du voyage
            updateData(journey);

            // On crée tous les cercles nécessaires
            populateCircles(journey);

            centerPane.setUserData(journey);

            // On affiche !
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
                // Si elle existe, faire tes updates
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
     * @param journey
     */
    private void populateCircles(Journey journey) {
        // 3) Dessin des lignes et cercles
        circles.getChildren().clear(); // On efface l'affichage précédent

        // Cercle de départ
        Circle depCircle = new Circle(CIRCLE_RADIUS);
        depCircle.getStyleClass().add("dep-arr");
        depCircle.setUserData(journey.depTime());
        circles.getChildren().add(depCircle);

        // Cercles intermédiaires
        journey.legs().stream()
                .filter(leg -> leg instanceof Journey.Leg.Foot)
                .map(leg -> (Journey.Leg.Foot) leg)
                .forEach(leg -> {
                    Circle intermediateCircle = new Circle(CIRCLE_RADIUS);
                    intermediateCircle.getStyleClass().add("transfer");
                    intermediateCircle.setUserData(leg.depTime());
                    circles.getChildren().add(intermediateCircle);
                });

        // Cercle d'arrivée
        Circle arrCircle = new Circle(CIRCLE_RADIUS);
        arrCircle.getStyleClass().add("dep-arr");
        arrCircle.setUserData(journey.arrTime());
        circles.getChildren().add(arrCircle);

    }
}
