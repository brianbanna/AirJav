package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Enregistrement qui représente l'indicateur de type d'un aéronef.
 *
 * @param string chaîne contenant la représentation textuelle de l'indicateur de type.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record AircraftTypeDesignator(String string) {

    // "[A-Z0-9]{2,4}" Expression régulière correspondant à un indicateur de type
    private static final Pattern NUM = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * Constructeur compact de l'enregistrement AircraftDescription qui valide la chaîne qui lui est passée
     * au moyen de l'expression régulière "[A-Z0-9]{2,4}"
     *
     * @param string chaîne contenant la représentation textuelle de l'indicateur de type.
     * @throws IllegalArgumentException si "string" ne représente pas un indicateur de type valide.
     */
    public AircraftTypeDesignator {
        // Vérification de la correspondance de la chaine à l’expression régulière si la chaine n'est pas vide
        if (!string.isEmpty()) {
            boolean typeDesignatorIsValid = NUM.matcher(string).matches();
            Preconditions.checkArgument(typeDesignatorIsValid);
        }
    }
}
