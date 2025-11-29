package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Enregistrement qui représente l'indicatif (call sign) d'un aéronef.
 *
 * @param string chaîne contenant la représentation textuelle de l'indicatif de l'aeronef.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record CallSign(String string) {

    // "[A-Z0-9 ]{0,8}" Expression régulière correspondant à un indicatif
    private static final Pattern num = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Constructeur compact de l'enregistrement AircraftDescription qui valide la chaîne qui lui est passée
     * au moyen de l'expression régulière "[A-Z0-9 ]{0,8}"
     *
     * @param string chaîne contenant la représentation textuelle de l'indicatif de l'aeronef.
     * @throws IllegalArgumentException si "string" ne représente pas un indicatif valide.
     */
    public CallSign {
        // Vérification de la correspondance de la chaine à l’expression régulière
        boolean callSignIsValid = num.matcher(string).matches();
        Preconditions.checkArgument(callSignIsValid);
    }
}