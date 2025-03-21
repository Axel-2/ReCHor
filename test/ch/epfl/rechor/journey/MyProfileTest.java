package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

public class MyProfileTest {

    private FileTimeTable fileTimeTable;
    private final Path testDirectory = Path.of("timetable"); // Adapte ce chemin selon ton projet
    private final LocalDate testDate = LocalDate.of(2025, 3, 17); // Une date correspondant aux fichiers test

    @BeforeEach
    void setUp() throws IOException {
        fileTimeTable = (FileTimeTable) FileTimeTable.in(testDirectory);
    }


    @Test
    void firstTest() {


        Profile profile = new Profile.Builder(fileTimeTable, testDate, 10).build();

        asserE



    }
}
