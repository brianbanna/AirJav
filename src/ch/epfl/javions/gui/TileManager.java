package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe qui représente un gestionnaire de tuiles OSM.
 *
 * @author Brian Jean Claud El Banna (356437)
 * @author Nicolas Khamis (355598)
 */
public final class TileManager {
    public static final int MAX_ZOOM_LEVEL = 19;
    public static final int MIN_ZOOM_LEVEL = 6;
    private static final int MAX_CACHE_MEMORY = 100;
    private final LinkedHashMap<TileId, Image> memoryCache; // Un cache mémoire qui associe a chaque tuile une image
    private final Path cacheDiskPath; // Le chemin d'accès au dossier contenant le cache disque
    private final String tileServerName; // Le nom du serveur de tuile

    /**
     * Constructeur de la classe TileManager.
     *
     * @param cacheDiskPath  le chemin d'accès au dossier contenant le cache disque.
     * @param tileServerName le nom du serveur de tuile.
     */
    public TileManager(Path cacheDiskPath, String tileServerName) {
        this.cacheDiskPath = cacheDiskPath;
        this.tileServerName = tileServerName;
        memoryCache = new MemoryCache<>(); // On crée une instance de MemoryCache
    }

    /**
     * Méthode qui retourne l'image d'une tuile a partir de son identité.
     *
     * @param tileId l'identité de la tuile.
     * @return l'image de la tuile.
     * @throws IOException si une erreur s'est produite pendant l'écriture des données.
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Création du nom du fichier et du chemin d'accès.
    --------------------------------------------------------------------------------------------------------------------
    */
        /* On formatte le fichier de la manière suivante:
         - Le fichier est de type PNG
         - Il est composé (dans cet ordre) du:
           1. niveau de zoom
           2. la coordonnée x de la tuile
           3. la coordonnée y de la tuile
         - Chaque attribut est séparé de l'autre par un séparateur File.separator */
        String fileName = String.format(
                // On utilise %d pour indiquer qu'il y'a un nombre et %s pour indiquer un séparateur qui est un String
                "%d%s%d%s%d.png",
                tileId.zoom(),
                File.separator,
                tileId.x(),
                File.separator,
                tileId.y());

        // On crée un chemin d'accès qui correspond au nom du fichier
        Path filePath = cacheDiskPath.resolve(fileName).normalize();


    /*
    --------------------------------------------------------------------------------------------------------------------
      -- Obtention de l'image.
    --------------------------------------------------------------------------------------------------------------------
    */
        // Cas 1: l'image est dans le cache mémoire
        if (memoryCache.containsKey(tileId)) {
            return memoryCache.get(tileId);
        }
        // Cas 2: l'image est dans le cache disque
        else if (Files.exists(filePath)) {
            return getImageFromDiskCache(filePath, tileId);
        }
        // Cas 3: l'image est obtenue depuis le serveur de tuiles
        else {
            /* On formatte le fichier de la manière suivante:
             - Le fichier est de type PNG
             - Il est composé (dans cet ordre) du:
               1. niveau de zoom
               2. la coordonnée x de la tuile
               3. la coordonnée y de la tuile */
            String fileNameUrl = String.format(
                    "/%d/%d/%d.png",
                    tileId.zoom(),
                    tileId.x(),
                    tileId.y());

            // On crée un URL qui commence par "https" suivi par le nom du serveur, puis par L'URL du fichier
            URL u = new URL("https", tileServerName, fileNameUrl);
            URLConnection c = u.openConnection();
            c.setRequestProperty("User-Agent", "Javions");
            Files.createDirectories(filePath.getParent()); // On crée un nouveau directory

            // On transfert l'image importée du inputStream vers le outputStream
            try (InputStream inputStream = c.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                inputStream.transferTo(outputStream);
            }

            return getImageFromDiskCache(filePath, tileId);
        }
    }

    /**
     * Méthode qui charge une image du cache disque, la place dans le cache mémoire et retourne l'image.
     *
     * @param path   le chemin d'accès du document.
     * @param tileId l'identité de la tuile.
     * @return l'image de la tuile.
     * @throws IOException si une erreur s'est produite pendant l'écriture des données.
     */
    private Image getImageFromDiskCache(Path path, TileId tileId) throws IOException {
        try (InputStream i = new FileInputStream(path.toFile())) {
            Image image = new Image(i); // On charge l'image.
            memoryCache.put(tileId, image); // On place l'image dans le cache mémoire.
            return image; // On retourne l'image chargée.
        }
    }


    /**
     * Enregistrement imbriqué qui représente l'identité d'une tuile OSM.
     */
    record TileId(int zoom, int x, int y) {

        /**
         * Constructeur de l'enregistrement TileId qui vérifie la validité des arguments.
         *
         * @param zoom le niveau de zoom de la tuile
         * @param x    l'index X de la tuile
         * @param y    l'index Y de la tuile
         */
        TileId {
            Preconditions.checkArgument(isValid(zoom, x, y));
        }

        /**
         * Retourne vrai si et seulement si les arguments sont valides.
         *
         * @param zoom le niveau de zoom de la tuile
         * @param x    l'index X de la tuile
         * @param y    l'index Y de la tuile
         * @return vrai si et seulement si les arguments sont valides.
         */
        public static boolean isValid(int zoom, int x, int y) {
            int maxIndex = 2 << zoom - 1;
            return (MIN_ZOOM_LEVEL <= zoom && zoom <= MAX_ZOOM_LEVEL)
                    && (0 <= x && x <= maxIndex)
                    && (0 <= y && y <= maxIndex);
        }
    }

    /**
     * Classe qui limite la taille d'un cache mémoire.
     *
     * @param <K> type générique des clés.
     * @param <V> type générique des valeurs.
     */
    private static class MemoryCache<K, V> extends LinkedHashMap<K, V> {
        /**
         * Constructeur de la classe LeastUsefulCache.
         */
        public MemoryCache() {
            super(MAX_CACHE_MEMORY);
        }

        /**
         * Méthode qui détermine si un élément doit etre supprimé.
         *
         * @param leastRecentlyUsedEntry l'image utilisée le moins récemment.
         * @return vrai si et seulement si la taille de table associative excède la capacité maximale du cache.
         */

        @Override
        protected boolean removeEldestEntry(Map.Entry leastRecentlyUsedEntry) {
            return size() > MAX_CACHE_MEMORY;
        }
    }
}