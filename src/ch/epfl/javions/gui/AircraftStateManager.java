package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ch.epfl.javions.Units.Time.MINUTE;
import static ch.epfl.javions.Units.Time.NANOSECONDS;

/**
 * Classe qui a pour but de garder à jour les états d'un ensemble d'aéronefs en fonction des messages reçus d'eux
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class AircraftStateManager {
    private final AircraftDatabase aircraftDatabase;

    // Ensemble des états des aéronefs dont la position est connue
    private final ObservableSet<ObservableAircraftState> statesSet = FXCollections.observableSet();

    // Table associant un accumulateur d'état d'aéronef à l'ICAO de tout aéronef dont un message a été reçu récemment
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table = new HashMap<>();

    // Table associant l'horodatage de l'aéronef à l'ICAO de tout aéronef dont un message a été reçu récemment
    private final Map<IcaoAddress, Long> timeStampsTable = new HashMap<>();
    private long lastTimeStampNs;

    /**
     * Constructuer de la classe AircraftStateManager.
     *
     * @param aircraftDataBase la base de données des aéronefs utilisée pour la gestion des états.
     * @throws NullPointerException si aircraftDataBase est null.
     */
    public AircraftStateManager(AircraftDatabase aircraftDataBase) {
        this.aircraftDatabase = Objects.requireNonNull(aircraftDataBase);
    }

    /**
     * Méthode retournant l'ensemble observable, mais non modifiable, des états observables des aéronefs dont la
     * position est connue
     *
     * @return l'ensemble observable, mais non modifiable, des états observables des aéronefs dont la position est
     * connue.
     */
    public ObservableSet<ObservableAircraftState> states() {
        return statesSet;
    }

    /**
     * Méthode qui met à jour l'état de l'aéronef correspondant.
     *
     * @param message le message envoyé.
     * @throws IOException si une erreur s'est produite pendant l'écriture des données.
     */
    public void updateWithMessage(Message message) throws IOException {
        lastTimeStampNs = message.timeStampNs();
        // Validation du ICAO
        IcaoAddress icao = Objects.requireNonNull(message.icaoAddress());
        // Les données de l'aéronef
        AircraftData aircraftData = aircraftDatabase.get(icao);

        // Création de l'état lorsque le message est le premier recu de cet aéronef
        if (!(table.containsKey(icao))) {
            ObservableAircraftState observableAircraftState = new ObservableAircraftState(icao, aircraftData);
            table.put(icao, new AircraftStateAccumulator<>(observableAircraftState));
            table.get(icao).update(Objects.requireNonNull(message));

        }

        // Mise a jour de l'état de l'aéronef
        else {
            table.get(icao).update(Objects.requireNonNull(message));
        }

        // Ajout des états des aéronefs dont la position est connue à l'ensemble des états
        if (Objects.nonNull(table.get(
                message.icaoAddress()).stateSetter().getPosition())) {
            statesSet.add(table.get(icao).stateSetter());
        }
        timeStampsTable.put(icao, lastTimeStampNs);
        table.get(icao).stateSetter().updateTrajectory();
    }

    /**
     * Méthode qui supprime de l'ensemble des états observables tous ceux qui correspondent à des aéronefs
     * dont aucun message n'a été reçu dans la minute précédant la réception du dernier message.
     */
    public void purge() {
        for (Map.Entry<IcaoAddress, Long> entry : timeStampsTable.entrySet()) {
            IcaoAddress key = entry.getKey();
            Long value = entry.getValue();

            /* Si aucun message n'a été reçu dans la minute précédant la réception du dernier message passé
             à updateWithMessage par l'aeronef, on supprime de l'ensemble des etats observables celui
             qui lui correspond */
            if ((Math.abs(value - lastTimeStampNs) > Units.convertTo(MINUTE, NANOSECONDS))) {
                AircraftStateAccumulator<ObservableAircraftState> accumulator = table.get(key);
                statesSet.remove(accumulator.stateSetter());
            }
        }
    }
}
