package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.*;

/**
 * Classe qui permet de convertir un voyage en un document GeoJSON
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class JourneyGeoJsonConverter {

    // Pour rendre la classe non instantiable
    private JourneyGeoJsonConverter() {}

    // Facteur de précision pour arrondir aux 5 décimales
    private static final double COORDINATE_PRECISION = 1e5;

    /**
     * Arrondit une valeur double à la précision définie (5 décimales).
     * @param value la valeur à arrondir
     */
    private static double roundCoordinate(double value) {
        return Math.round(value * COORDINATE_PRECISION) / COORDINATE_PRECISION;
    }

    /**
     * Permet de convertir un voyage en un document GeoJSON représentant son tracé.
     * @param journey voyage à convertir
     * @return Json constitué d'une ligne brisée
     */
    public static Json toGeoJson(Journey journey){

        // Création de la map qui sera retournée sous sa version Json, représente le fichier GeoJson
        Map<String, Json> geoJsonMap = new LinkedHashMap<>();

        // Tableau "parent" de tous les petits tableaux de coordonnées
        List<Json> coordsContainer = new ArrayList<>();

        // ------------------- AJOUT DE TOUTES LES COORDONNÉES ----------------- //

        // On s'occupe juste du premier stop, avant de rentrer dans la boucle
        addStopCoordinates(journey.depStop(), coordsContainer);

        // Boucle sur TOUS les stops du voyage, et ajoute leurs coordonnées dans la liste
        for (Journey.Leg leg : journey.legs()){
            // À chaque étape, on cherche 1) intermediateStop 2) arrStop
            // (depStop représente l'arrStop de l'étape d'avant.)

            // 1) intermediateStop
            for (Journey.Leg.IntermediateStop iStop : leg.intermediateStops()){
                addStopCoordinates(iStop.stop(), coordsContainer);
            }

            // 2) arrStop
            addStopCoordinates(leg.arrStop(), coordsContainer);

        }

        // On a tout, on transforme la Liste<Json> en JArray et on return la map JObject
        geoJsonMap.put("type", new Json.JString("LineString"));
        geoJsonMap.put("coordinates", new Json.JArray(coordsContainer));
        return new Json.JObject(geoJsonMap);

    }

    /**
     * Méthode qui ajoute les coordonnées dans la liste donnée
     * @param stop un arrêt
     * @param list une liste de coordonnées
     */
    private static void addStopCoordinates(Stop stop, List<Json> list){
        Json.JArray jArrayWithCoords = new Json.JArray(
                List.of(
                        new Json.JNumber(roundCoordinate(stop.longitude())),
                        new Json.JNumber(roundCoordinate(stop.latitude()))
                )
        );

        // On ajoute seulement si les coordonnées sont différentes du dernier stop
        if (list.isEmpty() || !list.getLast().equals(jArrayWithCoords)){
            list.add(jArrayWithCoords);
        }
    }
}
