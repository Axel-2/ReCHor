package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class JourneyGeoJsonConverter {

    // pour la rendre non instanciable
    private JourneyGeoJsonConverter() {};

    /**
     * Permet de convertir un voyage en un document GeoJSON représentant son tracé.
     * @param journey voyage à convertir
     * @return Json constitué d'une ligne brisée
     */
    public Json toGeoJson(Journey journey){

        // Création de la map qui sera retournée sous sa version Json, représente le fichier GeoJson
        Map<String, Json> geoJsonMap = new HashMap<>();
        geoJsonMap.put("type", new Json.JString("LineString"));

        List<Json> coordsContainer = new LinkedList<>();

        // On s'occupe juste du premier stop, avant de rentrer dans la boucle
        List<Json> coords = new LinkedList<>();

        Stop journeyFirstStop = journey.depStop();
        coords.add(new Json.JNumber((Math.round(journeyFirstStop.longitude() * 100000d)/100000d)));
        coords.add(new Json.JNumber((Math.round(journeyFirstStop.latitude() * 100000d)/100000d)));
        coordsContainer.add(new Json.JArray(coords));
        coords.clear();

        // Boucle sur TOUS les stops du voyage, et ajoute leurs coordonnées dans la liste
        for (Journey.Leg leg : journey.legs()){
            // À chaque étape, on cherche 1) intermediateStop 2) arrStop
            // (depStop représente l'arrStop de l'étape d'avant.)

            // 1) intermediateStop
            for (Journey.Leg.IntermediateStop iStop : leg.intermediateStops()){
                coords.add(new Json.JNumber((Math.round(iStop.stop().longitude() * 100000d)/100000d)));
                coords.add(new Json.JNumber((Math.round(iStop.stop().latitude() * 100000d)/100000d)));
                coordsContainer.add(new Json.JArray(coords));
                coords.clear();
            }

            // 2) arrStop
            coords.add(new Json.JNumber((Math.round(leg.arrStop().longitude() * 100000d)/100000d)));
            coords.add(new Json.JNumber((Math.round(leg.arrStop().latitude() * 100000d)/100000d)));
            coordsContainer.add(new Json.JArray(coords));
            coords.clear();

        }

        // On a tout, on transforme la Liste<Json> en JArray et on return la map JObject
        geoJsonMap.put("coordinates", new Json.JArray(coordsContainer));
        return new Json.JObject(geoJsonMap);

    }
}
