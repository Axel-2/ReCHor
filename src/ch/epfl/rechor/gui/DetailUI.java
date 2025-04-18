package ch.epfl.rechor.gui; // Mettre dans le bon package

import ch.epfl.rechor.Json; // Importer si nécessaire pour Journey plus tard
import ch.epfl.rechor.journey.Journey; // Importer Journey
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle; // Importer pour plus tard
import javafx.scene.shape.Line;   // Importer pour plus tard
import javafx.scene.text.Text;
import javafx.beans.property.SimpleObjectProperty; // Pour le test initial

import java.util.Objects; // Pour gérer le chemin CSS

/**
 * Classe qui représente la partie de l'interface
 * graphique qui montre les détails d'un voyage
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record DetailUI(Node rootNode) {

    public static final String NOJOURNEYID = "no-journey";

    /**
     * Fonction dont le but est de créer le graphe de
     * scène et de retourner une instance de DetailUI
     * @return une scène détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {

        // Noeud principal
        ScrollPane scrollPane= new ScrollPane();

        StackPane stackPane = new StackPane();
        scrollPane.setContent(stackPane);

        VBox noJourney = new VBox();
        noJourney.setId(NOJOURNEYID);
        VBox existJourney = new VBox();



        GridPane gridPane = new GridPane();





        // colonne 0: heures de départ et d'arrivée, et icône du véhicule
       // GridPane.setRowIndex(,0 );


        // colonne 1: cercles de départ et d'arrivée
       // GridPane.setRowIndex(,1);

        // colonne 2: nom de la gare de départ et d'arrivée
       // GridPane.setRowIndex(,2);

        // colonne 3: voie/quai de départ et d'arrivée
        //GridPane.setRowIndex(,3);



        return new DetailUI(scrollPane);
    }
}
