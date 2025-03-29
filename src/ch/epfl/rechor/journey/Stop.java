package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.util.Objects;

/**
 * Classe qui représente un arrêt de transport public (à la fois pour les gares et pour les voies ou quais)
 * @param name (Nom de l'arrêt, ne doit pas être nul)
 * @param platformName (nom du quai / voie, si il y en a)
 * @param longitude (longitude)
 * @param latitude  (latitude)
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Constructeur compact d'un arrêt,
     * vérifiant les conditions nécessaires aux paramètres
     */
    public Stop {
        Objects.requireNonNull(name, "le nom est null");
        Preconditions.checkArgument(Math.abs(longitude) <= 180 && Math.abs(latitude) <= 90);
    }

}
