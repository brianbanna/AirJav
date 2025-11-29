package ch.epfl.javions.aircraft;

/**
 * Type énuméré qui représente la catégorie de turbulence de sillage d'un aéronef.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * Retourne la catégorie de turbulence de sillage correspondant a la chaine donnée
     *
     * @param s la chaine considérée
     * @return une des WakeTurbulenceCategory, a savoir LIGHT, MEDIUM, HEAVY, UNKNOWN
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> (LIGHT);
            case "M" -> (MEDIUM);
            case "H" -> (HEAVY);
            default -> (UNKNOWN);
        };
    }
}