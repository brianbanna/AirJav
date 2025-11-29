package ch.epfl.javions;

/**
 * Des préconditions.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class Preconditions {
    /**
     * Constructeur par défaut privé de la classe non instanciable Preconditions.
     */
    private Preconditions() {
    }

    /**
     * Méthode qui lève l'exception IllegalArgumentException si son argument est faux, et ne fait rien sinon.
     *
     * @param shouldBeTrue  la condition à vérifier.
     * @throws IllegalArgumentException  si la condition fournie en paramètre est fausse.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
