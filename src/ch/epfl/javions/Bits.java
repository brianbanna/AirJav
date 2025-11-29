package ch.epfl.javions;

import java.util.Objects;

/**
 * Méthodes d'extraction d'un sous-ensemble de 64 bits.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class Bits {
    /**
     * Constructeur par défaut privé de la classe non instanciable Bits.
     */
    private Bits() {
    }

    /**
     * Extrait une plage de bits.
     *
     * @param value le vecteur de 64 bits.
     * @param start la position du bit auquel au commence.
     * @param size  la taille de la plage de bits à extraire.
     * @return l'entier représenté par la plage de bits commencant au bit d'index start,
     * de taille size et interprétée comme une valeur non signée.
     * @throws IllegalArgumentException  si la taille n'est pas strictement supérieure à 0
     *                                   et strictement inférieure à 32.
     * @throws IndexOutOfBoundsException si la plage décrite par start et size
     *                                   n'est pas totalement comprise entre 0 (inclus) et 64 (exclu).
     */
    public static int extractUInt(long value, int start, int size) {
        boolean sizeIsValid = size > 0 && size < Integer.SIZE;
        Preconditions.checkArgument(sizeIsValid);
        Objects.checkFromIndexSize(start, size, Long.SIZE); // Test de la validité de l’argument

        final long mask = (1 << size) - 1;
        /* On shift les bits à droite de "start" pour avoir tous
        les bits dont on a besoin en tant que bits de poids faible */
        long shiftedValue = value >> start;
        return (int) (shiftedValue & mask);
    }

    /**
     * Teste si un bit à une position donnée est un 1.
     *
     * @param value le vecteur de 64 bits.
     * @param index la position du bit testé.
     * @return vrai si et seulement si le bit à la position 'index' est un 1.
     * @throws IndexOutOfBoundsException si le vecteur n'est pas compris entre 0 (inclus) et 64 (exclu).
     */
    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE); // Test de la validité de l’argument

        final long mask = 1L;
        long shiftedValue = value >> index; // On aura ainsi le bit qu'on veut tester en position 0
        long testValue = shiftedValue & mask;
        return (testValue == 1); // On affiche true si le bit testé est 1
    }
}
