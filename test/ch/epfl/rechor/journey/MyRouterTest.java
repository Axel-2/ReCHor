package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MyRouterTest {

    private FileTimeTable fileTimeTable;
    private final Path testDirectory = Path.of("timetable");
    private final LocalDate testDate = LocalDate.of(2025, 3, 17);

    @BeforeEach
    void setUp() throws IOException {
        // Crée le TimeTable à partir des fichiers contenus dans le dossier "timetable"
        fileTimeTable = (FileTimeTable) FileTimeTable.in(testDirectory);
    }


    @Test
    void checkProfile() {
        Router router = new Router(fileTimeTable);
        router.profile(testDate, 7872);
    }

}