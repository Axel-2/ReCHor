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

    private static final String LABEL_TEXT_DEP    = "Départ\u202f:";
    private static final String LABEL_TEXT_ARR    = "Arrivée\u202f:";
    private static final String LABEL_TEXT_DATE   = "Date\u202f:";
    private static final String LABEL_TEXT_HOUR   = "Heure\u202f:";

    private static final String ID_DEP_FIELD      = "depStop";
    private static final String ID_DATE_PICKER    = "date";
    private static final String ID_TIME_FIELD     = "time";

    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter PARSE_TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private static final String SWAP_BUTTON_TEXT  = "↔";
    private static final String PROMPT_TEXT_DEP   = "Nom de l'arrêt de départ";
    private static final String PROMPT_TEXT_ARR   = "Nom de l'arrêt d'arrivée";

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
        LocalTimeStringConverter timeStringConverter = new LocalTimeStringConverter(
                DISPLAY_TIME_FORMAT,
                PARSE_TIME_FORMAT
        );
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
        changeButton.setText(SWAP_BUTTON_TEXT);
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
        Label depLabel = new Label(LABEL_TEXT_DEP);
        depStop.textField().setPromptText(PROMPT_TEXT_DEP);
        depStop.textField().setId(ID_DEP_FIELD);

        // Arrivée
        Label arrLabel = new Label(LABEL_TEXT_ARR);
        arrTextField.textField().setPromptText(PROMPT_TEXT_ARR);

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
        Label dateLabel = new Label(LABEL_TEXT_DATE);
        datePicker.setId(ID_DATE_PICKER);

        // Heure
        Label hourLabel = new Label(LABEL_TEXT_HOUR);
        hourTextField.setId(ID_TIME_FIELD);

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
