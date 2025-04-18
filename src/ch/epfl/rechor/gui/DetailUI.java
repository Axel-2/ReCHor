package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

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

        

    }
}
