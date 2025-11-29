package ch.epfl.javions;

/**
 * Des calculs mathématiques.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class Math2 {
    /**
     * Constructeur par défaut privé de la classe non instanciable Math2.
     */
    private Math2() {
    }

    /**
     * Limite la valeur v à l'intervalle allant de min à max.
     *
     * @param min la borne inférieure de l'intervalle.
     * @param v   la valeur considéréee.
     * @param max la borne supéerieure de l'intervalle.
     * @return min si v est inférieure à min, max si v est supérieure à max, et v sinon.
     * @throws IllegalArgumentException si min est strictement supérieur à max.
     */
    public static int clamp(int min, int v, int max) {
        boolean maxIsGreaterThanMin = min <= max;
        Preconditions.checkArgument(maxIsGreaterThanMin);
        return Math.max(min, Math.min(v, max));
    }

    /**
     * Retourne le sinus hyperbolique réciproque.
     *
     * @param x l'argument du sinus hyperbolique réciproque.
     * @return le sinus hyperbolique réciproque de l'argument x.
     */
    public static double asinh(double x) {
        double arg = x + Math.hypot(1, x);
        return Math.log(arg);
    }
}
