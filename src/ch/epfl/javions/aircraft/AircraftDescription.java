package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Enregistrement qui représente la description d'un aéronef.
 *
 * @param string chaîne contenant la représentation textuelle de la description.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record AircraftDescription(String string) {

    // "[ABDGHLPRSTV-][0123468][EJPT-]" Expression régulière correspondant à une description
    private static final Pattern NUM = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * Constructeur compact de l'enregistrement AircraftDescription qui valide la chaîne qui lui est passée
     * au moyen de l'expression régulière "[ABDGHLPRSTV-][0123468][EJPT-]"
     *
     * @param string chaîne contenant la représentation textuelle de la description.
     * @throws IllegalArgumentException si "string" ne représente pas une description valide.
     */
    public AircraftDescription {
        if (!string.isEmpty()) {
            boolean aircraftDescriptionIsValid = NUM.matcher(string).matches();
            Preconditions.checkArgument(aircraftDescriptionIsValid);
        }

    }
}
