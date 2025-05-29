package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    private static final int SUGGESTION_NUMBER = 30; // Nombre de résultats proposés
    private static final int MAX_HEIGHT = 240;

    /**
     * Fonction qui crée un champ textuel et une fenêtre
     * @param stopIndex index des arrêts
     * @return une instance de StopField
     */
    public static StopField create(StopIndex stopIndex) {
        // ------------ Champ textuel et String Property ----------------
        final TextField textField = new TextField();
        Property<String> stringProperty = new SimpleObjectProperty<>("");
        // --------------------- Pop Up et liste déroulante + configurations ----------------
        final Popup popup = new Popup();
        popup.setHideOnEscape(false);

        ListView<String> suggestions = new ListView<>();
        suggestions.setFocusTraversable(false);
        suggestions.setMaxHeight(MAX_HEIGHT);

        popup.getContent().add(suggestions); // ajout à la fenêtre

        // -------- Lambda qui gère la selection d'un stopName --------
        Runnable select = () -> {
            String selectedStopName = suggestions.getSelectionModel().getSelectedItem();
            String value = selectedStopName != null ? selectedStopName : "";
            stringProperty.setValue(value);
            textField.setText(value);
            popup.hide();
        };

        // --------- Gestionnaire des events souris -----------
        suggestions.setOnMouseClicked((MouseEvent me) -> {
            if (me.getButton() == MouseButton.PRIMARY && me.getClickCount() == 1) {
                select.run();
                me.consume();
            }
        });

        // ---------- Gestionnaire des events clavier ----------------
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DOWN, UP -> {
                    MultipleSelectionModel<String> model = suggestions.getSelectionModel();
                    int size  = suggestions.getItems().size();
                    int currentIndex = model.getSelectedIndex();
                    // +1 pour DOWN, –1 pour UP, avec rebouclage via modulo
                    int step  = event.getCode() == KeyCode.DOWN ? 1 : -1;
                    int next  = (currentIndex + step + size) % size;

                    model.select(next);
                    suggestions.scrollTo(next);
                    event.consume();
                }
                case ENTER, ESCAPE -> select.run();
                default -> {
                    // rien à faire
                }
            }
        });



        // -------- Observers de textField -------------
        ArrayList<Subscription> subscriptions = new ArrayList<>();
        textField.focusedProperty().subscribe(focusOn -> {
            if (focusOn) {
                // On ajoute les auditeurs
                subscriptions.add(textField.textProperty().subscribe(newText -> {
                    // On met à jour la liste des suggestions
                    List<String> suggestionsList = stopIndex.stopsMatching(newText, SUGGESTION_NUMBER);
                    suggestions.getItems().clear();
                    suggestions.getItems().addAll(suggestionsList);

                    // MAJ de la popup
                    if (!suggestionsList.isEmpty()) {
                        suggestions.getSelectionModel().selectFirst();
                        if (!popup.isShowing()) {
                            popup.show(textField.getScene().getWindow()); // On affiche
                        }
                    }
                }));
                subscriptions.add(textField.boundsInLocalProperty().subscribe(newBounds -> {
                    // On met à jour la position de la popup
                    Bounds bounds = textField.localToScreen(newBounds);
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
                stringProperty.setValue(selectedStopName != null ? selectedStopName : ""); // "" en cas de null
                textField.setText(selectedStopName != null ? selectedStopName : ""); // "" en cas de null
            }
        });

        return new StopField(textField, stringProperty);
    }

    /**
     * Associe un nom d'arrêt à textField
     * @param stopName nom d'arrêt à associer
     */
    public void setTo(String stopName){
        textField.setText(stopName);
        if (stopO instanceof ObjectProperty<String>){
             ((ObjectProperty<String>) stopO).setValue(stopName);
        }
    }
}
