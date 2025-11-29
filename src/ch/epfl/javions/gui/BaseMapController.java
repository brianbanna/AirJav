package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Math2;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

import static ch.epfl.javions.gui.TileManager.MAX_ZOOM_LEVEL;
import static ch.epfl.javions.gui.TileManager.MIN_ZOOM_LEVEL;

/**
 * Classe qui gère l'affichage et l'interaction avec le fond de carte
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class BaseMapController {
    private final static int TILE_SIZE = 256;
    private static final int DELAY_SCROLLING = 200;
    private final Canvas canvas;
    private final MapParameters mapParameters;
    private final ObjectProperty<Point2D> mouseCoordinatesWhenPressed;
    private final Pane pane;
    private final TileManager tileManager;
    private boolean redrawIsNeeded;


    /**
     * Constructeur de la classe BaseMapController
     *
     * @param tileManager   gestionnaire de tuiles à utiliser pour obtenir les tuiles de la carte
     * @param mapParameters paramètres de la portion visible de la carte
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        this.mouseCoordinatesWhenPressed = new SimpleObjectProperty<>();


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Panneau
    --------------------------------------------------------------------------------------------------------------------
    */
        // Stockage des coordonnées de la souris quand elle est cliquée
        pane.setOnMousePressed(event ->
                mouseCoordinatesWhenPressed.setValue(new Point2D(
                        event.getX(),
                        event.getY())));

        // Ajout de Listeners au Panneau qui appellent redrawOnNextPulse si les dimensions du panneau changent
        pane.widthProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        pane.heightProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Canvas
    --------------------------------------------------------------------------------------------------------------------
    */
        //Mise en place de liens JavaFX entre les dimensions du canvas et celles du panneaux
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // JavaFX appelle redrawIfNeeded a chaque battement
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Gestion du Scroll/ du Zoom
    --------------------------------------------------------------------------------------------------------------------
    */
        //Limitation de la fréquence à laquelle les changements de zoom peuvent avoir lieu
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + DELAY_SCROLLING);

            // On effectue deux translations du coin haut-gauche de la portion visible de la carte
            int currentZoom = mapParameters.getZoom();
            int newZoomLevel = Math2.clamp(MIN_ZOOM_LEVEL, currentZoom + zoomDelta, MAX_ZOOM_LEVEL);
            int difference = newZoomLevel - currentZoom;

            mapParameters.scroll(e.getX(), e.getY()); //  1ere translation,avant le changement du zoom
            mapParameters.changeZoomLevel(difference); // changement du zoom
            // 2eme translation, apres le changement du zoom,pour annuler la 1ere
            mapParameters.scroll(-e.getX(), -e.getY());
        });

        /* On ajoute un écouteur d'événements à l'objet pane. Lorsque l'utilisateur fait glisser la souris,
        la méthode scroll() est appelée avec la difference entre coordonnées de la souris a cet instant et les
        coordonnees de la souris lorsqu'elle est cliquee */
        pane.setOnMouseDragged(event -> {
            Point2D point = new Point2D(0, 0);
            point = point.add(mouseCoordinatesWhenPressed.get().subtract(event.getX(), event.getY()));
            mapParameters.scroll((point.getX()), point.getY());

            //Mise à jour des coordonnées de la souris
            mouseCoordinatesWhenPressed.setValue(new Point2D(event.getX(), event.getY()));
        });


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Ajout d'auditeurs JavaFX aux mapParameters (minX,minY,zoom) qui détectent les situations dans lesquelles
         le fond de carte doit être redessiné (changement de l'une de ces variables)
    --------------------------------------------------------------------------------------------------------------------
    */
        mapParameters.getMinXProperty().addListener((obs, oldValue, newValue) -> redrawOnNextPulse());
        mapParameters.getMinYProperty().addListener((obs, oldValue, newValue) -> redrawOnNextPulse());
        mapParameters.getZoomProperty().addListener((obs, oldValue, newValue) -> redrawOnNextPulse());
        redrawOnNextPulse();
    }


    /**
     * Methode qui se charge de redessiner la carte dans le cas ou redrawIsNeeded est vrai
     */
    private void redrawIfNeeded() {
        if (redrawIsNeeded) {
            redrawIsNeeded = false;

            GraphicsContext graphicsContext = canvas.getGraphicsContext2D(); //Contexte Graphique du canevas


            int tileXCoordinate = (int) Math.floor(mapParameters.getMinX() / TILE_SIZE); // index d'abscisse de la tuile
            int tileYCoordinate = (int) Math.floor(mapParameters.getMinY() / TILE_SIZE); //index d'ordonnee de la tuile
            int xMax = (int) Math.ceil(canvas.getWidth() / TILE_SIZE); // index maximal d'abcisse de la tuile
            int yMax = (int) Math.ceil(canvas.getHeight() / TILE_SIZE); //index maximal d'ordonnee de la tuile


            //On itere et on dessine toutes les tuiles du canvas
            for (int i = 0; i <= xMax; i++) {
                for (int j = 0; j <= yMax; j++) {
                    try {
                        graphicsContext.drawImage(tileManager.imageForTileAt(
                                        new TileManager.TileId(mapParameters.getZoom(),
                                                i + tileXCoordinate,
                                                j + tileYCoordinate)),
                                (i + tileXCoordinate) * TILE_SIZE - mapParameters.getMinX(),
                                (j + tileYCoordinate) * TILE_SIZE - mapParameters.getMinY());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Methode qui demande un redessin au prochain battement
     */
    private void redrawOnNextPulse() {
        redrawIsNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Methode qui retourne le panneau affichant le fond de la carte
     *
     * @return le panneau JavaFX affichant le fond de carte
     */
    public Pane getPane() {
        return pane;
    }

    /**
     * Une méthode qui déplace la portion visible de la carte afin qu'elle soit centrée en ce point.
     *
     * @param geoPos un point à la surface de la Terre.
     */
    public void centerOn(GeoPos geoPos) {
        // On divise la largeur et la longueur du canvas par 2 pour obtenir le centre du canvas
        double xCoordinate = WebMercator.x
                (mapParameters.getZoom(),
                        geoPos.longitude()) - (mapParameters.getMinX() + canvas.getWidth() / 2d);
        double yCoordinate = WebMercator.y
                (mapParameters.getZoom(),
                        geoPos.latitude()) - (mapParameters.getMinY() + canvas.getHeight() / 2d);

        mapParameters.scroll(xCoordinate, yCoordinate);
    }
}
