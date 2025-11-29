package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

import static ch.epfl.javions.gui.TileManager.MAX_ZOOM_LEVEL;
import static ch.epfl.javions.gui.TileManager.MIN_ZOOM_LEVEL;

/**
 * Classe qui représente les paramètres de la portion de la carte visible dans l'interface graphique.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class MapParameters {
    private final DoubleProperty minX;
    private final DoubleProperty minY;
    private final IntegerProperty zoom;

    /**
     * @param zoom valeur initiale du niveau de zoom.
     * @param minX valeur initiale de la coordonnée x.
     * @param minY valeur initiale de la coordonnée y.
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(MIN_ZOOM_LEVEL <= zoom && zoom <= MAX_ZOOM_LEVEL);
        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);
    }

    /**
     * Méthode d'accès à la propriété minX en lecture seule.
     *
     * @return la propriété minX en lecture seule.
     */
    public ReadOnlyDoubleProperty getMinXProperty() {
        return minX;
    }


    /**
     * Méthode d'accès à la propriété minY en lecture seule.
     *
     * @return la propriété minY en lecture seule.
     */
    public ReadOnlyDoubleProperty getMinYProperty() {
        return minY;
    }

    /**
     * Méthode d'accès à la propriété zoom en lecture seule.
     *
     * @return la propriété zoom en lecture seule.
     */
    public ReadOnlyIntegerProperty getZoomProperty() {
        return zoom;
    }

    /**
     * Getter public de la valeur contenue dans la propriété minX.
     *
     * @return la valeur de la coordonnée x.
     */
    public double getMinX() {
        return minX.get();
    }

    /**
     * Getter public de la valeur contenue dans la propriété minY.
     *
     * @return la valeur de la coordonnée y.
     */
    public double getMinY() {
        return minY.get();
    }

    /**
     * Getter public de la valeur contenue dans la propriété zoom.
     *
     * @return le niveau de zoom.
     */
    public int getZoom() {
        return zoom.get();
    }

    /**
     * Méthode qui translate le coin haut-gauche de la portion de carte affichée de ce vecteur.
     *
     * @param x la coordonnéee x du vecteur.
     * @param y la coordonnéee y du vecteur.
     */
    public void scroll(double x, double y) {
        minX.set(getMinX() + x);
        minY.set(getMinY() + y);
    }

    /**
     * Méthode qui modifie le niveau de zoom.
     *
     * @param zoomDifference la différence de niveau de zoom.
     */
    public void changeZoomLevel(int zoomDifference) {
        // Dans ce cas, on utilise Math.scalb au lieu d'un décalage car zoomDifference peut etre négative
        float zoomPower = Math.scalb(1, zoomDifference);
        zoom.set(Math2.clamp(MIN_ZOOM_LEVEL,
                getZoom() + zoomDifference,
                MAX_ZOOM_LEVEL)); // On limite la nouvelle valeur du zoom entre MIN_ZOOM_LEVEL et MAX_ZOOM_LEVEL
        minX.set(getMinX() * zoomPower);
        minY.set(getMinY() * zoomPower);
    }
}