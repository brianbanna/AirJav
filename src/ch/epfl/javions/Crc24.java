package ch.epfl.javions;

/**
 * Calculateur de CRC de 24 bits.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class Crc24 {
    public final static int GENERATOR = 0xFFF409; // Le generateur de CRC24
    private static final int ENTRIES = 256; // Les entrees correspondant a un generateur
    private static final int GENERATOR_SIZE = 24;
    private static final int MASK = 0xFFFFFF;
    private final int[] table;

    /**
     * Retourne un calculateur de CRC24.
     *
     * @param generator générateur dont on va utiliser les 24 bits de poids faible.
     */
    public Crc24(int generator) {
        table = buildTable(generator);
    }

    /**
     * Calcul de CRC24.
     *
     * @param generator générateur.
     * @param bytes     tableau de bytes dont on calcule le CRC24.
     * @return le CRC24 du tableau de bytes 'bytes'.
     */

    private static int crc_bitwise(int generator, byte[] bytes) {
        int crc = 0;
        byte[] augmentation = {0, 0, 0}; // Les 3 bytes nuls ajoutés pour augmenter le message
        int lsbGenerator = Bits.extractUInt(generator, 0, GENERATOR_SIZE);
        int[] table = {0, lsbGenerator};

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Algorithme de base (bit par bit) qui utlise un message augmenté
    --------------------------------------------------------------------------------------------------------------------
    */

        // Boucle qui parcourt les bytes du message
        for (byte aByte : bytes) {
            // On passe 8 fois (8 - 1 = 7) de gauche à droite avec le j--
            for (int j = Byte.SIZE - 1; j >= 0; j--) {
                int shiftedCrc = crc << 1;
                int bitAugmentedMessage = Bits.extractUInt(aByte, j, 1);
                int crcMsbBit = Bits.extractUInt(crc, 23, 1);

                crc = (shiftedCrc | bitAugmentedMessage)
                        ^ table[crcMsbBit];
            }
        }

        // Boucle simplifiée de la première boucle (pour parcourir les 3 bytes nuls)
        for (int zeroByte : augmentation) {
            /* Dans ce cas, passer de gauche à droite ou de droite à gauche
             revient au même (on parcourt un tableau de zeros) */
            for (int j = Byte.SIZE - 1; j >= 0; j--) {
                int shiftedCrc = crc << 1;
                int bitAugmentedMessage = Bits.extractUInt(zeroByte, j, 1);
                int crcMsbBit = Bits.extractUInt(crc, 23, 1);

                crc = (shiftedCrc | bitAugmentedMessage)
                        ^ table[crcMsbBit];
            }
        }

        return crc & MASK; // On retourne le CRC sous forme d'entier non signé
    }

    /**
     * Construit la table de 256 entrées correspondant au générateur 'generator'
     *
     * @param generator le générateur
     * @return la table de 256 entrées correspondant au générateur 'generator'
     */
    private static int[] buildTable(int generator) {
        int[] table = new int[ENTRIES];
        for (int i = 0; i < ENTRIES; i++) {
            byte[] temp = {(byte) i};
            // On transtype l'entier en un byte et on le place dans un tableau pour l'utiliser dans crc_bitwise
            table[i] = crc_bitwise(generator, temp);
        }
        return table;
    }

    /**
     * Retourne le CRC24 du tableau d'octets donné
     *
     * @param bytes tableau d'octets (de bytes)
     * @return le CRC24 du parametre bytes
     */
    public int crc(byte[] bytes) {
        int crc = 0;
        int MASK = 0xFFFFFF;

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Algorithme optimisé du calculateur de CRC (qui utlise un message augmenté)
    --------------------------------------------------------------------------------------------------------------------
    */

        // Boucle qui parcourt les bytes du message
        for (byte aByte : bytes) {
            int shiftedCrc = crc << Byte.SIZE;
            int byteAugmentedMessage = Bits.extractUInt(aByte, 0, Byte.SIZE);
            int crcMsbByte = Bits.extractUInt(crc, 16, Byte.SIZE);

            crc = (shiftedCrc | byteAugmentedMessage)
                    ^ table[crcMsbByte];
        }

        // Boucle simplifiée de la première boucle (pour parcourir les 3 bytes nuls)
        for (int k = 0; k < 3; k++) {
            int shiftedCrc = crc << Byte.SIZE;
            int crcMsbByte = Bits.extractUInt(crc, Short.SIZE, Byte.SIZE);
            crc = (shiftedCrc) ^ table[crcMsbByte];
        }

        int lsbBitsCrc = Bits.extractUInt(crc, 0, GENERATOR_SIZE);
        return lsbBitsCrc & MASK; // On retourne les 24 bits de poids faible du CRC sous forme d'entier non signé
    }
}
