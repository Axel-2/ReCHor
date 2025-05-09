package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.util.Subscription;

import java.util.ArrayList;
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
        ArrayList<Subscription> subscriptions = new ArrayList<>();
        textField.focusedProperty().subscribe(value -> {
            if (value) {
                // On ajoute les auditeurs
                subscriptions.add(textField.textProperty().subscribe(value2 -> {
                    // On met à jour la liste des suggestions
                    List<String> suggestionsList = stopIndex.stopsMatching(value2, SUGGESTION_NUMBER);
                    suggestions.getItems().clear();
                    suggestions.getItems().addAll(suggestionsList);

                    // MAJ de la popup
                    if(!suggestionsList.isEmpty()) {
                        suggestions.getSelectionModel().selectFirst();
                        if(!popup.isShowing()) {
                            popup.show(textField.getScene().getWindow()); // On affiche
                        }
                    }
                }));
                subscriptions.add(textField.boundsInLocalProperty().subscribe(value3 -> {
                    // On met à jour la position de la popup
                    Bounds bounds = textField.localToScreen(value3);
                    popup.setAnchorX(bounds.getMinX());
                    popup.setAnchorY(bounds.getMaxY());
                }));
            } else {
                // On désabonne les auditeurs
                subscriptions.forEach(Subscription::unsubscribe);
                subscriptions.clear();
                popup.hide();
                // Récupération de l'arrêt sélectionné
                String selectedStopName = suggestions.getSelectionModel().getSelectedItem();
                // Mise à jour de la propriété et du wrapper
                stopNameWrapper.set(selectedStopName != null ? selectedStopName : ""); // "" en cas de null
                textField.setText(selectedStopName != null ? selectedStopName : ""); // "" en cas de null
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
