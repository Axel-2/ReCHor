package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;

/**
 * Classe qui représente la combinaison d'un champ textuel et d'une fenêtre
 * @param textField champ textuel
 * @param stopO valeur observable contenant le nom de l'arrêt sélectionné
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {
    /**
     * Fonction qui crée un champ textuel et une fenêtre
     * @param stopIndex index des arrêts
     * @return une instance de StopField
     */
    public static StopField create(StopIndex stopIndex) {
        // ------------ Champ textuel et gestionnaire d'évènements associé ----------------
        TextField textField = new TextField();
        // Gestionnaire du clavier pour UP and DOWN
        textField.addEventHandler(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    switch(event.getCode()) {
                        case UP -> {
                            int currentSelectIndex = suggestions.getSelectionModel().getSelectedIndex();
                            if (currentSelectIndex > 0) { // Si on est pas tout en haut, on descend
                                suggestions.getSelectionModel().select(currentSelectIndex - 1);
                            }  else { // Si on est tout en haut, on va tout en bas
                                suggestions.getSelectionModel().select(suggestions.getItems().size() - 1);
                            }
                            event.consume();
                        }
                        case DOWN -> {
                            int currentSelectIndex = suggestions.getSelectionModel().getSelectedIndex();
                            if (currentSelectIndex < suggestions.getItems().size() - 1) { // Si on est pas tout en bas, on monte
                                suggestions.getSelectionModel().select(currentSelectIndex + 1);
                            } else { // Si on est tout en bas, on va tout en haut
                                suggestions.getSelectionModel().select(0);
                            }
                            event.consume();
                        }
                        default -> {
                            // On ne fait rien
                        }
                    }
                });


        // On a besoin de ce type pour utiliser .getReadOnlyProperty() après
        ReadOnlyStringWrapper stopNameWrapper = new ReadOnlyStringWrapper("");


        // --------------------- Pop Up et liste déroulante + configurations ----------------
        Popup popup = new Popup();
        popup.setHideOnEscape(false);

        ListView<String> suggestions = new ListView<>();
        suggestions.setFocusTraversable(false);
        suggestions.setMaxHeight(240);

        popup.getContent().add(suggestions); // ajout à la fenêtre

        // -------- Gestion de focus -------------

        textField.focusedProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    if (newValue) { // Signifie que le champ est actif
                        popup.show(); // Rend la fenêtre visible

                        // Mise à jour des suggestions en fonction de ce qui est écrit - Première observation
                        textField.textProperty().addListener(
                                (observable2, oldValue2, newValue2) -> {
                                    // On met à jour le nom de l'arrêt
                                    stopNameWrapper.set(newValue2);
                                    // On met à jour la liste des suggestions
                                    suggestions.getItems().clear();
                                    suggestions.getItems().addAll(stopIndex.stopsMatching(newValue2,5)); //TODO le 5 au bol
                                });

                        // Alignement de la fenêtre avec le textField - Deuxième observation
                        textField.boundsInLocalProperty().addListener(
                                (observable, oldBounds, newBounds) -> {
                                    // On transforme la valeur dans le système de coordonnées de la fenêtre
                                   Bounds bounds = textField.localToScreen(newBounds);
                                   // On positionne la fenêtre pour qu'elle soit alignée en bas du champ
                                   popup.setAnchorX(bounds.getMinX());
                                   popup.setAnchorY(bounds.getMaxY());
                                });
                    } else { // champ inactif
                        // Sinon : on observe plus, et la current selection et copié dans le txt field et dans la propriété text ?
                    }
                });
        // TODO : Avant de le transformer en property observable, il faut s'assurer que
        // TODO : stopNameWrapper est un nom de stop valide, ou une chaîne vide sinon
        return new StopField(textField, stopNameWrapper.getReadOnlyProperty());
    }

    /**
     * Associe un nom d'arrêt à textField
     * @param stopName nom d'arrêt à associer
     */
    public void setTo(String stopName){
        textField.setText(stopName);
    }
}
