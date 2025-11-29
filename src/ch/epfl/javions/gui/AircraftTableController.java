package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;

/**
 * Classe qui gère la table des aéronefs.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class AircraftTableController {
    private static final double CALLSIGN_AND_DESCRIPTION_COLUMN_WIDTH = 70d;
    private static final double ICAO_COLUMN_WIDTH = 60d;
    private static final double MODEL_COLUMN_WIDTH = 230d;
    private static final double NUMERIC_COLUMN_WIDTH = 85d;
    private static final double REGISTRATION_COLUMN_WIDTH = 90d;
    private static final double TYPE_COLUMN_WIDTH = 50d;
    private static final int NUM_DECIMAL_SPOTS_ALTITUDE_AND_SPEED = 0;
    private static final int NUM_DECIMAL_SPOTS_POSITION = 4;
    private static final int TWO_CLICKS = 2;
    private final TableView<ObservableAircraftState> tableView;


    /**
     * Crée un contrôleur de table des aéronefs avec l'ensemble d'états d'aéronefs et la propriété d'état d'aéronef
     * sélectionné spécifiés.
     *
     * @param states                L'ensemble d'états d'aéronefs à afficher dans la table.
     * @param aircraftStateProperty La propriété d'état d'aéronef sélectionné.
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> aircraftStateProperty) {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création du graphe de scene.
    --------------------------------------------------------------------------------------------------------------------
    */
        tableView = new TableView<>();
        tableView.getStylesheets().add("table.css");
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Ajout des auditeurs différents pour la table des aéronefs et création des colonnes
    --------------------------------------------------------------------------------------------------------------------
    */
        addAircraftStateListener(states);
        addSelectionListeners(aircraftStateProperty);


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création des colonnes numériques et textuelles.
    --------------------------------------------------------------------------------------------------------------------
    */
        textColumnsCreator(tableView);
        numericColumnsCreator(tableView);
    }

    /**
     * Ajoute un écouteur d'état d'aéronef à l'ensemble spécifié qui permet de mettre à jour la vue du
     * tableau correspondante.
     *
     * @param states L'ensemble observable d'états d'aéronef auquel l'écouteur doit être ajouté.
     */
    private void addAircraftStateListener(ObservableSet<ObservableAircraftState> states) {
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                // Code à exécuter si un état d'aéronef a été ajouté a l'ensemble
                ObservableAircraftState state = change.getElementAdded();
                tableView.getItems().add(state);
                tableView.sort();
            } else if (change.wasRemoved()) {
                // Code à exécuter si un état d'aeronef a été supprimé de l'ensemble
                ObservableAircraftState state = change.getElementRemoved();
                tableView.getItems().remove(state);
                tableView.sort();
            }
        });
    }

    /**
     * Méthode qui crée et configure les colonnes de texte pour la vue du tableau spécifiée.
     *
     * @param tableView La vue du tableau à laquelle les colonnes de texte doivent être ajoutées.
     */
    private void textColumnsCreator(TableView<ObservableAircraftState> tableView) {
        // Colonne de l'ICAO
        TableColumn<ObservableAircraftState, String> icaoColumn = createTextColumn("OACI",
                this::getIcaoString, ICAO_COLUMN_WIDTH);

        // Colonne de l'indicatif
        TableColumn<ObservableAircraftState, String> callSignColumn = createTextColumn("Indicatif",
                this::getCallSignString, CALLSIGN_AND_DESCRIPTION_COLUMN_WIDTH);

        // Colonne de l'immatriculation
        TableColumn<ObservableAircraftState, String> registrationColumn = createTextColumn("Immatriculation",
                this::getRegistrationString, REGISTRATION_COLUMN_WIDTH);

        // Colonne du modèle de l'aéronef
        TableColumn<ObservableAircraftState, String> modelColumn = createTextColumn("Modèle",
                this::getModelString, MODEL_COLUMN_WIDTH);

        // Colonne du type de l'aéronef
        TableColumn<ObservableAircraftState, String> typeColumn = createTextColumn("Type",
                this::getTypeString, TYPE_COLUMN_WIDTH);

        // Colonne de la description de l'aéronef
        TableColumn<ObservableAircraftState, String> descriptionColumn = createTextColumn("Description",
                this::getDescriptionString, CALLSIGN_AND_DESCRIPTION_COLUMN_WIDTH);

        // Ajout des colonnes
        tableView.getColumns().addAll(List.of(icaoColumn, callSignColumn, registrationColumn,
                modelColumn, typeColumn, descriptionColumn));
    }

    /**
     * Crée et ajoute les colonnes numériques à la vue du tableau spécifiée.
     *
     * @param tableView La vue du tableau à laquelle les colonnes numériques doivent être ajoutées.
     */
    private void numericColumnsCreator(TableView<ObservableAircraftState> tableView) {
        tableView.getColumns().addAll(List.of(
                //Colonne de la longitude
                createNumericColumn("Longitude (°)", createLongitudeExtractor(),
                        NUM_DECIMAL_SPOTS_POSITION, Units.Angle.DEGREE),
                //Colonne de la latitude
                createNumericColumn("Latitude (°)", createLatitudeExtractor(),
                        NUM_DECIMAL_SPOTS_POSITION, Units.Angle.DEGREE),
                //Colonne de l'altitude
                createNumericColumn("Altitude (m)", createAltitudeExtractor(),
                        NUM_DECIMAL_SPOTS_ALTITUDE_AND_SPEED, 1), // Pas de conversion d'ou le 1
                //Colonne de la vitesse
                createNumericColumn("Vitesse (km/h)", createVelocityExtractor(),
                        NUM_DECIMAL_SPOTS_ALTITUDE_AND_SPEED, Units.Speed.KILOMETER_PER_HOUR)
        ));
    }


    /**
     * Méthode qui prend en argument une valeur de type Consumer<ObservableAircraftState>, et qui appelle sa méthode
     * accept lorsqu'un clic double est effectué sur la table et qu'un aéronef est actuellement sélectionné
     *
     * @param consumer valeur de type Consumer<ObservableAircraftState> dont on va appeler la méthode
     *                 accept lorsqu'un clic double est effectué
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
        tableView.setOnMouseClicked(event -> {
            // Double click
            if (event.getClickCount() == TWO_CLICKS && event.getButton() == MouseButton.PRIMARY) {
                ObservableAircraftState selectedAircraftState = tableView.getSelectionModel().getSelectedItem();
                if (Objects.nonNull(selectedAircraftState)) {
                    consumer.accept(selectedAircraftState);
                }
            }
        });
    }

    /**
     * Méthode qui retourne le nœud à la racine de son graphe de scène—une instance de TableView
     *
     * @return une instance de TableView
     */
    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    /**
     * Crée une colonne de texte pour la vue du tableau avec le titre,l'extracteur de propriété et la largeur spécifiés.
     * Cette méthode crée une colonne de texte pour afficher des valeurs textuelles dans la vue du tableau.
     *
     * @param <S>               Le type de l'objet source associé à la ligne de données dans la vue du tableau.
     * @param title             Le titre de la colonne.
     * @param propertyExtractor La fonction extractrice de propriété qui récupère la valeur textuelle de la cellule.
     * @param width             La largeur préférée de la colonne.
     * @return La colonne de texte créée.
     */
    private <S> TableColumn<S, String> createTextColumn(String title,
                                                        Function<TableColumn.CellDataFeatures<S, String>,
                                                                String> propertyExtractor,
                                                        double width) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(propertyExtractor.apply(cellData)));
        column.setPrefWidth(width);
        column.setResizable(false);
        return column;
    }

    /**
     * Crée une colonne numérique pour la vue du tableau avec le titre, l'extracteur de valeur, le nombre de décimales
     * et l'unité de mesure spécifiés.
     *
     * @param <S>            Le type de l'objet source associé à la ligne de données dans la vue du tableau.
     * @param title          Le titre de la colonne.
     * @param valueExtractor La fonction extractrice de valeur qui récupère la valeur numérique de la cellule.
     * @param decimalSpots   Le nombre de décimales à afficher pour la valeur numérique.
     * @param unit           L'unité de mesure associée à la valeur numérique.
     * @return La colonne numérique créée.
     */
    private <S> TableColumn<S, String> createNumericColumn(String title,
                                                           Function<S, DoubleExpression> valueExtractor,
                                                           int decimalSpots,
                                                           double unit) {
        // Configuration du formatage des valeurs numériques avec le nombre de décimales spécifié
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(decimalSpots);
        numberFormat.setMaximumFractionDigits(decimalSpots);

        // Comparateur de colonnes utilisé pour le tri des valeurs numériques sous forme de texte
        Comparator<String> columnComparator = (s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            } else {
                try {
                    Double d1 = numberFormat.parse(s1).doubleValue();
                    Double d2 = numberFormat.parse(s2).doubleValue();
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    return 0;
                }
            }
        };

        // Création de la colonne numérique
        TableColumn<S, String> column = new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        column.setCellValueFactory(f ->
                Bindings.createStringBinding(() -> {
                            double value = valueExtractor.apply(f.getValue()).get();
                            double convertedValue = Units.convertTo(value, unit);
                            return numberFormat.format(convertedValue);
                        },
                        valueExtractor.apply(f.getValue())
                )
        );
        column.setPrefWidth(NUMERIC_COLUMN_WIDTH);
        column.setComparator(columnComparator);
        return column;
    }

    /**
     * Ajoute les listeners de sélection pour la table des aéronefs.
     */
    private void addSelectionListeners(ObjectProperty<ObservableAircraftState> aircraftStateProperty) {
        aircraftStateProperty.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), newValue)) {
                tableView.scrollTo(aircraftStateProperty.getValue());
                tableView.getSelectionModel().select(newValue);
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                aircraftStateProperty.set(newValue));
    }

    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Extracteurs de valeurs
    --------------------------------------------------------------------------------------------------------------------
    */

    /**
     * Crée un extracteur de valeur pour les attributs d'état d'aéronef qui utilisent GeoPos.
     *
     * @param positionProvider  Fournit la propriété de position de l'ObservableAircraftState
     * @param geoValueExtractor Extrait la valeur requise de GeoPos
     * @return L'extracteur de valeur
     */
    private Function<ObservableAircraftState, DoubleExpression> createCoordsExtractor(
            Function<ObservableAircraftState, ReadOnlyObjectProperty<GeoPos>> positionProvider,
            Function<GeoPos, Double> geoValueExtractor) {
        return f -> {
            ReadOnlyObjectProperty<GeoPos> positionProperty = positionProvider.apply(f);
            return Bindings.createDoubleBinding(
                    () -> {
                        GeoPos pos = positionProperty.get();
                        return geoValueExtractor.apply(pos);
                    },
                    positionProperty
            );
        };
    }

    //Les méthodes suivantes utilisent createCoordsExtractor

    private Function<ObservableAircraftState, DoubleExpression> createLongitudeExtractor() {
        return createCoordsExtractor(ObservableAircraftState::positionProperty, GeoPos::longitude);
    }

    private Function<ObservableAircraftState, DoubleExpression> createLatitudeExtractor() {
        return createCoordsExtractor(ObservableAircraftState::positionProperty, GeoPos::latitude);
    }

    /**
     * Crée un extracteur de valeur de type DoubleExpression pour un attribut spécifique de l'état d'aéronef.
     *
     * @param propertyProvider Fournit la propriété spécifique de l'ObservableAircraftState
     * @return L'extracteur de valeur de type DoubleExpression pour l'attribut spécifié.
     */
    private Function<ObservableAircraftState, DoubleExpression> createExtractor(
            Function<ObservableAircraftState, ReadOnlyDoubleProperty> propertyProvider) {
        return f -> Bindings.createDoubleBinding(
                () -> propertyProvider.apply(f).get(),
                propertyProvider.apply(f)
        );
    }
    //Les méthodes suivantes utilisent createCoordsExtractor

    /**
     * Crée un extracteur de valeur de type DoubleExpression pour l'attribut d'altitude de l'état d'aéronef.
     *
     * @return L'extracteur de valeur de type DoubleExpression pour l'altitude.
     */
    private Function<ObservableAircraftState, DoubleExpression> createAltitudeExtractor() {
        return createExtractor(ObservableAircraftState::altitudeProperty);
    }

    /**
     * Crée un extracteur de valeur de type DoubleExpression pour l'attribut de vitesse de l'état d'aéronef.
     *
     * @return L'extracteur de valeur de type DoubleExpression pour la vitesse.
     */
    private Function<ObservableAircraftState, DoubleExpression> createVelocityExtractor() {
        return createExtractor(ObservableAircraftState::velocityProperty);
    }

    /**
     * Extrait les données directement de l'état de l'aéronef.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne
     * @param fetcher  La fonction à appliquer pour extraire les données
     * @return La donnée extraite sous forme de chaîne de caractères
     */
    private String fetchDirectly(TableColumn.CellDataFeatures<ObservableAircraftState, String> features,
                                 Function<ObservableAircraftState, String> fetcher) {
        return fetcher.apply(features.getValue());
    }

    // Les méthodes suivantes utilisent fetchDirectly pour extraire différentes données

    /**
     * Récupère l'adresse ICAO de l'aéronef à partir des fonctionnalités des données de la cellule de la colonne.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne.
     * @return L'adresse ICAO de l'aéronef.
     */
    private String getIcaoString(TableColumn.CellDataFeatures<ObservableAircraftState, String> features) {
        return fetchDirectly(features, oas -> oas.getIcaoAddress().string());
    }

    /**
     * Récupère l'indicatif d'appel de l'aéronef à partir des fonctionnalités des données de la cellule de la colonne.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne.
     * @return L'indicatif d'appel de l'aéronef.
     */
    private String getCallSignString(TableColumn.CellDataFeatures<ObservableAircraftState, String> features) {
        return fetchDirectly(features, oas -> oas.callSignProperty().map(CallSign::string).getValue());
    }


    /**
     * Extrait les données de l'aéronef de l'état de l'aéronef.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne
     * @param fetcher  La fonction à appliquer pour extraire les données
     * @return La donnée extraite sous forme de chaîne de caractères, ou une chaîne vide si l'AircraftData est null
     */
    private String fetchAircraftData(TableColumn.CellDataFeatures<ObservableAircraftState, String> features,
                                     Function<AircraftData, String> fetcher) {
        return Objects.nonNull(features.getValue().getAircraftData())
                ? fetcher.apply(features.getValue().getAircraftData())
                : "";
    }
    // Les méthodes suivantes utilisent fetchAircraftData pour extraire différentes données

    /**
     * Récupère l'immatriculation de l'aéronef à partir des fonctionnalités des données de la cellule de la colonne.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne.
     * @return L'immatriculation de l'aéronef.
     */
    private String getRegistrationString(TableColumn.CellDataFeatures<ObservableAircraftState, String> features) {
        return fetchAircraftData(features, ad -> ad.registration().string());
    }


    /**
     * Récupère le modèle de l'aéronef à partir des fonctionnalités des données de la cellule de la colonne.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne.
     * @return Le modèle de l'aéronef.
     */
    private String getModelString(TableColumn.CellDataFeatures<ObservableAircraftState, String> features) {
        return fetchAircraftData(features, AircraftData::model);
    }

    /**
     * Récupère le type de l'aéronef à partir des fonctionnalités des données de la cellule de la colonne.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne.
     * @return Le type de l'aéronef.
     */
    private String getTypeString(TableColumn.CellDataFeatures<ObservableAircraftState, String> features) {
        return fetchAircraftData(features, ad -> ad.typeDesignator().string());
    }

    /**
     * Récupère la description de l'aéronef à partir des fonctionnalités des données de la cellule de la colonne.
     *
     * @param features Les fonctionnalités des données de la cellule de la colonne.
     * @return La description de l'aéronef.
     */
    private String getDescriptionString(TableColumn.CellDataFeatures<ObservableAircraftState, String> features) {
        return fetchAircraftData(features, ad -> ad.description().string());
    }
}