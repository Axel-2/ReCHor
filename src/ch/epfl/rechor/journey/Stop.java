package ch.epfl.rechor.journey;

public record Stop(String name, String platformName, double longitude, double latitude) {

    // constructeur compact
    public Stop {




        if (Math.abs(longitude) > 180 || Math.abs(latitude) > 90) {
            throw new IllegalArgumentException("Longitude or latitude is invalid");
        }
    }

}
