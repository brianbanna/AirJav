package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Chaîne d'octets.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class ByteString {
    private final byte[] bytes; // tableau de bytes
    private final HexFormat hf = HexFormat.of().withUpperCase();

    /**
     * Constructeur public de la classe ByteString avec le tableau de bytes donné.
     *
     * @param bytes tableau de bytes (d'octets)
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone(); // On clone l'attribut bytes pour respecter l'immuabilité
    }

    /**
     * Retourne la chaîne d'octets dont la chaîne passée en argument est la représentation hexadécimale.
     *
     * @param hexString chaine dont on retourne la chaîne d'octets
     * @return la chaîne d'octets dont la chaîne passée en argument est la représentation hexadécimale.
     * @throws IllegalArgumentException si la chaîne donnée n'est pas de longueur paire, ou si elle
     *                                  contient un caractère qui n'est pas un chiffre hexadécimal
     */
    public static ByteString ofHexadecimalString(String hexString) {
        HexFormat hf = HexFormat.of().withUpperCase();
        byte[] bytes = hf.parseHex(hexString);
        return new ByteString(bytes);
    }

    /**
     * Retourne la taille de la chaine
     *
     * @return la taille de la chaine
     */
    public int size() {
        return bytes.length;
    }

    /**
     * Retourne l'octet (interprété en non signé) à l'index donné.
     *
     * @param index la position donné
     * @return l'octet (interprété en non signé) à la position index
     * @throws IndexOutOfBoundsException si index est invalide donc si l'index est négatif
     *                                   ou s'il est superieur a l'index maximal du tableau de bytes
     */
    public int byteAt(int index) {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Test de la validité de l'index
    --------------------------------------------------------------------------------------------------------------------
    */
        if (size() - 1 < index || index < 0) {
            throw new IndexOutOfBoundsException();
        }

        // On extrait 8 bits car on traite l'octet sous forme d'entier non signé
        return Byte.toUnsignedInt(bytes[index]);
    }

    /**
     * Retourne les octets compris entre 2 indexs donnés en paramètre.
     *
     * @param fromIndex l'index de l'octet auquel on commence.
     * @param toIndex   l'index de l'octet auquel on s'arrête.
     * @return retourne les octets compris entre les index fromIndex (inclus) et toIndex (exclu).
     * @throws IndexOutOfBoundsException si la plage décrite par fromIndex et toIndex
     *                                   n'est pas totalement comprise entre 0 et la taille de la chaîne.
     * @throws IllegalArgumentException  si la différence entre toIndex et fromIndex n'est pas
     *                                   strictement inférieure à au nombre d'octets contenus
     *                                   dans une valeur de type long, donc 8.
     */
    public long bytesInRange(int fromIndex, int toIndex) {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Test de la validité des index
    --------------------------------------------------------------------------------------------------------------------
    */
        int size = toIndex - fromIndex; // la taille de la plage de bytes
        boolean sizeIsValid = size < Long.BYTES;
        Objects.checkFromToIndex(fromIndex, toIndex, size());
        Preconditions.checkArgument(sizeIsValid);

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Extraction des bytes de l'index fromIndex jusqu'à l'index toIndex
    --------------------------------------------------------------------------------------------------------------------
    */
        long extractedBytes = 0;
        for (int i = 0; i < size; i++) {
            int indexToExtractByte = fromIndex + i; // On commence à extraire a partir de fromIndex + i
            extractedBytes = (extractedBytes << Byte.SIZE) | byteAt(indexToExtractByte);
            // On shift "extractedBytes" de 8 à gauche (donc de la taille d'un byte)
        }
        return extractedBytes;
    }

    /**
     * Redéfinition de equals qui retourne vrai si et seulement si la valeur passée en argument
     * est une instance de ByteString et que ses octets sont identiques à ceux du récepteur.
     *
     * @param obj0 valeur passée en argument qu'on teste.
     * @return true si la valeur passée en argument est une instance de ByteString
     * et que ses octets sont identiques à ceux du récepteur.
     */
    @Override
    public boolean equals(Object obj0) {
    /*
    ----------------------------------------------------------------------------------------------------------------
      -- Test de la validité de l'objet obj0
    ----------------------------------------------------------------------------------------------------------------
    */
        if (Objects.isNull(obj0)) {
            return false;
        }

        if (obj0 instanceof ByteString obj) {
            return (Arrays.equals(obj.bytes, bytes));
        } else {
            return false;
        }
    }

    /**
     * Redéfinition de hashCode qui retourne la valeur de la
     * méthode hashCode de Arrays mais appliquée au tableau considéré.
     *
     * @return la valeur de la méthode hashCode de Arrays appliquée au tableau considéré.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    /**
     * Redéfinition de toString qui retourne une représentation des octets de la chaîne en hexadécimal.
     *
     * @return une représentation des octets de la chaîne en hexadécimal.
     */
    @Override
    public String toString() {
        return hf.formatHex(bytes);
    }
}
