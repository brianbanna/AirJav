package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * Accumulateur d'état d'aéronef.
 *
 * @param <T> état modifiable d'aéronef.
 */
public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final static double TEN_POW_10 = Math.pow(10, 10);
    private final T stateSetter;
    AirbornePositionMessage oddMessage; // Dernier Message Impair
    AirbornePositionMessage evenMessage; // Dernier Message Pair

    /**
     * Constructeur public de AircraftStateAccumulator.
     *
     * @param stateSetter l'état modifiable de l'aéronef.
     * @throws NullPointerException si stateSetter est nul.
     */
    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    /**
     * Getter public de l'attribut stateSetter.
     *
     * @return l'état modifiable de l'aéronef.
     */
    public T stateSetter() {
        return stateSetter;
    }

    /**
     * Met a jour l'état modifiable en fonction du message.
     *
     * @param message le message donné.
     */
    public void update(Message message) {
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {
            /*
            ------------------------------------------------------------------------------------------------------------
              -- Messages d'identification
            ------------------------------------------------------------------------------------------------------------
            */
            case AircraftIdentificationMessage msg1 -> {
                stateSetter.setCategory(msg1.category());
                stateSetter.setCallSign(msg1.callSign());
            }
            /*
            ------------------------------------------------------------------------------------------------------------
              -- Messages de position en vol
            ------------------------------------------------------------------------------------------------------------
            */
            case AirbornePositionMessage msg2 -> {
                stateSetter.setAltitude(msg2.altitude());
                boolean msg2IsEven = msg2.parity() == 0;

                /* différence entre l'horodatage du message passé à update et celui du dernier
                message de parité opposée reçu de l'aéronef */
                double timeDifference;


                /*
                --------------------------------------------------------------------------------------------------------
                  -- Messages pair
                --------------------------------------------------------------------------------------------------------
                */
                if (msg2IsEven) {
                    evenMessage = msg2;
                    if (oddMessage != null) {
                        timeDifference = Math.abs(msg2.timeStampNs() - oddMessage.timeStampNs());
                        if (timeDifference <= TEN_POW_10) {
                            GeoPos decodedPosition = CprDecoder.decodePosition(msg2.x(),
                                    msg2.y(),
                                    oddMessage.x(),
                                    oddMessage.y(),
                                    0);
                            if (!Objects.isNull(decodedPosition)) {
                                stateSetter.setPosition(decodedPosition);
                            }
                        }
                    }
                }
                /*
                --------------------------------------------------------------------------------------------------------
                  -- Messages impairs
                --------------------------------------------------------------------------------------------------------
                */
                else {
                    oddMessage = msg2;
                    if (evenMessage != null) {
                        timeDifference = Math.abs(msg2.timeStampNs() - evenMessage.timeStampNs());
                        if (timeDifference <= TEN_POW_10) {
                            GeoPos decodedPosition = CprDecoder.decodePosition(evenMessage.x(),
                                    evenMessage.y(),
                                    msg2.x(),
                                    msg2.y(),
                                    1);
                            if (!Objects.isNull(decodedPosition)) {
                                stateSetter.setPosition(decodedPosition);
                            }
                        }
                    }
                }
            }


            /*
            ------------------------------------------------------------------------------------------------------------
                -- Messages de vitesse en vol
            ------------------------------------------------------------------------------------------------------------
            */
            case AirborneVelocityMessage msg3 -> {
                stateSetter.setVelocity(msg3.speed());
                stateSetter.setTrackOrHeading(msg3.trackOrHeading());

            }


             /*
            ------------------------------------------------------------------------------------------------------------
                -- Aucun des 3 types de messages
            ------------------------------------------------------------------------------------------------------------
            */
            default -> throw new Error();


        }
    }
}