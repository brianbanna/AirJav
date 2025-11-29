package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Enregistrement qui collecte les données fixes d'un aéronef.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public record AircraftData(AircraftRegistration registration,
                           AircraftTypeDesignator typeDesignator,
                           String model,
                           AircraftDescription description,
                           WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * Constructeur compact de la classe aircraftData/
     *
     * @param registration           immatriculation de l'aéronef.
     * @param typeDesignator         type de l'aéronef.
     * @param model                  modele de l'aéronef.
     * @param description            description de l'aéronef.
     * @param wakeTurbulenceCategory catégorie de turbulence de sillage de l'aéronef.
     * @throws NullPointerException si l'un de ses arguments est nul.
     */
    public AircraftData {
        // Vérification que les arguments ne sont pas null
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
        Objects.requireNonNull(model);
    }
}