package ch.epfl.javions;

/**
 * Des coordonnées géographiques selon la projection WebMercator.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class WebMercator {
    private WebMercator() {
    }

    /**
     * Retourne la longitude donnée au niveau de zoom donné.
     *
     * @param zoomLevel le niveau de zoom donné.
     * @param longitude la longitude en radians.
     * @return la longitude donnée au niveau de zoom donné en T32.
     */
    public static double x(int zoomLevel, double longitude) {
        double longitudeTurn = Units.convertTo(longitude,
                Units.Angle.TURN);
        return Math.scalb(longitudeTurn + 0.5, 8 + zoomLevel);
    }

    /**
     * Retourne la latitude donnée au niveau de zoom donné
     *
     * @param zoomLevel le niveau de zoom donné.
     * @param latitude  la longitude en radians.
     * @return la latitude donnée au niveau de zoom donné en T32.
     */
    public static double y(int zoomLevel, double latitude) {
        double argument = Math.tan(latitude);
        double radianAngle = (-Math2.asinh(argument));
        double latitudeTurn = Units.convertTo(radianAngle,
                Units.Angle.TURN);
        return Math.scalb(latitudeTurn + 0.5, 8 + zoomLevel);
    }
}
