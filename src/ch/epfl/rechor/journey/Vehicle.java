package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Type énuméré des différents véhicules
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public enum Vehicle {
    // représente un tram
    TRAM,
    // représente un métro
    METRO,
    // représente un train
    TRAIN,
    // représente un bus ou un car
    BUS,
    // représente un bac ou un autre type de bateau
    FERRY,
    // représente une télécabine ou un autre type de transport aérien à câble
    AERIAL_LIFT,
    // représente un funiculaire
    FUNICULAR;

    public static final List<Vehicle> ALL = List.of(Vehicle.values());
}
