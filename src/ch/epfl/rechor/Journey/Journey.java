package ch.epfl.rechor;

import java.util.ArrayList;
import java.util.List;

public class Journey {

    private enum Vehicle {
        TRAM,
        METRO,
        TRAIN,
        BUS,
        FERRY,
        AERIAL_LIFT,
        FUNICULAR;

        // TODO Comment déclarer la variable ??
        public static final List<Vehicle> ALL = new ArrayList<Vehicle>();
    }


}
