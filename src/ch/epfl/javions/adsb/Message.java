package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Interface qui a pour but d'être implémentée par toutes les classes représentant des messages ADS-B «analysés».
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */

public interface Message {
    /**
     * Méthode qui retourne l'horodatage du message, en nanosecondes.
     *
     * @return l'horodatage du message, en nanosecondes.
     */
    long timeStampNs(); // méthode qui est publique et abstraite


    /**
     * Méthode qui retourne l'adresse OACI de l'expéditeur du message.
     *
     * @return l'adresse OACI de l'expéditeur du message.
     */
    IcaoAddress icaoAddress(); // méthode qui est publique et abstraite
}
