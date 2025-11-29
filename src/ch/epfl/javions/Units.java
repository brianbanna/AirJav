package ch.epfl.javions;

/**
 * Des définitions des différentes unités du Système International.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class Units {
    public static final double CENTI = 1e-2; // Représente le centi (10 puissance -2)
    public static final double KILO = 1e3; // Représente le kilo (10 puissance 3)

    /**
     * Constructeur par défaut privé de la classe non instanciable Units.
     */
    private Units() {
    }

    /**
     * Permet la conversion d'une valeur depuis une unité de départ vers une unité d'arrivée.
     *
     * @param value    la valeur qu'on veut convertir.
     * @param fromUnit l'unité de départ.
     * @param toUnit   l'unité d'arrivée.
     * @return la valeur "value" convertie en l'unité d'arrivée.
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        double ratio = (fromUnit / toUnit);
        return value * ratio;
    }

    /**
     * Permet la conversion d'une valeur depuis une unité de départ vers une unité d'arrivée de base, donc 1.
     *
     * @param value    la valeur qu'on veut convertir.
     * @param fromUnit l'unité de départ.
     * @return la valeur convertie en l'unité d'arrivée, donc l'unité de base qui vaut 1.
     */
    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }

    /**
     * Permet la conversion d'une valeur depuis une unité de départ de base, donc 1 vers une unité d'arrivée.
     *
     * @param value  la valeur qu'on veut convertir.
     * @param toUnit l'unité d'arrivée.
     * @return la valeur convertie en l'unité d'arrivée, depuis l'unité de départ de base qui vaut 1.
     */
    public static double convertTo(double value, double toUnit) {
        double ratio = (1 / toUnit);
        return value * ratio;
    }

    /**
     * Des unités d'angles du Système International.
     *
     * @author Brian Jean Claud El Banna (356437)
     * @author Nicolas Khamis (355598)
     */
    public final static class Angle {
        public static final double RADIAN = 1;
        public static final double TURN = 2 * Math.PI * RADIAN;
        public static final double DEGREE = TURN / 360;
        public static final double T32 = TURN / Math.scalb(1, 32);

        /**
         * Constructeur par défaut privé de la classe non instanciable Angle.
         */
        private Angle() {
        }
    }

    /**
     * Des unités de longueur du Système International.
     *
     * @author Brian Jean Claud El Banna (356437)
     * @author Nicolas Khamis (355598)
     */
    public final static class Length {
        public static final double METER = 1;
        public static final double CENTIMETER = METER * CENTI;
        public static final double INCH = 2.54 * CENTIMETER;
        public static final double FOOT = 12 * INCH;
        public static final double KILOMETER = METER * KILO;
        public static final double NAUTICAL_MILE = 1852 * METER;

        /**
         * Constructeur par défaut privé de la classe non instanciable Length.
         */
        private Length() {
        }
    }

    /**
     * Des unités de temps du Système International.
     *
     * @author Brian Jean Claud El Banna (356437)
     * @author Nicolas Khamis (355598)
     */
    public final static class Time {
        public static final double SECOND = 1;
        public static final double MILLISECONDS = 1e-3 * SECOND;
        public static final double NANOSECONDS = 1e-9 * SECOND;
        public static final double MINUTE = 60 * SECOND;
        public static final double HOUR = 60 * MINUTE;

        /**
         * Constructeur par défaut privé de la classe non instanciable Time.
         */
        private Time() {
        }
    }

    /**
     * Des unités de vitesse du Système International.
     *
     * @author Brian Jean Claud El Banna (356437)
     * @author Nicolas Khamis (355598)
     */
    public final static class Speed {
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;

        /**
         * Constructeur par défaut privé de la classe non instanciable Speed.
         */
        private Speed() {
        }
    }
}
