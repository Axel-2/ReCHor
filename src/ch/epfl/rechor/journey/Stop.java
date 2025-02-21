package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.util.Objects;

/**
 * Arrêt
 * @param name (Nom de l'arrêt, ne doit pas être nul)
 * @param platformName (nom u quai / voie, si il y en a)
 * @param longitude (longitude)
 * @param latitude  (latitude)
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    // constructeur compact
    public Stop {
        Objects.requireNonNull(name, "name is null");
        Preconditions.checkArgument(Math.abs(longitude) <= 180 && Math.abs(latitude) <= 90);
    }

}
