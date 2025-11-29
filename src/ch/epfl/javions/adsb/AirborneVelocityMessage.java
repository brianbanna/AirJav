package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement qui représente un message de vitesse en vol.
 *
 * @param timeStampNs    horodatage du message, en nanosecondes.
 * @param icaoAddress    adresse OACI de l'expéditeur du message.
 * @param speed          vitesse de l'aéronef, en m/s.
 * @param trackOrHeading direction de déplacement de l'aéronef, en radians.
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record AirborneVelocityMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double speed,
                                      double trackOrHeading) implements Message {
    public AirborneVelocityMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(speed >= 0);
        Preconditions.checkArgument(trackOrHeading >= 0);
        Objects.requireNonNull(icaoAddress);
    }

    /**
     * Méthode qui retourne le message de vitesse en vol correspondant au message brut donné.
     *
     * @param rawMessage message brut.
     * @return retourne le message de vitesse en vol correspondant au message brut donné, ou null si le sous-type est
     * invalide, ou si la vitesse ou la direction de déplacement ne peuvent pas être déterminés.
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload(); //attribut ME
        long subType = Bits.extractUInt(payload, 48, 3);
        long dependingBits = Bits.extractUInt(payload, 21, 22); // Les 22 bits dependant du sous-type
        double speed; // Norme du vecteur vitesse
        double trackOrHeading;
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Sous-Type 1 ou 2
    --------------------------------------------------------------------------------------------------------------------
    */
        if (subType == 1 || subType == 2) {
            double eastWestDirection = Bits.extractUInt(dependingBits, 21, 1);
            double eastWestSpeed = Bits.extractUInt(dependingBits, 11, 10);
            double northSouthDirection = Bits.extractUInt(dependingBits, 10, 1);
            double northSouthSpeed = Bits.extractUInt(dependingBits, 0, 10);


            /*
            ------------------------------------------------------------------------------------------------------------
              -- Calcul de la vitesse
            ------------------------------------------------------------------------------------------------------------
            */
            // Cas ou la vitesse est inconnue
            boolean unknownSpeed = (northSouthSpeed == 0 || eastWestSpeed == 0);
            if (unknownSpeed) {
                return null;
            }

            // Calcul de la norme de la vitesse en noeuds
            speed = Math.hypot(minusOne(eastWestSpeed),
                    minusOne(northSouthSpeed));
            if (subType == 1) {
                speed = Units.convertFrom(speed, Units.Speed.KNOT);  // Conversion de noeuds en m.s
            } else {
                double fourKnots = Units.Speed.KNOT * 4;
                speed = Units.convertFrom(speed, fourKnots); // Conversion de 4 noeuds en m.s
            }


            /*
            ------------------------------------------------------------------------------------------------------------
              -- Calcul de la direction : Si la direction est negative (temporaryDir) on lui ajoute 2 * PI (Un tour)
            ------------------------------------------------------------------------------------------------------------
            */
            double xValue = (eastWestDirection == 0) ? minusOne(eastWestSpeed) : -minusOne(eastWestSpeed);
            double yValue = (northSouthDirection == 0) ? minusOne(northSouthSpeed) : -minusOne(northSouthSpeed);
            double temporaryDir = Math.atan2(xValue, yValue);
            trackOrHeading = (temporaryDir < 0) ? temporaryDir + 2 * Math.PI : temporaryDir;
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Sous-Type 3 ou 4
    --------------------------------------------------------------------------------------------------------------------
    */
        else if (subType == 3 || subType == 4) {
            int heading = Bits.extractUInt(dependingBits, 11, 10);
            int airSpeed = Bits.extractUInt(dependingBits, 0, 10);
            if (airSpeed == 0) {
                return null;
            }


            /*
            ------------------------------------------------------------------------------------------------------------
              -- Calcul de la norme du vecteur vitesse
            ------------------------------------------------------------------------------------------------------------
            */
            speed = (subType == 3) ? Units.convertFrom(minusOne(airSpeed), Units.Speed.KNOT) :
                    Units.convertFrom(minusOne(airSpeed), Units.Speed.KNOT * 4);


            /*
            ------------------------------------------------------------------------------------------------------------
              -- Calcul de la direction
            ------------------------------------------------------------------------------------------------------------
            */
            if (Bits.testBit(dependingBits, 21)) {
                double twoToThePowerMinusTen = (double) 1 / (1 << 10);
                trackOrHeading = Units.convertFrom(heading * twoToThePowerMinusTen, Units.Angle.TURN);
            } else {
                return null;
            }

        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Sous-Type ni 1 ni 2 ni 3 ni 4
    --------------------------------------------------------------------------------------------------------------------
    */
        else {
            return null;
        }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création du message de vitesse en vol
    --------------------------------------------------------------------------------------------------------------------
    */
        return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                speed,
                trackOrHeading);
    }

    /**
     * Méthode qui rend la valeur qu'on lui donne en paramètre décrémentée de 1
     *
     * @param initialValue valeur à laquelle on veut soustraire 1
     * @return initialValue décrémentée de 1
     */
    private static double minusOne(double initialValue) {
        return initialValue - 1;
    }
}