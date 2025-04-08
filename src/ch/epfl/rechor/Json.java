package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface Json {

    record JArray(List<Json> jsonList) implements Json {

        @Override
        public String toString() {

            // on itère avec un stream et on ajoute toutes les
            // représentations textuelles des objets Json puis on join tout
            // avec les bons delimiters
            return jsonList.stream()
                    .map(Json::toString)
                    // un assistant nous a conseillé d'utiliser joining
                    .collect(Collectors.joining(",", "[", "]"));

        }
    }

    record JObject(Map<String, Json> jsonStringMap) implements Json {

        @Override
        public String toString() {

            // on fait de la même façon que dans JArray
            return jsonStringMap.entrySet()
                    .stream()
                    .map(entry -> String.format("\"%s\":%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }



    record JString(String jsonString) implements Json {

        @Override
        public String toString() {
            // ici une simple concaténation est la façon la plus et propre
            return  "\"" + jsonString + "\"";
        }
    }

    record JNumber(double jsonNumber) implements Json {

        @Override
        public String toString() {
            return Double.toString(jsonNumber);
        }
    }


}
