package ch.epfl.javions.aircraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipFile;

/**
 * Classe qui représente la base de données mictronics des aéronefs.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class AircraftDatabase {
    private final String fileName;

    /**
     * Constructeur de la classe AircraftDatabase
     *
     * @param fileName nom du fichier dont on va extraire les données.
     * @throws NullPointerException if fileName is null.
     */
    public AircraftDatabase(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    /**
     * Methode qui retourne les données de l'aéronef dont l'adresse OACI est celle donnée.
     *
     * @param address Addresse OACI de l'aéronef.
     * @return les données de l'aéronef dont l'adresse OACI est celle données.
     * @throws IOException en cas d'erreur d'entrée/sortie.
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        try (ZipFile zipFile = new ZipFile(fileName);
             InputStream stream = zipFile.getInputStream(zipFile.getEntry
                     (address.string().substring(4, 6)
                             + ".csv"));
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Recherche de la ligne correspondante et découpage de cette derniere en colonnes.
    --------------------------------------------------------------------------------------------------------------------
    */
            while ((line = bufferedReader.readLine()) != null) {
                int index = line.compareTo(address.string());
                boolean lineIsFound = index > 0 && line.startsWith(address.string());
                if (lineIsFound) {
                    String[] aircraftData = line.split(",", -1);
                    return new AircraftData(
                            new AircraftRegistration(aircraftData[1]),
                            new AircraftTypeDesignator(aircraftData[2]),
                            aircraftData[3],
                            new AircraftDescription(aircraftData[4]),
                            WakeTurbulenceCategory.of(aircraftData[5]));
                }
            }
        }
        return null;
    }
}