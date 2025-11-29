package ch.epfl.javions.adsb;

/**
 * Classe qui transforme les messages ADS-B bruts en messages d'identification, de position en vol, ou de vitesse en vol.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class MessageParser {
    // On crée un constructeur privé car la classe n'est pas instanciable
    private MessageParser() {
    }

    /**
     * Retourne l'instance de AircraftIdentificationMessage, de AirbornePositionMessage ou de AirborneVelocityMessage
     * correspondant au message brut donné,
     * ou null si le code de type de ce dernier ne correspond à aucun de ces trois types de messages,
     * ou si il est invalide.
     *
     * @param rawMessage le message brut donné
     * @return AircraftIdentificationMessage, AirbornePositionMessage, ou AirborneVelocityMessage si le code de type est
     * valide ou null s'il est invalide ou s'il ne correspond à aucun de ces trois types de messages
     */
    public static Message parse(RawMessage rawMessage) {
        int typeCode = rawMessage.typeCode();
        // Codes de type pour un message d'identification
        boolean typeCodeIndentification = typeCode >= 1 && typeCode <= 4;
        // Codes de type pour un message de position
        boolean typeCodePosition = (typeCode >= 9 && typeCode <= 18) || (typeCode >= 20 && typeCode <= 22);
        // Codes de type pour un message de vitesse
        boolean typeCodeVelocity = typeCode == 19;

        if (typeCodeIndentification) {
            return AircraftIdentificationMessage.of(rawMessage);
        } else if (typeCodePosition) {
            return AirbornePositionMessage.of(rawMessage);
        } else if (typeCodeVelocity) {
            return AirborneVelocityMessage.of(rawMessage);
        } else {
            return null;
        }
    }
}
