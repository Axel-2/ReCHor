package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

public sealed interface Json {

    record JArray(List<Json> jsonList) implements Json {

        // TODO mais c'est juste chiant

        @Override
        public String toString() {
            return "Jarray{" +
                    "jsonList=" + jsonList +
                    '}';
        }
    }

    record JObject(Map<Json, String> jsonStringMap) {

        @Override
        public String toString() {
            return "JObject{" +
                    "jsonStringMap=" + jsonStringMap +
                    '}';
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
