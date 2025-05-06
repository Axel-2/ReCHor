package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;

import java.util.List;

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
         final int SUGGESTION_NUMBER = 30;
        // ------------ Champ textuel et String wrapper ----------------
        TextField textField = new TextField();
        // On a besoin de ce type pour utiliser .getReadOnlyProperty() après
        ReadOnlyStringWrapper stopNameWrapper = new ReadOnlyStringWrapper("");
        // --------------------- Pop Up et liste déroulante + configurations ----------------
        Popup popup = new Popup();
        popup.setHideOnEscape(false);

        ListView<String> suggestions = new ListView<>();
        suggestions.setFocusTraversable(false);
        suggestions.setMaxHeight(240);

        popup.getContent().add(suggestions); // ajout à la fenêtre

        // -------- Lambda qui gère la selection d'un stopName --------
        Runnable select = () -> {
            // Récupération du nom actuellement sélectionné
            String selectedStopName = suggestions.getSelectionModel().getSelectedItem();

            // Mise à jours des textes
            stopNameWrapper.set(selectedStopName != null ? selectedStopName : ""); // "" en cas de null
            textField.setText(selectedStopName != null ? selectedStopName : ""); // "" en cas de null

            // Fermeture de la fenêtre
            popup.hide();
        };
        // ---------- Gestionnaire du clavier pour UP and DOWN ----------------
        textField.addEventHandler(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    switch(event.getCode()) {
                        case UP -> {
                            int currentSelectIndex = suggestions.getSelectionModel().getSelectedIndex();
                            if (currentSelectIndex > 0) { // Si on est pas tout en haut, on descend
                                suggestions.getSelectionModel().select(currentSelectIndex - 1);
                            }  else { // Si on est tout en haut, on va tout en bas
                                suggestions.getSelectionModel().selectLast();
                            }
                            event.consume();
                        }
                        case DOWN -> {
                            int currentSelectIndex = suggestions.getSelectionModel().getSelectedIndex();
                            if (currentSelectIndex < suggestions.getItems().size() - 1) { // Si on est pas tout en bas, on monte
                                suggestions.getSelectionModel().select(currentSelectIndex + 1);
                            } else { // Si on est tout en bas, on va tout en haut
                                suggestions.getSelectionModel().selectFirst();
                            }
                            event.consume();
                        }

                        default -> {
                            // On ne fait rien
                        }
                    }
                });


        // -------- Observers de textField -------------
        // TODO ici c'est pas encore bon

        // Suggestions
        textField.textProperty().addListener(
                (observable2, oldValue2, newValue2) -> {
                    // On met à jour la liste des suggestions
                    List<String> suggestionsList = stopIndex.stopsMatching(newValue2, SUGGESTION_NUMBER);
                    suggestions.getItems().clear();
                    suggestions.getItems().addAll(suggestionsList);

                    // MAJ de la popup
                    if(!suggestionsList.isEmpty()) {
                        suggestions.getSelectionModel().selectFirst();
                        if(!popup.isShowing()) {
                            popup.show(textField.getScene().getWindow()); // On affiche
                        }
                    } else {
                        popup.hide();
                    }

                });
        // Bounds
        textField.boundsInLocalProperty().addListener(
                (observable, oldBounds, newBounds) -> {
                    // On transforme la valeur dans le système de coordonnées de la fenêtre
                    Bounds bounds = textField.localToScreen(newBounds);
                    // On positionne la fenêtre pour qu'elle soit alignée en bas du champ
                    popup.setAnchorX(bounds.getMinX());
                    popup.setAnchorY(bounds.getMaxY());
                });
        // Focus
        textField.focusedProperty().addListener(
                (observable, oldFocus, newFocus) -> {
                    if (!newFocus) { // Signifie que le champ est inactif
                        select.run(); // On sélectionne le stopName
                    }
                });

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
