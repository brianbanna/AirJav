package ch.epfl.javions.demodulation;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Classe qui représente un décodeur d'échantillons.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class SamplesDecoder {
    private final static int BIAIS = 2048; // La valeur du biais
    private final int batchSize;
    private final int byteTableSize; // La taille du tableau byteTable
    private final InputStream stream; // Le flot des données
    private final byte[] byteTable; // Le tableau d'octets issus des échantillons

    /**
     * Constructeur de SamplesDecoder qui instancie les variables batchSize, stream, et byteTable.
     *
     * @param stream    flot des données.
     * @param batchSize taille des échantillons.
     * @throws IllegalArgumentException si la taille des lots n'est pas strictement positive.
     * @throws NullPointerException     si le flot est nul.
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Teste la validité du flot de données et de la taille des échantillons.
    --------------------------------------------------------------------------------------------------------------------
    */
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Instanciation des variables.
    --------------------------------------------------------------------------------------------------------------------
    */
        this.batchSize = batchSize;
        this.stream = stream;
        this.byteTableSize = 2 * batchSize;
        this.byteTable = new byte[byteTableSize];
        // la taille du tableau est 2 * batchSize car on a 2 octets par échantillon
    }

    /**
     * Méthode qui lit depuis le flot le nombre d'octets correspondant à un lot, puis convertit
     * ces octets en échantillons signés, avant de les placer dans le tableau "batch".
     *
     * @param batch tableau de valeurs short dans lequel on place les échantillons.
     * @return le nombre d'éléments effectivement convertis.
     * @throws IOException              s'il y'a une erreur d'entrée ou de sortie.
     * @throws IllegalArgumentException si la taille du tableau passé en argument
     *                                  n'est pas égale à la taille d'un lot.
     */
    public int readBatch(short[] batch) throws IOException {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Teste la validité du tableau de shorts "batch" fourni en paramètre.
    --------------------------------------------------------------------------------------------------------------------
    */
        boolean batchSizeIsValid = batch.length == batchSize;
        Preconditions.checkArgument(batchSizeIsValid);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Transformation des octets grâce à la technique little-endian.
    --------------------------------------------------------------------------------------------------------------------
    */
        int batchIndex = 0; // L'index des des éléments du tableau batch
        int totalElements = stream.readNBytes(byteTable, 0, byteTableSize);
        // On crée une instance de ByteString pour utiliser la méthode byteAt
        ByteString byteString = new ByteString(byteTable);

        for (int byteTableIndex = 0; byteTableIndex < totalElements; byteTableIndex += 2) {
            /* Le tableau byteTable a pour taille batchSize * 2 mais le tableau batch a pour taille batchSize.
             C'est pour cela qu'on incrémente l'index des éléments du tableau byteTable de 2
             et l'index des éléments du tableau batch de 1*/

            int lsbByte = byteString.byteAt(byteTableIndex + 1); // Les 8 bits de poids faible
            int msbByte = byteString.byteAt(byteTableIndex); // Les 8 bits de poids fort
            // On décale les 8 bits de poids faible à comme indiquée par la technique little-endian
            int lsbByteShifted = lsbByte << Byte.SIZE;

            int temp = (lsbByteShifted | msbByte)
                    - BIAIS;
            batch[batchIndex] = (short) (temp); // On extrait les 16 bits (2 octets) de poids faible
            batchIndex++;
        }

        return totalElements / Short.BYTES;
    }
}
