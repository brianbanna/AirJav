package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

/**
 * Classe qui représente un message ADS-B «brut», c.-à-d. dont l'attribut ME n'a pas encore été analysé.
 *
 * @param timeStampNs horodatage du message, exprimé en nanosecondes depuis une origine donnée généralement l'instant
 *                    correspondant au tout premier échantillon de puissance calculé.
 * @param bytes       les octets du message.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */

public record RawMessage(long timeStampNs, ByteString bytes) {
    public final static int LENGTH = 14; // Longueur en octets des messages ADS-B
    private final static int CA_SIZE = 3;
    private final static int DF_SIZE = 5;
    private final static int DF = 17;

    /**
     * Constructeur compact de RawMessage.
     *
     * @param timeStampNs horodatage du message, exprimé en nanosecondes depuis une origine donnée.
     * @param bytes       les octets du message.
     * @throws IllegalArgumentException si l'horodatage est (strictement) négatif.
     * @throws IllegalArgumentException si la chaîne d'octets ne contient pas LENGTH octets.
     */
    public RawMessage {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Vérification des conditions
    --------------------------------------------------------------------------------------------------------------------
    */
        boolean timeStampsIsPositive = timeStampNs >= 0;
        boolean byteSizeIs14 = bytes.size() == LENGTH;
        Preconditions.checkArgument(timeStampsIsPositive);
        Preconditions.checkArgument(byteSizeIs14);
    }


    /**
     * @param timeStampNs horodatage du message, exprimé en nanosecondes depuis une origine donnée.
     * @param bytes       les octets du message.
     * @return le message ADS-B brut avec l'horodatage et les octets donnés ou null si le CRC24 des octets ne vaut pas 0
     */
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        int crc = crc24.crc(bytes); // crc des octets
        boolean crcIsZero = (crc == 0);
        if (crcIsZero) {
            return new RawMessage(timeStampNs, new ByteString(bytes));
        } else {
            return null;
        }
    }

    /**
     * Methode qui retourne la taille d'un message dont le premier octet est celui donné,
     * et qui vaut LENGTH si l'attribut DF contenu dans ce premier octet vaut 17, et 0 sinon—
     * indiquant que le message n'est pas d'un type connu.
     *
     * @param byte0 premier octet d'un message.
     * @return LENGTH si l'attribut DF contenu dans ce premier octet vaut 17, et 0 sinon.
     */
    public static int size(byte byte0) {
        int downlinkFormat = Bits.extractUInt(byte0, CA_SIZE, DF_SIZE);
        if (downlinkFormat == DF) {
            return LENGTH;
        } else return 0;
    }

    /**
     * Retourne le code de type de l'attribut ME passé en argument.
     *
     * @param payload attribut ME dont on retourne le code de type.
     * @return le code de type de l'attribut ME passé en argument.
     */
    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, 51, DF_SIZE);
    }

    /**
     * Retourne le format du message, c.-à-d. l'attribut DF stocké dans son premier octet.
     *
     * @return l'attribut DF stocké dans son premier octet.
     */
    public int downLinkFormat() {
        byte byte0 = (byte) bytes.byteAt(0);
        return Bits.extractUInt(byte0, CA_SIZE, DF_SIZE); // downlinkFormat
    }

    /**
     * Methode qui retourne l'adresse OACI de l'expéditeur du message.
     *
     * @return l'adresse OACI de l'expéditeur du message.
     */
    public IcaoAddress icaoAddress() {
    /* Cette méthode extrait les  octets d'indice 1 à 3 de bytes ,les convertit en une chaîne hexadécimale
    (Long.toHexString()) et les stocke dans un objet StringBuilder. Ensuite, la méthode ajoute des zéros à  gauche
    jusqu'à ce que la chaîne hexadécimale atteigne une longueur de 6 caractères (la longueur  de l'adresse ICAO) en
    utilisant une boucle while et la méthode insert() de StringBuilder. */
        StringBuilder icao = new StringBuilder(Long.toHexString(bytes.bytesInRange(1, 4)));
        while (icao.length() < 6) {
            icao.insert(0, "0");
        }
        // Conversion de la chaine en majuscule
        return new IcaoAddress(icao.toString().toUpperCase());
    }

    /**
     * Methode qui retourne la "charge utile" du message c.-à-d. l'attribut ME.
     *
     * @return l'attribut ME du message.
     */
    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    /**
     * Methode qui retourne le code de type du message, c.-à-d. les cinq bits de poids le plus fort de son attribut ME.
     *
     * @return les cinq bits de poids le plus fort de l'attribut ME du message.
     */
    public int typeCode() {
        long payload = bytes.bytesInRange(4, 11);
        return Bits.extractUInt(payload, 51, DF_SIZE);
    }
}




