package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement qui représente un message ADS-B de positionnement en vol.
 *
 * @param timeStampNs horodatage du message, exprimé en nanosecondes.
 * @param icaoAddress l'adresse OACI de l'expéditeur du message.
 * @param altitude    l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres.
 * @param parity      la parité du message (0 s'il est pair, 1 s'il est impair).
 * @param x           la longitude locale et normalisée à laquelle se trouvait l'aéronef au moment de l'envoi du message
 * @param y           la latitude locale et normalisée à laquelle se trouvait l'aéronef au moment de l'envoi du message.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */

public record AirbornePositionMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double altitude,
                                      int parity,
                                      double x,
                                      double y) implements Message {
    private final static int PAYLOAD_SIZE = 12;
    private final static int COORD_LENGTH = 17;
    private final static int FORMAT_START = 34;
    private final static int ALT_START = 36;
    /**
     * Constructeur compact de AirbornePositionMessage
     *
     * @throws NullPointerException     si icaoAddress est nul.
     * @throws IllegalArgumentException si timeStampNs est strictement inférieure à 0.
     */
    public AirbornePositionMessage {
        boolean timeStampsIsValid = timeStampNs >= 0;
        boolean parityIsValid = parity == 0 || parity == 1;
        boolean xIsValid = x >= 0 && x < 1;
        boolean yIsValid = y >= 0 && y < 1;
        Preconditions.checkArgument(timeStampsIsValid);
        Preconditions.checkArgument(parityIsValid);
        Preconditions.checkArgument(xIsValid);
        Preconditions.checkArgument(yIsValid);
        Objects.requireNonNull(icaoAddress);
    }

    /**
     * Méthode qui retourne le message de positionnement en vol correspondant au message brut donné.
     *
     * @param rawMessage message brut.
     * @return le message de positionnement en vol correspondant au message brut donné
     * ou null si l'altitude qu'il contient est invalide.
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        double altitudeInFeet; // Valeur de l'altitude en pieds
        double altitudeInMeters;  // Valeur de l'altitude en mètres
        long payload = rawMessage.payload();
        int parity = Bits.extractUInt(payload, FORMAT_START, 1);

        double longitude = Bits.extractUInt(payload, 0, COORD_LENGTH);
        double latitude = Bits.extractUInt(payload, 17, COORD_LENGTH);

        double longitudeNormalized = Math.scalb(longitude, -COORD_LENGTH);
        double latitudeNormalized = Math.scalb(latitude, -COORD_LENGTH);
        long altitude = Bits.extractUInt(payload, ALT_START, PAYLOAD_SIZE); // Attribut ALT du message

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Démêlage
    --------------------------------------------------------------------------------------------------------------------
    */
        boolean fourthAltBitIsOne = Bits.testBit(altitude, 4); // La condition est vraie si Q est égal à 1
        if (fourthAltBitIsOne) {
            int fourLsbBitsAltitude = Bits.extractUInt(altitude, 0, 4);
            int sevenMsbBitsAltitude = Bits.extractUInt(altitude, 5, 7);

            // L'attribut "altitude" duquel on a supprimé le quatrième bit
            int result = ((sevenMsbBitsAltitude << 4) | fourLsbBitsAltitude);

            altitudeInFeet = result * 25 - 1000; // valeur de l'altitude en pieds
            altitudeInMeters = Units.convertFrom(altitudeInFeet, Units.Length.FOOT); // valeur de l'altitude en mètre
        } else {
            int shuffle = 0;
            int groupeA = 0, groupeB = 0, groupeC = 0, groupeD = 0;
            /* On crée 4 groupes de 3 bits chacun en extrayant les bits 1 à 1
                • Le groupe D correspond au bits d’indice pair de la
                  séquence des 6 bits de poids faible de « altitude »
                • Le groupe B correspond au bits d’indice impair de la
                  séquence des 6 bits de poids faible de « altitude »
                • Le groupe A correspond au bits d’indice pair de la
                  séquence des 6 bits de poids fort de « altitude »
                • Le groupe C correspond au bits d’indice impair de la
                  séquence des 6 bits de poids fort de « altitude »

            A noter que les bits de chaque groupe sont dans l’ordre inverse
            de leur ordre final ex : on a une séquence D4D2D1 au lieu de D1D2D4.

            On place ces groupes de bits dans l’ordre inverse de leur ordre final en plaçant
            le groupe D en premier (dans les bits de poids faible), puis le groupe A,
            puis le groupe B, et enfin le groupe C.

            On aura ainsi une séquence de bits dans l’ordre inverse de leur ordre réel.
            Enfin, on inverse l’ordre de cette séquence de bits en extrayant les bits
            de cette nouvelle séquence obtenue 1 à 1 et on les décale à gauche de une position à chaque fois. */
            for (int i = 0; i < 5; i += 2) {
                groupeD = groupeD << 1 | Bits.extractUInt(altitude, i, 1);
                groupeB = groupeB << 1 | Bits.extractUInt(altitude, i + 1, 1);
                // On fait +6 pour extraire les bit suivant
                groupeA = groupeA << 1 | Bits.extractUInt(altitude, i + 6, 1);
                groupeC = groupeC << 1 | Bits.extractUInt(altitude, i + 6 + 1, 1);
            }
            int invertedShuffle = groupeC << 9 | groupeB << 6 | groupeA << 3 | groupeD;
            for (int j = 0; j < PAYLOAD_SIZE; j++) {
                shuffle = shuffle << 1 | Bits.extractUInt(invertedShuffle, j, 1);
            }

            //  le groupe des 3 bits de poids faible de "shuffle"
            int leastSignificantBits = Bits.extractUInt(shuffle, 0, 3);
            //  le groupe des 9 bits de poids fort de "shuffle"
            int mostSignificantBits = Bits.extractUInt(shuffle, 3, 9);


            int leastSignificantBitsDecoded = (int) decodeGrayCode(leastSignificantBits);
            int mostSignificantBitsDecoded = (int) decodeGrayCode(mostSignificantBits);

            // Si La valeur de lsBitsDecoded est 0, 5 ou 6, alors l'altitude est invalide
            boolean altitudeIsInvalid = leastSignificantBitsDecoded == 0
                    || leastSignificantBitsDecoded == 5
                    || leastSignificantBitsDecoded == 6;

            if (altitudeIsInvalid) {
                return null;
            }

            // Si la valeur représentée par le groupe des bits de poids faible vaut 7, elle est remplacée par 5
            if (leastSignificantBitsDecoded == 7) {
                leastSignificantBitsDecoded = 5;
            }

            /* Si la valeur représentée par le groupe des bits de poids fort est impaire, alors celle représentée par
            le groupe des bits de poids faible est «reflétée», c'est a dire remplacé par 6 - sa valeur originale */
            boolean msbIsOdd = mostSignificantBitsDecoded % 2 != 0;

            if (msbIsOdd) {
                leastSignificantBitsDecoded = 6 - leastSignificantBitsDecoded;
            }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calcul de l'altitude
    --------------------------------------------------------------------------------------------------------------------
    */
            final int baseAltitude = -1300;
            final int multipleOf100 = leastSignificantBitsDecoded * 100;
            final int multipleOf500 = mostSignificantBitsDecoded * 500;
            altitudeInFeet = baseAltitude + multipleOf100 + multipleOf500;
            altitudeInMeters = Units.convertFrom(altitudeInFeet, Units.Length.FOOT);
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création du message de position en vol
    --------------------------------------------------------------------------------------------------------------------
    */
        return new AirbornePositionMessage(rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                altitudeInMeters,
                parity,
                longitudeNormalized,
                latitudeNormalized);
    }

    /**
     * Méthode qui va interpreter un long selon le code de Gray
     *
     * @param grayCode long interpreté
     * @return le long décodé
     */

    private static long decodeGrayCode(long grayCode) {
    /* La condition du while  vérifie si la variable grayCode décalée d'un bit vers la droite n'est pas égale à zéro.
       la boucle s'exécute tant  qu'il reste des bits à traiter.

       Le XOR permet d'inverser les bits de binary à chaque itération de la boucle while en utilisant les bits
       correspondants de grayCode ce qui permet de convertir le code Gray en binaire normal ( en inversant
       les bits de "binary" chaque fois que les bits correspondants de grayCode sont différents) */
        long binary = grayCode;
        while ((grayCode >>= 1) != 0) {
            binary ^= grayCode;
        }
        return binary;
    }

    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }
}