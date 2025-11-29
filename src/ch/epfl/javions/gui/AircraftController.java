package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Objects;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * Classe qui gère la vue des aéronefs.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class AircraftController {
    private static final AircraftDescription EMPTY_AIRCRAFT_DESCRIPTION = new AircraftDescription("");
    private static final AircraftTypeDesignator EMPTY_AIRCRAFT_TYPE_DESIGNATOR = new AircraftTypeDesignator("");
    private static final double NO_ROTATION_ANGLE = 0d;
    private static final int ABOVE_CONSTANT = 0;
    private static final int BELOW_CONSTANT = 1;
    private static final int MARGIN = 4;
    private static final int MINIMUM_ZOOM_LEVEL = 11;
    private final ObjectProperty<ObservableAircraftState> aircraftStateProperty;
    private final Pane pane;
    private ChangeListener<Number> minXListener;
    private ChangeListener<Number> minYListener;
    private ChangeListener<Number> zoomListener;

    /**
     * Constructeur de la classe AircraftController.
     *
     * @param mapParameters         les paramètres de la portion de la carte visible à l'écran.
     * @param states                l'ensemble (observable mais non modifiable)des états des aéronefs qui doivent
     *                              apparaître sur la vue.
     * @param aircraftStateProperty propriété JavaFX contenant l'état de l'aéronef sélectionné.
     */
    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {
        this.pane = new Pane(new Canvas());
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");
        this.aircraftStateProperty = aircraftStateProperty;

        addAircraftStateListener(states, mapParameters);
    }

    /**
     * Getter public du panneau.
     *
     * @return le panneau.
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Getter public de la valeur contenue dans aircraftStateProperty.
     *
     * @return l'etat de l'aeronef.
     */
    private ObservableAircraftState getObservableAircraftState() {
        return this.aircraftStateProperty.get();
    }

    /**
     * Methode d'ajout d'un auditeur sur l'ensemble observable passé au constructeur
     */
    private void addAircraftStateListener(ObservableSet<ObservableAircraftState> states,
                                          MapParameters mapParameters) {
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                // Code à exécuter si un état d'aéronef a été ajouté a l'ensemble
                ObservableAircraftState state = change.getElementAdded();
                aeronefCreator(state, mapParameters);
            } else if (change.wasRemoved()) {
                // Code à exécuter si un état d'aeronef a été supprimé de l'ensemble
                ObservableAircraftState state = change.getElementRemoved();
                aeronefRemover(state);
            }
        });
    }

    /**
     * Méthode qui se charge de créer le graphe de scène de la vue d'un aéronef.
     *
     * @param aircraftState l'état de l'aéronef sélectionné.
     */
    private void aeronefCreator(ObservableAircraftState aircraftState,
                                MapParameters mapParameters) {
        final Group aircraftGroup = new Group();

        // On ajoute l'adresse OACI a l'identité du noeud de l'aéronef
        aircraftGroup.setId(aircraftState.getIcaoAddress().string());
        // On crée une propriété avec l'atitude de l'aéronef
        DoubleProperty altitudeProperty = new SimpleDoubleProperty(aircraftState.altitudeProperty().getValue());

        // Afin qu'un aéronef volant a une altitude plus élevée soit dessiné au dessus
        aircraftGroup.viewOrderProperty().bind(altitudeProperty.negate());

        // On ajoute au groupe de l'aéronef sa trajectoire et le groupe de l'étiquette + icone
        createTrajectory(aircraftState, aircraftGroup, mapParameters);
        aircraftGroup.getChildren().add(createAircraftDesign(aircraftState, mapParameters));

        pane.getChildren().add(aircraftGroup); // On ajoute l'aéronef créé au panneau
    }

    /**
     * Méthode qui se charge d'effacer le groupe correspondant à un état d'aéronef donné
     *
     * @param aircraftState état d'aéronef que l'on doit enlever du graphe de scene
     */
    private void aeronefRemover(ObservableAircraftState aircraftState) {
        //On recherche le groupe correspondant à cet aircraftState à travers son icaoAdress
        String icaoAdress = aircraftState.getIcaoAddress().string();
        pane.getChildren().removeIf(child -> icaoAdress.equals(child.getId()));
    }

    /**
     * Méthode qui se charge de la création de la trajectoire
     *
     * @param aircraftState état de l'aeronef sélectionné
     * @param group         groupe correspondant à l'aéronef
     */
    private void createTrajectory(ObservableAircraftState aircraftState,
                                  Group group,
                                  MapParameters mapParameters) {
        Group trajectory = new Group();
        trajectory.getStyleClass().add("trajectory");
        ObservableList<ObservableAircraftState.AirbornePos> observableTrajectory
                = aircraftState.getObservableTrajectory();

        trajectory.visibleProperty().bind(Bindings.createBooleanBinding(() -> aircraftState.equals(
                        getObservableAircraftState()),
                this.aircraftStateProperty));
        repositionNodes(trajectory, mapParameters);
        // Ajout d'un auditeur sur la visibleProperty de la trajectoire
        trajectory.visibleProperty().addListener((observable, oldValue, newValue) -> {
            // Trajectoire est visible
            if (newValue) {
                // Mise en place d'un auditeur sur la vue non modifiable de la liste des trajectoires
                reconstructLines(trajectory, aircraftState, observableTrajectory, mapParameters);
                observableTrajectory.addListener((ListChangeListener<ObservableAircraftState.AirbornePos>) change
                        -> reconstructLines(trajectory, aircraftState, observableTrajectory, mapParameters));
                setListeners(trajectory, aircraftState, mapParameters);

                // Trajectoire est invisible
            } else {
                removeListeners(mapParameters);
                trajectory.getChildren().clear();
            }
        });
        trajectory.setViewOrder(BELOW_CONSTANT); //On place la trajectoire en dessous de l'icone et l'étiquette
        group.getChildren().add(trajectory);
    }

    /**
     * Méthode qui se charge de reconstruire les lignes représentant la trajectoire de l'aéronef lors du changement
     * du niveau de zoom
     *
     * @param trajectory    groupe représentant la trajectoire de l'aéronef.
     * @param aircraftState état de l'aéronef.
     */
    private void reconstructLines(Group trajectory,
                                  ObservableAircraftState aircraftState,
                                  ObservableList<ObservableAircraftState.AirbornePos> observableTraj,
                                  MapParameters mapParameters) {
        //On efface tous les anciens elements du groupe de la trajectoire (lignes) pour y reconstruire les nouveaux
        trajectory.getChildren().clear();

        for (int i = 0; i < aircraftState.getObservableTrajectory().size() - 1; i += 1) {
            ObservableAircraftState.AirbornePos startPos = observableTraj.get(i);
            ObservableAircraftState.AirbornePos endPos = observableTraj.get(i + 1);
            Line line = new Line();

            //Coordonnées du point de début de la ligne (1ere position)
            line.setStartX(WebMercator.x(
                    mapParameters.getZoom(),
                    startPos.position().longitude()));
            line.setStartY((WebMercator.y(
                    mapParameters.getZoom(),
                    startPos.position().latitude())));
            //Coordonnées du point de fin de la ligne (2e Position)
            line.setEndX(WebMercator.x(
                    mapParameters.getZoom(),
                    endPos.position().longitude()));
            line.setEndY(WebMercator.y(
                    mapParameters.getZoom(),
                    endPos.position().latitude()));

            trajectory.getChildren().add(line);
            Stop s1 = new Stop(0, ColorRamp.PLASMA.at(ColorRamp.plasmaColorFunction(startPos.altitude())));
            Stop s2 = new Stop(1, ColorRamp.PLASMA.at(ColorRamp.plasmaColorFunction(endPos.altitude())));
            line.setStroke(new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1,
                    s2));
        }
    }

    /**
     * Méthode qui permet le repositionnement des lignes de la trajectoire lors du changement de minX ou de minY
     *
     * @param group groupe auquel on va appliquer cette translation
     */
    private void repositionNodes(Group group,
                                 MapParameters mapParameters) {
        group.layoutXProperty().bind(mapParameters.getMinXProperty().negate());
        group.layoutYProperty().bind(mapParameters.getMinYProperty().negate());
    }

    /**
     * Méthode qui se charge de créer le groupe contenant l'étiquette et l'icone.
     *
     * @param aircraftState l'état de l'aéronef sélectionné.
     * @return le groupe contenant l'étiquette et l'icone.
     */
    private Group createAircraftDesign(ObservableAircraftState aircraftState,
                                       MapParameters mapParameters) {
        final Group aircraftDesign = new Group(); // Groupe de l'icône et de l'étiquette
        aircraftDesign.setViewOrder(ABOVE_CONSTANT); //On place  l'icône et  l'étiquette en dessous de la trajectoire

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Positionnement du groupe de l'icône et de l'étiquette sur la carte.
    --------------------------------------------------------------------------------------------------------------------
    */
        aircraftDesign.layoutXProperty().bind(Bindings.createDoubleBinding(() -> WebMercator.x(
                        mapParameters.getZoom(),
                        aircraftState.getPosition().longitude()) - mapParameters.getMinX(),
                mapParameters.getZoomProperty(),
                mapParameters.getMinXProperty(),
                mapParameters.getMinYProperty(),
                aircraftState.positionProperty()));

        aircraftDesign.layoutYProperty().bind(Bindings.createDoubleBinding(() -> WebMercator.y(
                        mapParameters.getZoom(),
                        aircraftState.getPosition().latitude()) - mapParameters.getMinY(),
                mapParameters.getZoomProperty(),
                mapParameters.getMinXProperty(),
                mapParameters.getMinYProperty(),
                aircraftState.positionProperty()));


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création de l'icône.
    --------------------------------------------------------------------------------------------------------------------
    */
        final SVGPath aircraftIcon = iconCreator(aircraftState);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création de l'étiquette.
    --------------------------------------------------------------------------------------------------------------------
    */
        final Group label = labelCreator(aircraftState, aircraftIcon, mapParameters);

        aircraftDesign.getChildren().add(label);
        aircraftDesign.getChildren().add(aircraftIcon);
        return aircraftDesign;
    }

    /**
     * Méthode qui se charge de créer l'icone.
     *
     * @param aircraftState l'état de l'aéronef sélectionné.
     * @return l'icone associée a l'aéronef sélectionné.
     */
    private SVGPath iconCreator(ObservableAircraftState aircraftState) {
        final SVGPath aircraft = new SVGPath();
        // On ajoute la classe de style aircraft a l'icône
        aircraft.getStyleClass().add("aircraft");
        AircraftData aircraftData = aircraftState.getAircraftData();


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Dessin de l'icône.
    --------------------------------------------------------------------------------------------------------------------
    */
        AircraftTypeDesignator aircraftTypeDesignator = aircraftData != null ?
                aircraftData.typeDesignator() :
                EMPTY_AIRCRAFT_TYPE_DESIGNATOR;
        AircraftDescription aircraftDescription = aircraftData != null ?
                aircraftData.description() :
                EMPTY_AIRCRAFT_DESCRIPTION;
        WakeTurbulenceCategory wakeTurbulenceCategory = aircraftData != null ?
                aircraftData.wakeTurbulenceCategory() :
                WakeTurbulenceCategory.UNKNOWN;

        ObservableValue<AircraftIcon> aircraftIcon = aircraftState.categoryProperty().map(c -> AircraftIcon.iconFor(
                aircraftTypeDesignator,
                aircraftDescription,
                c.intValue(),
                wakeTurbulenceCategory));

        aircraft.contentProperty().bind(aircraftIcon.map(AircraftIcon::svgPath));


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Rotation de l'icône.
    --------------------------------------------------------------------------------------------------------------------
    */
        /* Si l'aéronef peut tourner, on lie sa rotation au cap de l'aeronef en degré.
        Sinon, la rotation de l'aéronef est de 0, c'est a dire qu'il reste sur sa meme trajectoire */
        if (aircraftIcon.getValue().canRotate()) {
            // On lie la rotation de l'aéronef a l'angle en degré
            aircraft.rotateProperty().bind(Bindings.createDoubleBinding(() ->
                            Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE),
                    aircraftState.trackOrHeadingProperty()));
        } else {
            aircraft.setRotate(NO_ROTATION_ANGLE);
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Couleur de l'icône.
    --------------------------------------------------------------------------------------------------------------------
    */
        aircraft.fillProperty().bind(Bindings.createObjectBinding(() ->
                        ColorRamp.PLASMA.at(ColorRamp.plasmaColorFunction(aircraftState.getAltitude())),
                aircraftState.altitudeProperty()));


        return aircraft;
    }

    /**
     * Méthode qui se charge de créer l'étiquette.
     *
     * @param aircraftState l'état de l'aéronef sélectionné.
     * @param aircraftIcon  l'icone de l'aéronef sélectionné
     * @return l'étiquette associée a l'aéronef sélectionné.
     */
    private Group labelCreator(ObservableAircraftState aircraftState,
                               SVGPath aircraftIcon,
                               MapParameters mapParameters) {
        Text text = new Text();
        Rectangle background = new Rectangle();

        // On lie l'arrière-plan a la longueur et la largeur du texte + 4
        background.widthProperty().bind
                (text.layoutBoundsProperty().map(b -> b.getWidth() + MARGIN));
        background.heightProperty().bind
                (text.layoutBoundsProperty().map(b -> b.getHeight() + MARGIN));

        // On lie le texte au titre + altitude/vitesse créé par labelText
        text.textProperty().bind
                (Bindings.createStringBinding(() -> labelText(aircraftState),
                        aircraftState.altitudeProperty(),
                        aircraftState.velocityProperty()));

        final Group label = new Group(background, text); // Groupe du texte + arrière-plan
        label.getStyleClass().add("label");

        /* Quand on clicke sur l'icone, la valeur contenue dans aircraftStateProperty
        est modifiée a la valeur de l'icone sur lequel on a clické */
        aircraftIcon.setOnMouseClicked(event -> this.aircraftStateProperty.set(aircraftState));

        /* L'étiquette sera vue ssi le niveau de zoom est supérieur ou égal à 11,
        ou que l'aéronef sélectionné est celui auquel l'étiquette correspond. */
        BooleanBinding showLabel = Bindings.createBooleanBinding(() ->
                        (mapParameters.getZoomProperty().get() >= MINIMUM_ZOOM_LEVEL
                                || (aircraftState.equals(getObservableAircraftState())))
                , mapParameters.getZoomProperty(), this.aircraftStateProperty);

        label.visibleProperty().bind
                (showLabel);
        return label;
    }

    /**
     * Méthode qui se charge de créer le texte de l'étiquette.
     *
     * @param aircraftState l'état de l'aéronef sélectionné.
     * @return le texte affiché dans l'étiquette.
     */
    private String labelText(ObservableAircraftState aircraftState) {
        /* Par défaut, on retourne l'adresse OACI. Cependant, si l'immatriculation est connue, on la retourne. Sinon si
        l'indicatif est connu, on le retourne. */

        String title = aircraftState.getIcaoAddress().string(); // Par défaut, on retourne l'adresse OACI
        if (Objects.nonNull
                (aircraftState.getAircraftData())) {
            // Si l'immatriculation n'est pas connue, c.-à-d. qu'elle est nulle.
            if (Objects.nonNull
                    (aircraftState.getAircraftData().registration())) {
                title = aircraftState.getAircraftData().registration().string();
            }
        }
        // Si l'l'indicatif n'est pas connue, c.-à-d. qu'elle est nulle.
        else if (Objects.nonNull
                (aircraftState.getCallSign())) {
            title = aircraftState.getCallSign().string();
        }


        /* Si la vitesse est connue, on retourne le titre de l'étiquette suivie de sa vitesse et de son altitude.

        Sinon, c.-à-d. quand la vitesse est inconnue,
        c.-à-d. qu'elle est encore dans son état initial (dans ce cas Double.isNan),
        on remplace la valeur de la vitesse par un point d'intérrogation. */

        if (Double.isNaN(aircraftState.getVelocity())) {
            /* On utilise
            - %.0f pour indiquer qu'il y'a un float avec 0 décimales
             - %s pour indiquer un String
             - %n pour indiquer un saut de ligne
             - \u200A pour indiquer un espace quart-cadratin
             - \u2002 pour indiquer un espace demi-cadratin */

            return "%s%n%s\u200A%s\u2002%.0f\u200A%s".formatted(title,
                    "?",
                    "km/h",
                    aircraftState.getAltitude(),
                    "m");
        } else {
            return "%s%n%.0f\u200A%s\u2002%.0f\u200A%s".formatted(title,
                    Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR),
                    "km/h",
                    aircraftState.getAltitude(),
                    "m");
        }
    }

    /**
     * Méthode qui se charge d'ajouter des auditeurs sur minX,minY et le zoom afin de mettre à jour la position de
     * la trajectoire
     *
     * @param group                   trajectoire
     * @param observableAircraftState etat de l'aeronef selectionne
     */
    private void setListeners(Group group,
                              ObservableAircraftState observableAircraftState,
                              MapParameters mapParameters) {
        minXListener = (obs, oldV, newV) -> repositionNodes(group, mapParameters);
        mapParameters.getMinXProperty().addListener(minXListener);

        minYListener = (obs, oldV, newV) -> repositionNodes(group, mapParameters);
        mapParameters.getMinYProperty().addListener(minYListener);

        zoomListener = (obs, oldV, newV) -> reconstructLines(group,
                observableAircraftState,
                observableAircraftState.getObservableTrajectory(),
                mapParameters);
        mapParameters.getZoomProperty().addListener(zoomListener);
    }

    /**
     * Méthode qui se charge d'enlever les auditeurs mis en place par setListeners
     */
    private void removeListeners(MapParameters mapParameters) {
        mapParameters.getMinXProperty().removeListener(minXListener);
        mapParameters.getMinYProperty().removeListener(minYListener);
        mapParameters.getZoomProperty().removeListener(zoomListener);
    }
}
