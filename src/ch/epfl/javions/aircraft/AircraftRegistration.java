package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Enregistrement qui représente l'immatriculation d'un aéronef.
 *
 * @param string chaîne contenant la représentation textuelle de l'immatriculation.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record AircraftRegistration(String string) {

    // "[A-Z0-9 .?/_+-]+" Expression régulière correspondant à l'immatriculation
    private static final Pattern NUM = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Constructeur compact de l'enregistrement AircraftDescription qui valide
     * la chaîne qui lui est passée au moyen de l'expression régulière "[A-Z0-9 .?/_+-]+"
     *
     * @param string chaîne contenant la représentation textuelle de l'immatriculation.
     * @throws IllegalArgumentException si "string" est vide.
     * @throws IllegalArgumentException si "string" ne représente pas immatriculation valide.
     */
    public AircraftRegistration {
        // Vérification de la correspondance de la chaine à l’expression régulière
        boolean aircraftRegistrationIsValid = NUM.matcher(string).matches();
        Preconditions.checkArgument(aircraftRegistrationIsValid);
    }
}
