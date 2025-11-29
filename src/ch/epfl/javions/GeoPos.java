package ch.epfl.javions;

/**
 * Des coordonnées géographiques.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {
    private final static double MOINS_2_PUISS_30 = Math.scalb(-1, 30);
    private final static double PLUS_2_PUISS_30 = Math.scalb(1, 30);

    /**
     * Constructeur public de l'enregistrement GeoPos.
     *
     * @param longitudeT32 la longitude en T32.
     * @param latitudeT32  la latitude en T32.
     * @throws IllegalArgumentException si la latitude n'est pas valide.
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * Permet de vérifier si la latitude est valide, c’est-à-dire comprise entre -2 puissance 30 et 2 puissance 30.
     *
     * @param latitudeT32 la latitude dont on vérifie la validité.
     * @return vrai si et seulement si la valeur de la latitude est comprise entre -2 puissance 30 et 2 puissance 30.
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return (MOINS_2_PUISS_30 <= latitudeT32) && (latitudeT32 <= PLUS_2_PUISS_30);
    }

    /**
     * Retourne la longitude en radians.
     *
     * @return la longitude en radians.
     */
    public double longitude() {
        return Units.convertFrom(longitudeT32,
                Units.Angle.T32);
    }

    /**
     * Retourne la latitude en radians.
     *
     * @return la latitude en radians.
     */
    public double latitude() {
        return Units.convertFrom(latitudeT32,
                Units.Angle.T32);
    }

    /**
     * Retourne en texte la longitude et la latitude.
     *
     * @return en texte la longitude et la latitude.
     */
    @Override
    public String toString() {
        double longitudeDegree = Units.convert(longitudeT32,
                Units.Angle.T32,
                Units.Angle.DEGREE);
        double latitudeDegree = Units.convert(latitudeT32,
                Units.Angle.T32,
                Units.Angle.DEGREE);
        return ("(" + longitudeDegree + "°, " + latitudeDegree + "°)");
    }
}
