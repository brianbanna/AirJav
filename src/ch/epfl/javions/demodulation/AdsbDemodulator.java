package ch.epfl.javions.demodulation;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


/**
 * Classe représentant un démodulateur de messages ADS-B.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class AdsbDemodulator {
    private final PowerWindow powerWindow;
    private final static int WINDOW_SIZE = 1200;
    private final static int ADSB_LENGTH = 14;
    private final byte[] adsbMessage = new byte[ADSB_LENGTH];


    /**
     * Constructeur de la classe AdsbDemodulator qui retourne un démodulateur obtenant les octets contenant les
     * échantillons du flot passé en argument
     *
     * @param sampleStream échantillons du flot.
     * @throws IOException si une erreur d'entrée/sortie se produit lors de la création
     *                     de l'objet de type PowerWindow représentant la fenêtre de 1200 échantillons de puissance,
     *                     utilisée pour rechercher les messages.
     */
    public AdsbDemodulator(InputStream sampleStream) throws IOException {
        powerWindow = new PowerWindow(sampleStream, WINDOW_SIZE);
    }

    /**
     * Méthode qui retourne le prochain message ADS-B du flot d'échantillons passé au constructeur,
     * ou null s'il n'y en a plus c.-à-d. que la fin du flot d'échantillons a été atteinte.
     *
     * @return le prochain message ADS-B du flot d'échantillons passé au constructeur
     * ou null s'il n'y en a plus c.-à-d. que la fin du flot d'échantillons a été atteinte.
     * @throws IOException en cas d'erreur d'entrée/sortie.
     */
    public RawMessage nextMessage() throws IOException {
        //Somme des Pics Précedente
        int sumPrevious = 0;
        //Somme des Pics Actuelle
        int sumPics = powerWindow.get(0)
                + powerWindow.get(10)
                + powerWindow.get(35)
                + powerWindow.get(45);

        while (powerWindow.isFull()) {
            //Prochaine Somme des Pics
            int sumNext = powerWindow.get(1)
                    + powerWindow.get(11)
                    + powerWindow.get(36)
                    + powerWindow.get(46);
            //Somme des vallées
            int sumValleys = powerWindow.get(5)
                    + powerWindow.get(15)
                    + powerWindow.get(20)
                    + powerWindow.get(25)
                    + powerWindow.get(30)
                    + powerWindow.get(40);

            //Conditions vérifiées en présence d’un préambule
            boolean sumPicsIsBiggerThanDoubleSumValley = sumPics >= 2 * sumValleys;
            boolean sumPicsIsStrictlyBiggerThanSurroundingSums = (sumPics > sumPrevious) && (sumPics > sumNext);

            boolean preambleIsFound = sumPicsIsBiggerThanDoubleSumValley &&
                    sumPicsIsStrictlyBiggerThanSurroundingSums;


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Décodage du premier bit

        À chaque itération, la fonction "Decode(i)" est appelée et le résultat est décalé vers la gauche de (7 - i) bits
        Ensuite, le résultat est OR avec la valeur de "adsbMessage[0]". Finalement, la valeur résultante est stockée
        dans "adsbMessage[0]".
    --------------------------------------------------------------------------------------------------------------------
    */
            if (preambleIsFound) {
                //Remplissage du tableau de 0
                Arrays.fill(adsbMessage, (byte) 0);
                for (int i = 0; i < Byte.SIZE; i++) {
                    adsbMessage[0] |= ((Decode(i) << (7 - i)));
                }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Décodage du reste des bits

        Le code fourni est une double boucle for en Java qui parcourt le tableau d'octets "adsbMessage" et qui effectue
        des opérations de décodage.La boucle externe commence à l'index 1 de "adsbMessage" (puisque le premier élément a
        déjà été traité.À chaque itération de la boucle interne, la variable "index" est calculée (position du bit
        correspondant)La fonction "Decode(index)" est ensuite appelée pour récupérer la valeur du bit correspondant de
        "adsbMessage". Cette valeur est ensuite décalée vers la gauche de (7 - j) bits et OR avec la valeur actuelle de
        "adsbMessage[i]". Finalement, la valeur résultante est stockée dans "adsbMessage[i]".
    --------------------------------------------------------------------------------------------------------------------
    */
                //Condition vérifiée lorsque l'attribut DF est valide
                boolean downlinkFormatIsValid = (RawMessage.size(adsbMessage[0]) == RawMessage.LENGTH);
                if (downlinkFormatIsValid) {
                    for (int i = 1; i < ADSB_LENGTH; i++) {
                        for (int j = 0; j < Byte.SIZE; j++) {
                            int index = j + i * Byte.SIZE;
                            adsbMessage[i] |= ((Decode(index) << (7 - j)));
                        }
                    }


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création du message
    --------------------------------------------------------------------------------------------------------------------
    */
                    /* Conversion de 0.1 microseconde à nanoseconde
                    (Le passage d'une position à une autre prend 1 microseconde, on multiplie par 100
                    donc on divise par 0.01 (Units.CENTI)) */

                    long timeStamps = (long) (powerWindow.position() / Units.CENTI);
                    RawMessage message = RawMessage.of(timeStamps, adsbMessage);

                    // Cas ou le  message est trouvé (crc est valide)
                    if (message != null) {
                        powerWindow.advanceBy(WINDOW_SIZE);   // On avance la fenetre de 1200 positions
                        return message;
                    }

                    // Cas ou le message est ignoré (crc invalide)
                    else {
                        powerWindow.advance();
                        sumPrevious = sumPics; // sumPrevious prend la valeur de la somme actuelle
                    }

                } else {
                    powerWindow.advance();
                    sumPrevious = sumPics; // sumPrevious prend la valeur de la somme actuelle
                    sumPics = sumNext; // sumPics prend la valeur de sumNext
                }

            } else {
                powerWindow.advance();
                sumPrevious = sumPics; // sumPrevious prend la valeur de la somme actuelle
                sumPics = sumNext; // sumPics prend la valeur de sumNext*
            }
        }
        return null;
    }

    /**
     * Méthode qui réalise le décodage des bits
     *
     * @param index index du bit
     * @return 0 si la somme des Pics à la position 80 + index * 10 est inferieure à la somme des pics à la position
     * 85 + index * 10 et 1 sinon
     */
    private byte Decode(int index) {
        boolean sumPics80IsLessThanSumPics85 = powerWindow.get(80 + index * 10) < powerWindow.get(85 + index * 10);
        return (byte) (sumPics80IsLessThanSumPics85 ? 0 : 1);
    }
}