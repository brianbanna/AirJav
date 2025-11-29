package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * Décodeur de position CPR.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class CprDecoder {
    // Nombre de zones de latitude pour le découpage pair
    private static final double NB_ZONES_LAT_EVEN_DECOMP = 60;

    // Nombre de zones de latitude pour le découpage impair
    private static final double NB_ZONES_LAT_ODD_DECOMP = 59;

    // Largeur des zones de latitude pour le découpage pair
    private static final double ZONES_WIDTH_LAT_EVEN_DECOMP = (double) 1 / NB_ZONES_LAT_EVEN_DECOMP;

    // Largeur des zones de latitude pour le découpage impair
    private static final double ZONES_WIDTH_LAT_ODD_DECOMP = (double) 1 / NB_ZONES_LAT_ODD_DECOMP;

    // On crée un constructeur privé car la classe est non instanciable
    private CprDecoder() {
    }

    /**
     * Retourne la positon géographique correspondant aux positions locales normalisées données.
     *
     * @param x0         longitude locale normalisée d'un message pair.
     * @param y0         latitude locale normalisée d'un message pair.
     * @param x1         longitude locale normalisée d'un message impair.
     * @param y1         latitude locale normalisée d'un message impair.
     * @param mostRecent la position du message le plus récent.
     * @return les coordonnées géographiques correspondant aux positions locales normalisées fournies,
     * ou null si la latitude de la position décodée n'est pas valide (c.-à-d. comprise entre ±90°)
     * ou si la position ne peut pas être déterminée en raison d'un changement de bande de latitude.
     * @throws IllegalArgumentException si mostRecent ne vaut pas 0 ou 1.
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Teste la validité de l'attribut "mostRecent"
    --------------------------------------------------------------------------------------------------------------------
    */
        boolean mostRecentIsValid = (mostRecent == 0 || mostRecent == 1);
        Preconditions.checkArgument(mostRecentIsValid);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calcul du numéro des zones de latitude dans les découpages
    --------------------------------------------------------------------------------------------------------------------
    */
        // On crée un tableau qui contient les numéros de zones de latitude pair et impair respectivement
        double[] numZonesLatitude = numberOfZonesCalculator(y0, y1,
                NB_ZONES_LAT_EVEN_DECOMP, NB_ZONES_LAT_ODD_DECOMP);
        double numZoneEvenLat = numZonesLatitude[0];
        double numZoneOddLat = numZonesLatitude[1];


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calcul de la latitude (en tours) et conversion des angles en leur équivalent négatif
    --------------------------------------------------------------------------------------------------------------------
    */
        double evenLatitude = measurmentCalculator(ZONES_WIDTH_LAT_EVEN_DECOMP, numZoneEvenLat, y0);
        double oddLatitude = measurmentCalculator(ZONES_WIDTH_LAT_ODD_DECOMP, numZoneOddLat, y1);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calcul du numéro des zones de longitude dans les découpages grâce à la formule de l'énoncé
    --------------------------------------------------------------------------------------------------------------------
    */
        // Nombre de zones de longitude pour le découpage pair
        double numberLongZonesEvenDecomp = numberOfLongitudeZones(evenLatitude);

        // Nombre de zones de longitude pour le découpage impair
        double numberLongZonesOddDecomp = numberLongZonesEvenDecomp - 1;


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Test du changement des bandes de latitude
    --------------------------------------------------------------------------------------------------------------------
    */
        // Nombre de zones de longitude pour le découpage de test (pour vérifier s'il y'a un changement de bande)
        double numberLongZonesTest = numberOfLongitudeZones(oddLatitude);

        // Le nombre de zones de longitudes calculés avec la latitude paire et impaire doit être la même
        boolean changeOfBand = (numberLongZonesEvenDecomp != numberLongZonesTest);
        if (changeOfBand) {
            return null;
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calcul de la longitude (en tours) et conversion des angles en leur équivalent négatif
    --------------------------------------------------------------------------------------------------------------------
    */
        double evenLongitude = 0;
        double oddLongitude = 0;

        if (numberLongZonesEvenDecomp == 1) {
            evenLongitude = x0;
            oddLongitude = x1;
        } else if (numberLongZonesEvenDecomp > 1) {
            // Largeur des zones de longitude pour le découpage pair
            double zonesWidthLongEvenDecomp = (double) 1 / numberLongZonesEvenDecomp;

            // Largeur des zones de longitude pour le découpage impair
            double zonesWidthLongOddDecomp = (double) 1 / numberLongZonesOddDecomp;

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calcul du numéro des zones de longitude dans les découpages
    --------------------------------------------------------------------------------------------------------------------
    */
            // On crée un tableau qui contient les numéros de zones de longitude pair et impair respectivement
            double[] numZonesLongitude = numberOfZonesCalculator(x0, x1,
                    numberLongZonesEvenDecomp, numberLongZonesOddDecomp);
            double numZoneEvenLong = numZonesLongitude[0];
            double numZoneOddLong = numZonesLongitude[1];


            // Calcul des longitudes pairs et impairs (en tours) et conversion des angles en leur équivalent négatif
            evenLongitude = measurmentCalculator(zonesWidthLongEvenDecomp, numZoneEvenLong, x0);
            oddLongitude = measurmentCalculator(zonesWidthLongOddDecomp, numZoneOddLong, x1);
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Affichage final
    --------------------------------------------------------------------------------------------------------------------
    */
        try {
            // Si le message le plus récent est pair, on affiche la longitude et la latitude en T32 d'un message pair
            if (mostRecent == 0) {
                double evenLongitudeT32 = Units.convert(evenLongitude,
                        Units.Angle.TURN,
                        Units.Angle.T32);
                double evenLatitudeT32 = Units.convert(evenLatitude,
                        Units.Angle.TURN,
                        Units.Angle.T32);
                return new GeoPos((int) Math.rint(evenLongitudeT32),
                        (int) Math.rint(evenLatitudeT32));
            }
            // Si le message le plus récent est impair, on affiche la longitude et la latitude en T32 d'un message impair
            else {
                double oddLongitudeT32 = Units.convert(oddLongitude,
                        Units.Angle.TURN,
                        Units.Angle.T32);
                double oddLatitudeT32 = Units.convert(oddLatitude,
                        Units.Angle.TURN,
                        Units.Angle.T32);
                return new GeoPos((int) Math.rint(oddLongitudeT32),
                        (int) Math.rint(oddLatitudeT32));
            }
        } catch (IllegalArgumentException e) {
            /* Si la latitude de la position décodée n'est pas valide (c.-à-d.
        comprise entre ±90°). Ceci est déterminé directement grace au constructeur
        de GeoPos (qui lance IllegalArgumentException si la latitude n'est pas valide) */
            return null;
        }
    }

    /**
     * Méthode qui calcule le numero d'une zone pair et impair pour une mesure (longitude ou latitude)
     *
     * @param valueNormalized0  la valeur paire normalisée de la latitude locale ou de la longitude locale
     * @param valueNormalized1  la valeur impaire normalisée de la latitude locale ou de la longitude locale
     * @param nbZonesEvenDecomp le nombre de zones d'une mesure paire (longitude ou latitude)
     * @param nbZonesOddDecomp  le nombre de zones d'une mesure impaire (longitude ou latitude)
     * @return un tableau qui contient les numéros de zones pair et impair respectivement de la mesure donnée
     */
    private static double[] numberOfZonesCalculator(double valueNormalized0,
                                                    double valueNormalized1,
                                                    double nbZonesEvenDecomp,
                                                    double nbZonesOddDecomp) {
        double calculatedEvenZoneNumber; // le numero d'une zone paire pour la mesure
        double calculatedOddZoneNumber; // le numero d'une zone impaire pour la mesure
        // argument de Math.rint
        double argument = valueNormalized0 * nbZonesOddDecomp - valueNormalized1 * nbZonesEvenDecomp;
        double zoneNumber = Math.rint(argument);

        if (zoneNumber < 0) {
            calculatedEvenZoneNumber = zoneNumber + nbZonesEvenDecomp;
            calculatedOddZoneNumber = zoneNumber + nbZonesOddDecomp;
        } else {
            calculatedEvenZoneNumber = zoneNumber;
            calculatedOddZoneNumber = zoneNumber;
        }

        // tableau qui contient les numéros de zones pair et impair respectivement de la mesure
        return new double[]{calculatedEvenZoneNumber, calculatedOddZoneNumber};
    }

    /**
     * Méthode qui calcule une mesure d'une coordonnée géographique (longitude ou latitude).
     *
     * @param measureWidth    la largeur de la zone de latitude ou de longitude pour un découpage donné.
     * @param zoneNumber      le numero de la zone dans laquelle se trouve l'aeronef.
     * @param normalizedValue la valeur pair ou impaire normalisée de la latitude locale ou de la longitude locale.
     * @return la mesure recentrée d'une coordonnée géographique donnée (longitude ou latitude).
     */
    private static double measurmentCalculator(double measureWidth, double zoneNumber, double normalizedValue) {
        double calculatedMeasurment = measureWidth * (zoneNumber + normalizedValue);

        /* On recentre l'angle calculé en convertissant les angles
        supérieurs ou égaux à ½ tour en leur équivalent négatif */
        if (calculatedMeasurment > 0.5) {
            calculatedMeasurment -= 1;
        }
        return calculatedMeasurment;
    }

    /**
     * Méthode qui calcule le numero de zones de longitude.
     *
     * @param latitude la latitude de l'aeronef.
     * @return le numero de zones de longitude.
     */
    private static double numberOfLongitudeZones(double latitude) {
        double nbZonesLongitude;
        double aNumerator = 1 - Math.cos(2
                * Math.PI
                * ZONES_WIDTH_LAT_EVEN_DECOMP);

        // On convertit l'angle de l'unité tour à l'unité de base, donc le radian
        double radianAngle = Units.convertFrom(latitude, Units.Angle.TURN);
        double aDenominatorArgument = Math.cos(radianAngle);
        double aDenominator = Math.pow(aDenominatorArgument, 2);
        double aCosArgument = 1 - (aNumerator / aDenominator);
        double a = Math.acos(aCosArgument);

        /* Si la valeur passée à aCos est supérieur à 1 en valeur absolue, la latitude est 1
        sinon, on applique la formule de l'énoncé */
        if (Double.isNaN(a)) {
            nbZonesLongitude = 1;
        } else {
            nbZonesLongitude = Math.floor(Units.Angle.TURN / a);
        }

        return nbZonesLongitude;
    }
}
