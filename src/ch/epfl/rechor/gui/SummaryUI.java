package ch.epfl.rechor.gui;
import ch.epfl.rechor.gui.DetailUI;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.NoSuchElementException;


/**
 * Représente la partie de l'interface graphique
 * qui montre la vue d'ensemble des voyages.
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    public static SummaryUI create(ObservableValue())
}

