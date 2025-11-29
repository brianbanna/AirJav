package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classe qui représente un calculateur de puissance.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class PowerComputer {
    private final int batchSize;
    private final SamplesDecoder samplesDecoder;
    // Le tableau d'octets issus des échantillons décodés
    private final short[] samplesTab;

    // Le tableau contenant les 8 derniers échantillons produits par la radio
    private final short[] eightValueTab = new short[8];

    /**
     * Constructeur de la classe PowerComputer qui retourne un calculateur de puissance utilisant
     * le flot d'entrée donné pour obtenir les octets de la radio AirSpy et produisant
     * des échantillons de puissance par lots de taille donnée.
     *
     * @param stream    flot des données.
     * @param batchSize taille des échantillons.
     * @throws IllegalArgumentException si la taille des lots n'est pas strictement positive.
     * @throws NullPointerException     si le flot est nul.
     */
    public PowerComputer(InputStream stream, int batchSize) {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Teste la validité de la taille des échantillons.
    --------------------------------------------------------------------------------------------------------------------
    */
        boolean batchSizeIsAMultipleOf8 = (batchSize % 8 == 0);
        boolean batchSizeIsStriclyPositive = (batchSize > 0);
        Preconditions.checkArgument(batchSizeIsAMultipleOf8
                && batchSizeIsStriclyPositive);

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Instanciation des variables.
    --------------------------------------------------------------------------------------------------------------------
    */
        this.batchSize = batchSize;
        int samplesTabSize = 2 * batchSize; // La taille du tableau samplesTab
        samplesTab = new short[samplesTabSize];
        samplesDecoder = new SamplesDecoder(stream, samplesTabSize);
    }

    /**
     * Méthode qui lit depuis le décodeur d'échantillons le nombre d'échantillons nécessaire au calcul
     * d'un lot d'échantillons de puissance, puis les calcule avant de les placer dans le tableau "batch".
     *
     * @param batch tableau de valeurs short qui contient le résulat des calculs de puissance.
     * @return le nombre d'échantillons de puissance placés dans le tableau.
     * @throws IOException              en cas d'erreur d'entrée/sortie.
     * @throws IllegalArgumentException si la taille du tableau passé en argument
     *                                  n'est pas égale à la taille d'un lot.
     */
    public int readBatch(int[] batch) throws IOException {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Teste la validité du flot de données et de la taille des échantillons.
    --------------------------------------------------------------------------------------------------------------------
    */
        boolean batchLengthIsValid = batch.length == batchSize;
        Preconditions.checkArgument(batchLengthIsValid);

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Calculateur de puissance gràce au "filtrage Passe-Bas" et la Décimation.
    --------------------------------------------------------------------------------------------------------------------
    */
        int nbSamples = samplesDecoder.readBatch(samplesTab); // Le nombre d'éléments effectivement convertis

        /* index représente l'indice des éléments dans le tableau "eightValueTab"
        c'est à dire le tableau qui contient les 8 derniers échantillons produits par la radio */
        int index = 0;

        for (int j = 0; j < nbSamples; j += 2) {
            // On incrémente j de 2 à chaque passage car on parcourt le tableau 2 à 2 (les éléments pairs et impairs)

            /* Si index est égal à 8, on le réinitialise à 0 pour pouvoir
            modifier les éléments du tableau des le début. Ceci donne au tableau
            l'aspect circulaire, puisqu'on remplace les plus anciens éléments
            par de nouveaux éléments aux lignes 93 - 94 */
            if (index == 8) {
                index = 0;
            }
            eightValueTab[index] = samplesTab[j];
            eightValueTab[index + 1] = samplesTab[j + 1];

            int evenIndexElements = eightValueTab[6] - eightValueTab[4] + eightValueTab[2] - eightValueTab[0];
            int i = (int) Math.pow(evenIndexElements, 2);

            int oddIndexElements = eightValueTab[7] - eightValueTab[5] + eightValueTab[3] - eightValueTab[1];
            int q = (int) Math.pow(oddIndexElements, 2);

            /* On remplit à l'index j/2 car le tableau batch est de taille "batchSize"
            et le tableau samplesTab est de taille "batchSize * 2" */
            batch[j / 2] = q + i;
            index += 2;
        }

        return nbSamples / Short.BYTES;
    }
}