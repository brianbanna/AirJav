package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement qui représente un message ADS-B d'identification et de catégorie
 *
 * @param timeStampNs horodatage du message, exprimé en nanosecondes
 * @param icaoAddress l'adresse OACI de l'expéditeur du message
 * @param category    la catégorie d'aéronef de l'expéditeur
 * @param callSign    l'indicatif de l'expéditeur
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record AircraftIdentificationMessage(long timeStampNs,
                                            IcaoAddress icaoAddress,
                                            int category,
                                            CallSign callSign) implements Message {
    private final static int CA_SIZE = 4;
    private final static int CALLSIGN_SIZE = 8;
    private final static int ADSB_LENGTH = 14;
    private final static int ALPHABET_LENGTH = 26;
    private final static int DEBUT_OF_PARTIAL_CATEG = 48;
    private final static int ASCII_NUM = 64;

    /**
     * Constructeur compact de AircraftIdentificationMessage
     *
     * @throws NullPointerException     si icaoAddress ou callSign sont nuls
     * @throws IllegalArgumentException si timeStampNs est strictement inférieure à 0.
     */
    public AircraftIdentificationMessage {
        boolean timeStampsisPositive = timeStampNs >= 0;
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampsisPositive);
    }

    /**
     * @param rawMessage message brut
     * @return le message d'identification correspondant au message brut donné ou @null
     * si au moins un des caractères de l'indicatif qu'il contient est invalide
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();
        char[] characters = new char[CALLSIGN_SIZE];
        StringBuilder str = new StringBuilder();
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création de la chaine de caractere de callSign (Pour les nombres entre 1 et 26 qui correspondent aux lettre,
         on fait l'addition adéquate (+64) pour obtenir le code ascii  de la lettre correspondante.
    --------------------------------------------------------------------------------------------------------------------
    */
        for (int j = 0; j < CALLSIGN_SIZE; j++) {
            int character = Bits.extractUInt(payload, 6 * j, 6);
            boolean charIsALetter = character >= 1 && character <= ALPHABET_LENGTH;
            characters[j] = (charIsALetter) ? (char) (character + ASCII_NUM) : (char) character;
            str.append(characters[j]);
        }
        str.reverse();


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création de la catégorie :  les 4 bits de poids fort valent 14 moins le code de type et les 4 bits de poids
         faible sont le champ CA
    --------------------------------------------------------------------------------------------------------------------
    */

        long partialCategory = Bits.extractUInt(payload, DEBUT_OF_PARTIAL_CATEG, 3); //Correspond aux 4 bits de poids faible
        long typeCode = rawMessage.typeCode();
        byte mostSignificantBits = (byte) (ADSB_LENGTH - typeCode);  // les 4 bits de poids fort
        int category = (int) ((mostSignificantBits << CA_SIZE) | partialCategory);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Vérification de la validité des caractères à l'aide d'un bloc try-catch et création du message d'identification
    --------------------------------------------------------------------------------------------------------------------
    */
        try {
            CallSign callSign = new CallSign(str.toString().stripTrailing());
            return new AircraftIdentificationMessage(rawMessage.timeStampNs(),
                    rawMessage.icaoAddress(),
                    category,
                    callSign);
        } catch (IllegalArgumentException e) {
            // La classe CallSign s'occupe de lancer cette exception si les caractères ne sont pas valides
            return null;
        }
    }
}