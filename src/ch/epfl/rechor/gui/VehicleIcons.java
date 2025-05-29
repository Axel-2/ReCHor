package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Classe qui donne accès aux images (icônes) représentant les différents véhicules
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class VehicleIcons {

    private final static Map<Vehicle, Image> vehicleCacheMap = new EnumMap<>(Vehicle.class);

    // non instantiable
    private VehicleIcons() {}

    /**
     * Fonction qui prend en argument une valeur de type Vehicle
     * et retourne l'image JavaFX correspondante
     * @param vehicle Un type de vehicle
     * @return une image JFX
     */
    public static Image iconFor(Vehicle vehicle) {
        String vehiclePath = "%s.png".formatted(vehicle.name());
        return vehicleCacheMap.computeIfAbsent(
                vehicle, k -> new Image(vehiclePath)
        );
    }
}
