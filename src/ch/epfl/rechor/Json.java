package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

public sealed interface Json {

    record JArray(List<Json> jsonList) implements Json {

        @Override
        public String toString() {

            StringBuilder bld = new StringBuilder();

            // on commence par ouvrir la liste
            bld.append("[");
            // on itère sur tous les objets Json et on ajoute leur représation textuelle
            // avec toString
            jsonList.forEach(
                    (json -> {
                        bld.append(json.toString());
                        bld.append(",");
                    })
            );
            // on ferme la liste
            bld.append("]");

            String result = jsonList.stream()
                    .map(json::to)


            return bld.toString();

        }
    }

    record JObject(Map<String, Json> jsonStringMap) {

        @Override
        public String toString() {

            StringBuilder bld = new StringBuilder();

            // on commence par ouvrir l'objet
            bld.append("{\"")
            jsonStringMap.forEach(
                    (string, json) -> {
                        bld
                                .append("\"")
                                .append(string)
                                .append("\"")
                                .append(": ")
                                .append(json.toString())
                                .append("}")
                                .append(",");
                    }
            );
            // on ferme l'objet
            bld.append("}");


        }
    }

    record JString(String jsonString) {

        @Override
        public String toString() {
            return "JString{" +
                    "jsonString='" + jsonString + '\'' +
                    '}';
        }
    }

    record JNumber(double jsonNumber) {

        @Override
        public String toString() {
            return "JNumber{" +
                    "jsonNumber=" + jsonNumber +
                    '}';
        }
    }


}
