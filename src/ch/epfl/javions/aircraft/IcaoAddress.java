package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Enregistrement qui représente l'adresse OACI d'un aéronef.
 *
 * @param string chaîne contenant la représentation textuelle de l'adresse OACI
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record IcaoAddress(String string) {

    // "[0-9A-F]{6}" Expression régulière correspondant à une Adresse OACI
    private static final Pattern NUM = Pattern.compile("[0-9A-F]{6}");

    /**
     * Constructeur compact de l'enregistrement AircraftDescription quI valide
     * la chaîne qui lui est passée au moyen de l'expression régulière "[0-9A-F]{6}"
     *
     * @param string chaîne contenant la représentation textuelle de l'adresse OACI
     * @throws IllegalArgumentException si "string" est vide
     * @throws IllegalArgumentException si "string" ne représente pas une adresse OACI valide
     */

    public IcaoAddress {
        // Vérification de la correspondance de la chaine à l’expression régulière
        boolean icaoIsValid = NUM.matcher(string).matches();
        Preconditions.checkArgument(icaoIsValid);
    }
}