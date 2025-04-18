package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import javax.swing.text.Element;
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
        String vehiclePath = vehicle.name() + ".png";
        return vehicleCacheMap.computeIfAbsent(
                vehicle, k -> new Image(vehiclePath)
        );
    }
}
