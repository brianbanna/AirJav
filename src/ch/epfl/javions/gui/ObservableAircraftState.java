package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * Classe qui représente l'état d'un aéronef qui est observable au sens du patron de conception Observer.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class ObservableAircraftState implements AircraftStateSetter {


    private final AircraftData aircraftData;
    private final DoubleProperty altitude;
    private final DoubleProperty trackOrHeading;
    private final DoubleProperty velocity;
    private final IcaoAddress icaoAddress;
    private final IntegerProperty category;
    private final LongProperty lastMessageTimeStampNs;
    private final ObjectProperty<CallSign> callSign;
    private final ObjectProperty<GeoPos> position;

    // La liste observable et modifiable de trajectoires
    private final ObservableList<AirbornePos> modifiableTrajectory;

    // La vue obversable et non modifiable sur la liste modifiableTrajectory
    private final ObservableList<AirbornePos> observableTrajectory;

    private long savedTimeStampNs; // Attribut qui mémorise l'horodotage du dernier message

    /**
     * Crée une instance de ObservableAircraftState.
     *
     * @param icaoAddress  l'adresse OACI de l'aéronef dont l'état sera représenté par cette instance.
     * @param aircraftData les caractéristiques fixes de l'aéronef provenant de la base de données mictronics.
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;

        lastMessageTimeStampNs = new SimpleLongProperty();
        category = new SimpleIntegerProperty();
        callSign = new SimpleObjectProperty<>();
        position = new SimpleObjectProperty<>();
        /* On initialise la vitesse et l'altitude a Double.Nan pour pouvoir déterminer
        par la suite si ces 2 attributs ont une valeur connue. */
        altitude = new SimpleDoubleProperty(Double.NaN);
        velocity = new SimpleDoubleProperty(Double.NaN);
        trackOrHeading = new SimpleDoubleProperty();

        modifiableTrajectory = FXCollections.observableArrayList();
        observableTrajectory = FXCollections.unmodifiableObservableList(modifiableTrajectory);

        updateTrajectory();
    }

    /**
     * Getter public de l'attribut icaoAddress.
     *
     * @return l'adresse ICAO de l'aéronef.
     */
    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    /**
     * Getter public de l'attribut aircraftData.
     *
     * @return les caractéristiques fixes de l'aéronef.
     */
    public AircraftData getAircraftData() {
        return aircraftData;
    }

    /**
     * Getter public de la valeur contenue dans la propriété lastMessageTimeStampNs.
     *
     * @return l'horodatage du dernier message reçu de l'aéronef, en nanosecondes.
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    /**
     * Setter public de l'attribut lastMessageTimeStampNs.
     *
     * @param newLastMessageTimeStampNs nouvelle valeur de l'horodatage de l'aéronef.
     */
    public void setLastMessageTimeStampNs(long newLastMessageTimeStampNs) {
        savedTimeStampNs = getLastMessageTimeStampNs(); // On mémorise la dernière valeur de l'horodotage
        lastMessageTimeStampNs.set(newLastMessageTimeStampNs);
    }

    /**
     * Méthode d'accès à la propriété lastMessageTimeStampNs en lecture seule.
     *
     * @return la propriété lastMessageTimeStampNs en lecture seule.
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    /**
     * Getter public de la valeur contenue dans la propriété category.
     *
     * @return la catégorie de l'aéronef.
     */
    public int getCategory() {
        return category.get();
    }

    /**
     * Setter public de l'attribut category.
     *
     * @param newCategory nouvelle valeur de la catégorie de l'aeronef.
     */
    public void setCategory(int newCategory) {
        category.set(newCategory);
    }

    /**
     * Méthode d'accès à la propriété category en lecture seule.
     *
     * @return la propriété category en lecture seule.
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    /**
     * Getter public de la valeur contenue dans la propriété callSign.
     *
     * @return l'indicatif de l'aéronef.
     */
    public CallSign getCallSign() {
        return callSign.get();
    }

    /**
     * Setter public de l'attribut callSign.
     *
     * @param newCallSign nouvel indicatif de l'aeronef.
     */
    public void setCallSign(CallSign newCallSign) {
        callSign.set(newCallSign);
    }

    /**
     * Méthode d'accès à la propriété callSign en lecture seule.
     *
     * @return la propriété callSign en lecture seule.
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    /**
     * Getter public de la valeur contenue dans la propriété position.
     *
     * @return l'indicatif de l'aéronef.
     */
    public GeoPos getPosition() {
        return position.get();
    }

    /**
     * Setter public de l'attribut position.
     *
     * @param newPosition nouvelle position de l'aeronef.
     */
    public void setPosition(GeoPos newPosition) {
        position.set(newPosition);
        updateTrajectory();
    }

    /**
     * Méthode d'accès à la propriété position en lecture seule.
     *
     * @return la propriété position en lecture seule.
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    /**
     * Getter public de la valeur contenue dans la propriété altitude.
     *
     * @return l'altitude de l'aéronef, en mètres.
     */
    public double getAltitude() {
        return altitude.get();
    }

    /**
     * Setter public de l'attribut altitude.
     *
     * @param newAltitude nouvelle valeur de l'altitude de l'aeronef.
     */
    public void setAltitude(double newAltitude) {
        altitude.set(newAltitude);
        updateTrajectory();
    }

    /**
     * Méthode d'accès à la propriété altitude en lecture seule.
     *
     * @return la propriété altitude en lecture seule.
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    /**
     * Getter public de la valeur contenue dans la propriété velocity.
     *
     * @return la vitesse de l'aéronef, en mètres par seconde.
     */
    public double getVelocity() {
        return velocity.get();
    }

    /**
     * Setter public de l'attribut velocity.
     *
     * @param newVelocity nouvelle valeur de la vitesse de l'aeronef.
     */
    public void setVelocity(double newVelocity) {
        velocity.set(newVelocity);
    }

    /**
     * Méthode d'accès à la propriété velocity en lecture seule.
     *
     * @return la propriété velocity en lecture seule.
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    /**
     * Getter public de la valeur contenue dans la propriété trackOrHeading.
     *
     * @return la route ou le cap de l'aéronef, en radians.
     */
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    /**
     * Setter public de l'attribut trackOrHeading.
     *
     * @param newTrackOrHeading nouvelle valeur de la direction de l'aeronef.
     */
    public void setTrackOrHeading(double newTrackOrHeading) {
        trackOrHeading.set(newTrackOrHeading);
    }

    /**
     * Méthode d'accès à la propriété trackOrHeading en lecture seule.
     *
     * @return la propriété trackOrHeading en lecture seule.
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    /**
     * Méthode d'accès à la vue non modifiable sur la liste des trajectoires.
     *
     * @return la vue sur la liste des trajectoires.
     */
    public ObservableList<AirbornePos> getObservableTrajectory() {
        return observableTrajectory;
    }

    /**
     * Getter de l'avant dernier élément de la liste modifiableTrajectory.
     *
     * @return l'avant dernière position de l'aéronef.
     */
    private AirbornePos getLastPosition() {
        return modifiableTrajectory.get(modifiableTrajectory.size() - 1);
    }

    /**
     * Méthode qui permet le calcul de la trajectoire.
     */
    public void updateTrajectory() {
        // La trajectoire est vide
        if (modifiableTrajectory.isEmpty()) {
            // Si la position est connue
            if (Objects.nonNull(getPosition())) {
                modifiableTrajectory.add(new AirbornePos(getPosition(), getAltitude()));
                savedTimeStampNs = getLastMessageTimeStampNs();
            }
        }
        // Nouvelle position recue
        else if (getPosition() != getLastPosition().position()) {
            // Si l'altitude est connue
            if (!Double.isNaN(getAltitude())) {
                modifiableTrajectory.add(new AirbornePos(getPosition(), getAltitude()));
                savedTimeStampNs = getLastMessageTimeStampNs();
            }
        }
        // Nouvelle altitude recue
        else if (getAltitude() != getLastPosition().altitude()) {
            if (savedTimeStampNs == getLastMessageTimeStampNs()) {
                // Si la position est connue
                if (Objects.nonNull(getPosition())) {
                    modifiableTrajectory.set(modifiableTrajectory.size() - 1,
                            new AirbornePos(getPosition(), getAltitude()));
                }
            }
        }
    }

    /**
     * Enregistrement qui représente une paire de position à la surface de la Terre et une altitude.
     *
     * @param position position de l'aéronef à la surface de la Terre.
     * @param altitude altitude de l'aéronef.
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }
}