package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.HashMap;
import java.util.Map;

public class JourneyGeoJsonConverter {

    // pour la rendre non instanciable
    private JourneyGeoJsonConverter() {};

    /**
     * Permet de convertir un voyage en un document GeoJSON représentant son tracé.
     * @param journey voyage à convertir
     * @return Json constitué d'une ligne brisée
     */
    public Json toGeoJson(Journey journey){

        Map<String, Json> map = new HashMap<>();
        Json temp = new Json.JString("LineString");
        map.put("type", temp);

        // Ligne brisée
        Json.JObject LineString = new Json.JObject(map);
    }
}
