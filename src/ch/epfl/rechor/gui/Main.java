package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Classe principale du projet, qui initialise l'interface graphique
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public class Main extends Application {

    // Attribut de classe : liste des voyages observables
    private static ObservableValue<List<Journey>> journeyList;

    public static void main(String[] args) {
        launch(args);
    }

    // Cache du profil
    private  record ProfileKey(LocalDate date, int arrivalId) {}
    private final Map<ProfileKey, Profile> profileCache = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {

        // ----------- Chargement des données horaires ---------------------
        TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));
        Router router = new Router(timeTable);

        Stations stations = timeTable.stations();
        List<String> stopsLists = IntStream.range(0, stations.size())
                .mapToObj(stations::name)
                .toList();

        StationAliases aliases = timeTable.stationAliases();
        Map<String, String> alternatesNamesMap = IntStream.range(0, aliases.size())
                .boxed()
                .collect(Collectors.toMap(
                        aliases::alias,
                        aliases::stationName
                ));

        StopIndex stopIndex = new StopIndex(stopsLists, alternatesNamesMap);

        // ---------------- UI -------------------------
        QueryUI queryUI = QueryUI.create(stopIndex); // 1)

        // Mise à jour de la liste des voyages
        journeyList = Bindings.createObjectBinding(
                ()-> {

                    LocalDate date = queryUI.dateO().getValue();
                    String depStop = queryUI.depStopO().getValue();
                    String arrStop = queryUI.arrStopO().getValue();

                    if (depStop.isEmpty() || arrStop.isEmpty()) {
                        return Collections.emptyList();
                    }

                    int arrId = stationId(timeTable, arrStop);

                    ProfileKey profileKey = new ProfileKey(date, arrId);
                    Profile profile = profileCache.computeIfAbsent(
                            profileKey,

                            k -> router.profile(
                                    date,
                                    arrId
                            )
                    );

                    return JourneyExtractor.journeys(
                                profile,
                                stationId(timeTable, queryUI.depStopO().getValue()
                                )
                        );

                },
                queryUI.dateO(),
                queryUI.depStopO(),
                queryUI.arrStopO()
        );


        SummaryUI summaryUI = SummaryUI.create(journeyList, queryUI.timeO()); // 2)
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO()); // 3)

        // SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(summaryUI.rootNode(), detailUI.rootNode());

        // BorderPane
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(splitPane);
        borderPane.setTop(queryUI.rootNode());

        // Scene
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setTitle("ReCHor");
        primaryStage.show();

        if (!journeyList.getValue().isEmpty()) {
            Platform.runLater(() -> scene.lookup("#depStop").requestFocus());
        }

    }

    // Méthode privée nous permettant d'avoir l'id d'une station à partir d'un nom et de l'horaire
    private static int stationId(TimeTable timeTable, String name) {
        var stations = timeTable.stations();
        for (var i = 0; i < stations.size(); i += 1)
            if (stations.name(i).equals(name)) return i;
        throw new NoSuchElementException();
    }

}
