package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Interface qui a pour but d'être implémentée par toutes les classes représentant l'état (modifiable) d'un aéronef.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */

public interface AircraftStateSetter {
    /**
     * Méthode qui change l'horodatage du dernier message reçu de l'aéronef à la valeur donnée.
     *
     * @param timeStampNs nouvelle valeur de l'horodatage de l'aéronef.
     */
    void setLastMessageTimeStampNs(long timeStampNs); // méthode qui est publique et abstraite

    /**
     * Méthode qui change la catégorie de l'aéronef à la valeur donnée.
     *
     * @param category nouvelle valeur de la catégorie de l'aeronef.
     */
    void setCategory(int category); // méthode qui est publique et abstraite

    /**
     * Méthode qui change l'indicatif de l'aéronef à la valeur donnée.
     *
     * @param callSign nouvel indicatif de l'aeronef.
     */
    void setCallSign(CallSign callSign); // méthode qui est publique et abstraite

    /**
     * Méthode qui change la position de l'aéronef à la valeur donnée.
     *
     * @param position nouvelle position de l'aeronef.
     */
    void setPosition(GeoPos position); // méthode qui est publique et abstraite

    /**
     * Méthode qui change l'altitude de l'aéronef à la valeur donnée.
     *
     * @param altitude nouvelle valeur de l'altitude de l'aeronef.
     */
    void setAltitude(double altitude); // méthode qui est publique et abstraite

    /**
     * Méthode qui change la vitesse de l'aéronef à la valeur donnée.
     *
     * @param velocity nouvelle valeur de la vitesse de l'aeronef.
     */
    void setVelocity(double velocity); // méthode qui est publique et abstraite

    /**
     * Méthode qui change la direction de l'aéronef à la valeur donnée.
     *
     * @param trackOrHeading nouvelle valeur de la direction de l'aeronef.
     */
    void setTrackOrHeading(double trackOrHeading); // méthode qui est publique et abstraite
}
