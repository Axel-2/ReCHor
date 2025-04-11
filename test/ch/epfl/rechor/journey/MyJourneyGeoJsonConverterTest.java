package ch.epfl.rechor.journey; // Ou le package où se trouve ton test

import ch.epfl.rechor.Json;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MyJourneyGeoJsonConverterTest {

    // Fonction utilitaire pour trouver l'ID d'une station par nom (reprise du test précédent)
    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i += 1)
            if (stations.name(i).equals(stationName)) return i;
        throw new NoSuchElementException("Station not found: " + stationName);
    }

    @Test
    void generateAndPrintGeoJsonForEpflToGruyeres() throws IOException {
        // 1. Setup: Charger l'horaire, définir le trajet
        System.out.println("Chargement de l'horaire...");
        TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable"))); // Adapte le chemin si besoin
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.APRIL, 1); // Adapte la date si besoin
        int depStationId = stationId(stations, "Ecublens VD, EPFL");
        int arrStationId = stationId(stations, "Gruyères");

        // 2. Calculer le profil
        System.out.println("Calcul du profil EPFL -> Gruyères...");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);

        // 3. Extraire un voyage (p.ex., le premier trouvé)
        System.out.println("Extraction du premier voyage...");
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);

        // Vérifie s'il y a au moins un voyage avant d'essayer d'y accéder
        assertFalse(journeys.isEmpty(), "Aucun voyage trouvé entre EPFL et Gruyères.");
        Journey journeyToConvert = journeys.get(0); // Prend le premier voyage
        assertNotNull(journeyToConvert, "Le premier voyage extrait est null.");

        // 4. Convertir en GeoJSON (objet Json)
        System.out.println("Conversion en GeoJSON...");
        // Assure-toi que ta méthode toGeoJson est static !
        Json geoJsonObject = JourneyGeoJsonConverter.toGeoJson(journeyToConvert);
        assertNotNull(geoJsonObject, "L'objet Json GeoJSON généré est null.");

        // 5. Obtenir la représentation String (NÉCESSITE toString() CORRECTS DANS Json.java !)
        System.out.println("Génération de la chaîne JSON...");
        String geoJsonString = geoJsonObject.toString();
        assertNotNull(geoJsonString, "La chaîne GeoJSON générée est null.");
        assertFalse(geoJsonString.isEmpty(), "La chaîne GeoJSON générée est vide.");

        // 6. Imprimer la chaîne pour copier-coller
        System.out.println("\n--- DEBUT GeoJSON ---");
        System.out.println(geoJsonString);
        System.out.println("--- FIN GeoJSON ---\n");

        System.out.println("Plop : " + geoJsonString);
        // 7. Assertions basiques pour vérifier que ce n'est pas complètement cassé
        assertTrue(geoJsonString.startsWith("{\"type\":\"LineString\""), "La chaîne GeoJSON ne commence pas correctement.");
        assertTrue(geoJsonString.contains("\"coordinates\":[["), "La chaîne GeoJSON ne semble pas contenir de coordonnées.");
        assertTrue(geoJsonString.endsWith("}"), "La chaîne GeoJSON ne finit pas correctement.");

        System.out.println("Test terminé. Copie le contenu entre les lignes DEBUT et FIN GeoJSON et colle-le sur geojsonlint.com");
    }
}