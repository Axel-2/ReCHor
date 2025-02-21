package ch.epfl.rechor.journey;

import java.util.List;
/**
 * Véhicule
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FUNICULAR;

    public static final List<Vehicle> ALL = List.of(Vehicle.values());


}
