package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 * Classe qui représente la partie de l'interface
 * graphique qui montre les détails d'un voyage
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record DetailUI(Node rootNode) {


    /**
     * Fonction dont le but est de créer le graphe de
     * scène et de retourner une instance de DetailUI
     * @return une scène détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {

<<<<<<< HEAD
        GridPane gridPane = new GridPane();
=======
        return GridPane();



>>>>>>> 867ece0595b56bd7a61f0b6263a591695781236b

        // colonne 0: heures de départ et d'arrivée, et icône du véhicule
       // GridPane.setRowIndex(,0 );


        // colonne 1: cercles de départ et d'arrivée
       // GridPane.setRowIndex(,1);

        // colonne 2: nom de la gare de départ et d'arrivée
       // GridPane.setRowIndex(,2);

        // colonne 3: voie/quai de départ et d'arrivée
        //GridPane.setRowIndex(,3);



        return new DetailUI(gridPane);
    }
}
