package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.*;

public final class JourneyGeoJsonConverter {

    // pour la rendre non instanciable
    private JourneyGeoJsonConverter() {}

    // Facteur de précision pour arrondir aux 5 décimales
    private static final double COORDINATE_PRECISION = 1e5;

    /**
     * Arrondit une valeur double à la précision définie (5 décimales).
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
        stopsCoordsToArray(journey.depStop(), coordsContainer);

        // Boucle sur TOUS les stops du voyage, et ajoute leurs coordonnées dans la liste
        for (Journey.Leg leg : journey.legs()){
            // À chaque étape, on cherche 1) intermediateStop 2) arrStop
            // (depStop représente l'arrStop de l'étape d'avant.)

            // 1) intermediateStop
            for (Journey.Leg.IntermediateStop iStop : leg.intermediateStops()){
                stopsCoordsToArray(iStop.stop(), coordsContainer);
            }

            // 2) arrStop
            stopsCoordsToArray(leg.arrStop(), coordsContainer);

        }

        // On a tout, on transforme la Liste<Json> en JArray et on return la map JObject
        geoJsonMap.put("type", new Json.JString("LineString"));
        geoJsonMap.put("coordinates", new Json.JArray(coordsContainer));
        return new Json.JObject(geoJsonMap);

    }

    /**
     * Fonction qui ajoute les coordonées dans la liste donnée
     * @param stop un arret
     * @param list une liste de coordonées
     */
    private static void stopsCoordsToArray(Stop stop, List<Json> list){
        List<Json> coords = new ArrayList<>();
        coords.add(new Json.JNumber(roundCoordinate(stop.longitude())));
        coords.add(new Json.JNumber(roundCoordinate(stop.latitude())));
        Json.JArray JArrayWithCoords = new Json.JArray(coords);

        // On ajoute seulement si les coordonnées sont différentes du dernier stop
        // Dans le cas ou la liste n'est pas nulle, sinon il n'y a pas de dernier stop
        if (!list.isEmpty()){
            if (!list.getLast().equals(JArrayWithCoords)) {
                list.add(JArrayWithCoords);
            }
        } else {
            list.add(JArrayWithCoords);
        }
    }
}
