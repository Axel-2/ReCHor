package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Interface qui représente un document JSON
 * Un document JSON peut être un tableau,
 * un objet, une chaîne ou un nombre.
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public sealed interface Json {

    /**
     * Représente un tableau JSON.
     * @param jsonList la liste des éléments JSON du tableau
     */
    record JArray(List<Json> jsonList) implements Json {

        @Override
        public String toString() {

            // on itère avec un stream et on ajoute toutes les
            // représentations textuelles des objets Json puis on join tout
            // avec les bons delimiters
            return jsonList.stream()
                    .map(Json::toString)
                    .collect(Collectors.joining(",", "[", "]"));

        }
    }

    /**
     * Représente un objet JSON (une collection de paires clé/valeur).
     * @param jsonStringMap la map associant chaque clé à sa valeur Json
     */
    record JObject(Map<String, Json> jsonStringMap) implements Json {

        @Override
        public String toString() {
            // On fait de la même façon que dans JArray
            return jsonStringMap.entrySet()
                    .stream()
                    .map(entry -> String.format("\"%s\":%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }


    /**
     * Représente une chaîne JSON.
     * @param jsonString la valeur de la chaîne (sans guillemets)
     */
    record JString(String jsonString) implements Json {

        @Override
        public String toString() {
            // ici une simple concaténation est la façon la plus et propre
            return  "\"" + jsonString + "\"";
        }
    }

    /**
     * Représente un nombre JSON.
     * @param jsonNumber la valeur numérique
     */
    record JNumber(double jsonNumber) implements Json {

        @Override
        public String toString() {
            return Double.toString(jsonNumber);
        }
    }


}
