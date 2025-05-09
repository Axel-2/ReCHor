package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.timetable.Platforms;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//
//        // Chargement des données horaires
//        TimeTable timeTable = FileTimeTable.in(Path.of("timetable"));
//        Profile profile = new Profile()
//
//        Stations stations = timeTable.stations();
//        List<String> stopsLists = IntStream.range(0, stations.size())
//                .mapToObj(stations::name)
//                .toList();
//
//        StationAliases aliases = timeTable.stationAliases();
//        Map<String, String> alternatesNamesMap = IntStream.range(0, aliases.size())
//                .boxed()
//                .collect(Collectors.toMap(
//                        aliases::alias,
//                        aliases::stationName
//                ));
//
//        // Création de QueryUI
//        StopIndex stopIndex = new StopIndex(stopsLists, alternatesNamesMap);
//        QueryUI queryUI = QueryUI.create(stopIndex);
//
//        // TODO
//        ObservableValue<List<Journey>> journeyList = Bindings.createObjectBinding(
//                ()-> {
//                    return JourneyExtractor.journeys()
//                },
//                queryUI
//        );
//        ObservableValue<LocalTime> time = null;
//        ObservableValue<Journey> currentJourney = null;
//
//
//        SummaryUI summaryUI = SummaryUI.create(journeyList, time);
//        DetailUI detailUI = DetailUI.create(currentJourney);
//
//        // SplitPane
//        SplitPane splitPane = new SplitPane();
//        splitPane.getItems().addAll(summaryUI.rootNode(), detailUI.rootNode());
//
//        //BorderPane
//        BorderPane borderPane = new BorderPane();
//        borderPane.setCenter(splitPane);
//        borderPane.setTop(queryUI.rootNode());
//
//        // Scene
//        Scene scene = new Scene(borderPane);
//
//        // Mise en place de la fenêtre
//        primaryStage.setScene(scene);
//        primaryStage.setMinHeight(600);
//        primaryStage.setMinWidth(800);
//        primaryStage.setTitle("ReCHor");
//        primaryStage.show();
//        Platform.runLater(() -> scene.lookup("#depstop").requestFocus());

    }

}
