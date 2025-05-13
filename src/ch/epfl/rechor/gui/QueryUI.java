package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Classe qui représente l'interface utilisateur de la requête
 * @param rootNode (Noeud racine)
 * @param depStopO (Arrêt de départ observable
 * @param arrStopO (Arrêt d'arrivée observable)
 * @param dateO (Date observable)
 * @param timeO (Temps observable)
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO
) {

    private final static String CSS_PATH = "/query.css";

    /**
     * Fonction qui crée l'interface utilisateur de la requête
     * @param stopIndex index des arrêts
     * @return une instance de QueryUI
     */
    public static QueryUI create(StopIndex stopIndex) {
        // Départ, échange et arrivée
        StopField depStopField =  StopField.create(stopIndex);
        Button changeButton = new Button();
        StopField arrStopField = StopField.create(stopIndex);

        // Date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField hourTextField = new TextField();

        // Formatage
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("H:mm");
        LocalTimeStringConverter timeStringConverter = new LocalTimeStringConverter(displayFormatter, parseFormatter);
        TextFormatter<LocalTime> textFormatter = new TextFormatter<>(timeStringConverter, LocalTime.now());
        hourTextField.setTextFormatter(textFormatter);

        HBox mainBox = createMainBox(depStopField, changeButton, arrStopField);
        HBox dateHourNode = createDateHourNode(datePicker, hourTextField);

        //  ------------- Logique observable ---------------
        // Extraction des valeurs observables
        ObservableValue<String> depStopO = depStopField.stopO();
        ObservableValue<String> arrStopO = arrStopField.stopO();
        ObservableValue<LocalDate> dateO = datePicker.valueProperty();
        ObservableValue<LocalTime> timeO = textFormatter.valueProperty();

        // Logique du bouton
        changeButton.setText("↔");
        changeButton.setOnAction(e -> {
                    String d = depStopO.getValue();
                    String a = arrStopO.getValue();
                    depStopField.setTo(a);
                    arrStopField.setTo(d);
                }
        );

        // Création du nœud final
        VBox rootNode = new VBox();
        rootNode.getChildren().addAll(
                mainBox,
                dateHourNode
        );
        rootNode.getStylesheets().add(loadCSS(CSS_PATH));

        return new QueryUI(rootNode, depStopO, arrStopO, dateO, timeO);
    }


    public static HBox createMainBox(
            StopField depStop,
            Button changeButton,
            StopField arrTextField
            ) {

        HBox mainBox = new HBox();

        // Départ
        Label depLabel = new Label("Départ\u202f:");
        depStop.textField().setPromptText("Nom de l'arrêt de départ");
        depStop.textField().setId("depStop");

        // Arrivée
        Label arrLabel = new Label("Arrivée\u202f:");
        arrTextField.textField().setPromptText("Nom de l'arrêt d'arrivée");

        // Ajout du contenu
        mainBox.getChildren().addAll(
                depLabel,
                depStop.textField(),
                changeButton,
                arrLabel,
                arrTextField.textField()
        );

        return mainBox;
    }


    private static HBox createDateHourNode(DatePicker datePicker, TextField hourTextField) {

        HBox hBox = new HBox();
        // Date
        Label dateLabel = new Label("Date\u202f:");
        datePicker.setId("date");

        // Heure
        Label hourLabel = new Label("Heure\u202f:");
        hourTextField.setId("time");

        hBox.getChildren().addAll(
                dateLabel,
                datePicker,
                hourLabel,
                hourTextField
        );

        return hBox;
    }


    private static String loadCSS(String cssPath) {
        try {
            return Objects.requireNonNull(QueryUI.class.getResource(cssPath)).toExternalForm();
        } catch (NullPointerException e) {
            System.err.println("Erreur de chargement CSS : " + cssPath);
            return "";
        }
    }
}
