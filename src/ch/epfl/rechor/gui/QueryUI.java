package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO
) {

    private final static String CSS_PATH = "/query.css";




    public static QueryUI create(StopIndex stopIndex) {


        // Départ, échange et arrivée
        TextField depStop = new StopField();
        Button changeButton = new Button();
        TextField arrTextField = new TextField();

        // Date et heure
        DatePicker datePicker = new DatePicker();
        TextField hourTextField = new TextField();

        // TODO ca je suis pas trop sur de capter
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("H:mm");
        LocalTimeStringConverter timeStringConverter = new LocalTimeStringConverter(displayFormatter, parseFormatter);
        TextFormatter<LocalTime> textFormatter = new TextFormatter<>(timeStringConverter, LocalTime.of(1, 1));
        hourTextField.setTextFormatter(textFormatter);


        HBox mainBox = createMainBox(depStop, changeButton, arrTextField);
        HBox dateHourNode = createDateHourNode(datePicker, hourTextField);

        VBox rootNode = new VBox();
        rootNode.getChildren().addAll(
                mainBox,
                dateHourNode
        );
        rootNode.getStylesheets().add(loadCSS(CSS_PATH));

        stopIndex.stopsMatching("ss", 2);

        ObservableValue<String> depStopO = new SimpleObjectProperty<>();
        ObservableValue<String> arrStop0 = new SimpleObjectProperty<>();
        ObservableValue<LocalDate> date0 = new SimpleObjectProperty<>(LocalDate.of(2025, 10,1));
        ObservableValue<LocalTime> time0 = new SimpleObjectProperty<>(LocalTime.of(10, 10));

        depStopO.subscribe(depStop::setText);
        arrStop0.subscribe(arrTextField::setText);


        return new QueryUI(rootNode, depStopO, arrStop0, date0, time0);
    }


    public static HBox createMainBox(
            TextField depStop,
            Button changeButton,
            TextField arrTextField
            ) {

        HBox mainBox = new HBox();

        // Départ
        Label depLabel = new Label("Départ\\u202f:");
        depStop.setPromptText("Nom de l'arrêt de départ");
        depStop.setId("depStop");

        // Arrivée
        Label arrLabel = new Label("Arrivée\\u202f:");
        arrTextField.setPromptText("Nom de l'arrêt d'arrivée");


        mainBox.getChildren().addAll(
                depLabel,
                depStop,
                changeButton,
                arrLabel,
                arrTextField
        );

        return mainBox;
    }


    public static HBox createDateHourNode(DatePicker datePicker, TextField hourTextField) {

        HBox hBox = new HBox();
        // Date
        Label dateLabel = new Label("Date\\u202f:");
        datePicker.setId("date");

        // Heure
        Label hourLabeL = new Label("Heure\\u202f:");
        hourTextField.setId("time");

        hBox.getChildren().addAll(
                dateLabel,
                datePicker,
                hourLabeL,
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
