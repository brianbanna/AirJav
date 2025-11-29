package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Classe qui représente un dégradé de couleurs.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class ColorRamp {
    // Le dégradé Plasma
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
    private final static int MAX_ALTITUDE = 12_000;
    private static final double MAX_INDEX = 1d;
    private static final int MIN_NUMBER_COLORS = 2;
    private final List<Color> colors;
    private final double elementsSpacing;

    /**
     * Constructeur de la classe ColorRamp.
     *
     * @param colors séquence de couleurs JavaFX, passée sous la forme
     *               d'un nombre variable d'arguments ou éventuellement d'une liste.
     * @throws IllegalArgumentException si le nombre de couleurs n'est pas supérieur ou égal a 2.
     */
    public ColorRamp(Color... colors) {
        Preconditions.checkArgument(colors.length >= MIN_NUMBER_COLORS);
        this.colors = List.copyOf(List.of(colors)); // On crée une liste immuable
        // On divise l'unité par le nombre de couleurs pour avoir la distance.
        elementsSpacing = MAX_INDEX / (colors.length - 1);
    }

    /**
     * La fonction utlisée pour calculer la valeur passée au dégradé afin d'obtenir la couleur correspondante.
     *
     * @param altitude l'altitude courante.
     * @return la valeur passée au dégradé afin d'obtenir l'index de la couleur correspondante.
     */
    public static double plasmaColorFunction(double altitude) {
        return Math.cbrt(altitude / MAX_ALTITUDE);
    }

    /**
     * Méthode qui retourne la couleur qui correspond a un nombre.
     *
     * @param index le nombre dont on obtient la couleur
     * @return la couleur qui correspond au nombre index.
     */
    public Color at(double index) {
        if (index <= 0d) {
            return colors.get(0);

        } else if (index >= MAX_INDEX) {
            return colors.get(colors.size() - 1);

        } else {
            return mixColors(index);
        }
    }

    /**
     * Méthode qui permet d'obtenir le mélange entre 2 couleurs.
     *
     * @param index l'index donné.
     * @return la couleur mixte.
     */
    private Color mixColors(double index) {
        double colorProportion = (index % elementsSpacing) / elementsSpacing;
        int lowerBound = (int) Math.floor(index / elementsSpacing);
        int upperBound = (int) Math.ceil(index / elementsSpacing);
        Color color1 = colors.get(lowerBound);
        Color color2 = colors.get(upperBound);

        return color1.interpolate(color2, colorProportion);
    }
}
