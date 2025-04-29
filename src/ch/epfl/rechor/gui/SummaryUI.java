package ch.epfl.rechor.gui;
import ch.epfl.rechor.journey.Journey;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

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


        // Ajout du CSS
        try {
            String cssPath = Objects.requireNonNull(
                    DetailUI.class.getResource("/summary.css") // Assure-toi que le chemin est correct
            ).toExternalForm();
            trueList.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Erreur chargement CSS detail.css: " + e.getMessage());
            // Gérer l'erreur ou l'ignorer si le CSS n'est pas critique pour ce test
        }

        return new SummaryUI(trueList, null);

    }
}

class JourneyCell extends ListCell<Journey> {

    // Elements du graphe de scène
    private final HBox topBox = new HBox();
    private final Pane centerPane = new Pane();
    private final HBox bottomBox = new HBox();
    private final Text depTimeText = new Text();
    private final Text arrTimeText = new Text();
    private final BorderPane root = new BorderPane(centerPane, topBox, arrTimeText, bottomBox, depTimeText);

    public JourneyCell(ListView<Journey> journeyCellListView) {
        // Todo à faire
    }

    @Override
    protected void updateItem(Journey journey, boolean empty){
        super.updateItem(journey, empty);
        if (journey == null || empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            setGraphic(root);
        }
    }
}

